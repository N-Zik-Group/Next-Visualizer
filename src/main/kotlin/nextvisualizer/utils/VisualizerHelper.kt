package nextvisualizer.utils

import android.media.audiofx.Visualizer
import android.os.Handler
import timber.log.Timber

class VisualizerHelper(val sessionId: Int) {

    companion object {
        private val sharedVisualizers = java.util.concurrent.ConcurrentHashMap<Int, Visualizer>()

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
                e.printStackTrace()
                null
            }
        }
        
        @Synchronized
        fun releaseShared() {
            sharedVisualizers.values.forEach { it.release() }
            sharedVisualizers.clear()
        }
    }

    private val fftBuff: ByteArray = ByteArray(Visualizer.getCaptureSizeRange()[1])
    private val waveBuff: ByteArray = ByteArray(Visualizer.getCaptureSizeRange()[1])
    private val fftMF: FloatArray = FloatArray(fftBuff.size / 2 - 1)
    private val fftM: DoubleArray = DoubleArray(fftBuff.size / 2 - 1)
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    fun getFft(): ByteArray {
        val visualizer = getSharedVisualizer(sessionId)
        visualizer?.let { if (it.enabled) it.getFft(fftBuff) }
        return fftBuff
    }

    fun getWave(): ByteArray {
        val visualizer = getSharedVisualizer(sessionId)
        visualizer?.let { if (it.enabled) it.getWaveForm(waveBuff) }
        return waveBuff
    }

    fun getSamplingRate(): Int {
        val visualizer = getSharedVisualizer(sessionId)
        return try { visualizer?.samplingRate ?: 44100000 } catch (e: Exception) { 44100000 }
    }

    fun getFftMagnitude(): DoubleArray {
        getFft()
        for (k in 0 until fftMF.size) {
            val i = (k + 1) * 2
            fftM[k] = Math.hypot(fftBuff[i].toDouble(), fftBuff[i + 1].toDouble())
        }
        return fftM
    }

    /**
     * Fill the provided output array with Fft values from startHz to endHz
     * Returns the number of elements copied
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


