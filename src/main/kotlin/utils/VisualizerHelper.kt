package utils

import android.media.audiofx.Visualizer
import android.os.Handler
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Immutable capture published once per frame by the app-side capture coordinator (issue #606).
 * [fft] and [waveform] are fresh copies for this frame, never the shared native `Visualizer`
 * buffers -- readers must never mutate them, they may be aliased by multiple concurrent readers.
 */
data class VisualizerSnapshot(
    val fft: ByteArray,
    val waveform: ByteArray,
    val samplingRate: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisualizerSnapshot) return false
        return samplingRate == other.samplingRate &&
            fft.contentEquals(other.fft) &&
            waveform.contentEquals(other.waveform)
    }

    override fun hashCode(): Int {
        var result = fft.contentHashCode()
        result = 31 * result + waveform.contentHashCode()
        result = 31 * result + samplingRate
        return result
    }
}

class VisualizerHelper(val sessionId: Int) {

    companion object {
        private val sharedVisualizers = ConcurrentHashMap<Int, Visualizer>()

        /**
         * Functional injection point wired once by `app.n_zik.android`'s
         * `VisualizerCaptureCoordinator` (see ARCHITECTURE-SPINE AD-2/AD-5 - this module cannot
         * depend on coroutines, so it exposes a plain Kotlin function type instead). Defaults to
         * `{ null }` so [getFft]/[getWave] transparently fall back to the pre-migration direct
         * native call when nothing wires this provider.
         */
        @Volatile
        var snapshotProvider: (Int) -> VisualizerSnapshot? = { null }

        private val warnedFallback = AtomicBoolean(false)

        @Synchronized
        fun getSharedVisualizer(sessionId: Int): Visualizer? {
            if (sessionId < 0) return null
            if (sharedVisualizers.containsKey(sessionId)) {
                return sharedVisualizers[sessionId]
            }
            return try {
                val v = Visualizer(sessionId)
                v.enabled = false
                v.captureSize = Visualizer.getCaptureSizeRange()[1]
                v.enabled = true
                sharedVisualizers[sessionId] = v
                v
            } catch (e: Exception) {
                Timber.e(e, "VisualizerHelper: Failed to get shared visualizer")
                null
            }
        }

        @Synchronized
        fun releaseShared() {
            sharedVisualizers.values.forEach { it.release() }
            sharedVisualizers.clear()
        }

        /**
         * The single point of direct `Visualizer.getFft()`/`getWaveForm()` capture for
         * [sessionId]. Callers (the `VisualizerCaptureCoordinator`) must only ever invoke this
         * from `NzikDispatchers.VISUALIZER` and only from one capture loop per active sessionId
         * (AD-2) -- this function has no thread affinity of its own, serialization is the
         * caller's responsibility. Returns a fresh, immutable [VisualizerSnapshot] every call;
         * never hands out the shared native buffers.
         */
        fun captureNow(sessionId: Int): VisualizerSnapshot {
            val visualizer = getSharedVisualizer(sessionId)
            val size = Visualizer.getCaptureSizeRange()[1]
            val fft = ByteArray(size)
            val waveform = ByteArray(size)
            var samplingRate = 44100000
            if (visualizer != null && visualizer.enabled) {
                visualizer.getFft(fft)
                visualizer.getWaveForm(waveform)
                samplingRate = try {
                    visualizer.samplingRate
                } catch (e: Exception) {
                    samplingRate
                }
            }
            return VisualizerSnapshot(fft = fft, waveform = waveform, samplingRate = samplingRate)
        }

        /** Logs the pre-migration-fallback warning exactly once for the process lifetime. */
        private fun warnFallbackOnce() {
            if (warnedFallback.compareAndSet(false, true)) {
                Timber.tag("VisualizerHelper").w(
                    "snapshotProvider is not wired -- falling back to direct native Visualizer " +
                        "capture. Did the app forget to set VisualizerHelper.snapshotProvider " +
                        "(VisualizerCaptureCoordinator)?"
                )
            }
        }

        /**
         * Test-only: resets the "already warned" latch and the default no-op provider so each
         * test can observe the fallback warning firing independently of test execution order.
         *
         * Must never be called from production code -- doing so silently reverts the whole
         * capture-coordinator seam ([snapshotProvider]) to the pre-migration direct-native
         * fallback path. Public (not `internal`) only because the test that calls it lives in a
         * different Gradle module.
         */
        @VisibleForTesting
        fun resetForTests() {
            warnedFallback.set(false)
            snapshotProvider = { null }
        }
    }

    // Native-fallback-only scratch buffers. Never reassigned to a VisualizerSnapshot's arrays --
    // those are documented immutable and may be aliased by other concurrent readers.
    private val fftBuff: ByteArray = ByteArray(Visualizer.getCaptureSizeRange()[1])
    private val waveBuff: ByteArray = ByteArray(Visualizer.getCaptureSizeRange()[1])
    private val fftMF: FloatArray = FloatArray(fftBuff.size / 2 - 1)
    private val fftM: DoubleArray = DoubleArray(fftBuff.size / 2 - 1)
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    fun getFft(): ByteArray {
        val snapshot = snapshotProvider(sessionId)
        if (snapshot != null) {
            return snapshot.fft
        }
        warnFallbackOnce()
        val visualizer = getSharedVisualizer(sessionId)
        visualizer?.let { if (it.enabled) it.getFft(fftBuff) }
        return fftBuff
    }

    fun getWave(): ByteArray {
        val snapshot = snapshotProvider(sessionId)
        if (snapshot != null) {
            return snapshot.waveform
        }
        warnFallbackOnce()
        val visualizer = getSharedVisualizer(sessionId)
        visualizer?.let { if (it.enabled) it.getWaveForm(waveBuff) }
        return waveBuff
    }

    fun getSamplingRate(): Int {
        val visualizer = getSharedVisualizer(sessionId)
        return try { visualizer?.samplingRate ?: 44100000 } catch (e: Exception) { 44100000 }
    }

    /**
     * Transitively reads the snapshot via [getFft] -- no direct native call lives here, so no
     * further change was needed for this method to stop calling `Visualizer.getFft()` directly.
     */
    fun getFftMagnitude(): DoubleArray {
        val fft = getFft()
        for (k in 0 until fftMF.size) {
            val i = (k + 1) * 2
            fftM[k] = Math.hypot(fft[i].toDouble(), fft[i + 1].toDouble())
        }
        return fftM
    }

    /**
     * Fill the provided output array with Fft values from startHz to endHz
     * Returns the number of elements copied
     *
     * Transitively reads the snapshot via [getFftMagnitude]/[getFft] -- no direct native call
     * lives here either.
     */
    fun fillFftMagnitudeRange(startHz: Int, endHz: Int, output: DoubleArray): Int {
        val sIndex = hzToFftIndex(startHz)
        val eIndex = hzToFftIndex(endHz)

        // Update internal fftM
        getFftMagnitude()

        val length = eIndex - sIndex
        if (length <= 0) return 0

        val copyLength = Math.min(length, output.size)
        // Safe copy
        if (sIndex + copyLength <= fftM.size) {
            System.arraycopy(fftM, sIndex, output, 0, copyLength)
        }
        return copyLength
    }

    /**
     * Get Fft values from startHz to endHz
     */
    fun getFftMagnitudeRange(startHz: Int, endHz: Int): DoubleArray {
        val sIndex = hzToFftIndex(startHz)
        val eIndex = hzToFftIndex(endHz)
        return getFftMagnitude().copyOfRange(sIndex, eIndex)
    }

    /**
     * Equation from documentation, kth frequency = k*Fs/(n/2)
     */
    fun hzToFftIndex(Hz: Int): Int {
        return Math.min(Math.max(Hz * 1024 / (44100 * 2), 0), 255)
    }

    /**
     * Log WfmAnalog and Fft values every 1s
     */
//    fun startDebug() {
//        handler = Handler()
//        runnable = object : Runnable {
//            override fun run() {
//                Timber.tag("WfmAnalog").d(getWave().contentToString())
//                Timber.tag("Fft").d(getFftMagnitude().contentToString())
//                handler.postDelayed(this, 1000)
//            }
//        }
//        handler.post(runnable)
//    }

    /**
     * Stop logging
     */
    fun stopDebug() {
        handler.removeCallbacks(runnable)
    }

    /**
     * Release visualizer when not using anymore
     */
    fun release() {
        // We use a shared visualizer, so we don't release it here.
    }

}
