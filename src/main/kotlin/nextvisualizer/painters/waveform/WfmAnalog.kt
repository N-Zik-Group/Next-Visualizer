package nextvisualizer.painters.waveform

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import nextvisualizer.painters.Painter
import nextvisualizer.utils.VisualizerHelper

class WfmAnalog(
    val colorPaint: Int = Color.WHITE,
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPaint;style = Paint.Style.STROKE;strokeWidth = 6f
    },
    //
    var num: Int = 256,
    //
    var ampR: Float = 1f,
    // Smoothing factor: 0.0 = frozen, 1.0 = instant (raw).
    // 0.35 gives smooth interpolation between ~43Hz captures at 120Hz display.
    var smooth: Float = 0.35f
) : Painter() {

    private var skipFrame = false
    private lateinit var waveform: ByteArray
    // Pre-allocated buffer for drawLines: each segment needs 4 floats (x1,y1,x2,y2)
    private var lines = FloatArray(num * 4)
    // Smoothed Y values for interpolation between captures
    private var smoothedY = FloatArray(num + 1)
    private var initialized = false

    override fun calc(helper: VisualizerHelper) {
        waveform = helper.getWave()

        // Detect silence directly from waveform data (values near 128 = silence)
        var quiet = true
        val step = waveform.size / 32
        for (i in 0 until waveform.size step step.coerceAtLeast(1)) {
            val sample = (waveform[i].toInt() and 0xFF) - 128
            if (sample > 3 || sample < -3) {
                quiet = false
                break
            }
        }

        skipFrame = quiet
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        if (skipFrame) {
            // Decay smoothed values toward 0 during silence for smooth fade-out
            if (initialized) {
                var allZero = true
                for (i in smoothedY.indices) {
                    smoothedY[i] *= (1f - smooth)
                    if (smoothedY[i] > 0.5f || smoothedY[i] < -0.5f) allZero = false
                }
                if (allZero) return
                // Still draw the fade-out
            } else return
        }

        val width = canvas.width.toFloat()
        val point = waveform.size / (num + 1)
        val sliceWidth = width / num

        // Ensure buffers are correct size
        if (smoothedY.size != num + 1) {
            smoothedY = FloatArray(num + 1)
            initialized = false
        }
        val needed = num * 4
        if (lines.size != needed) lines = FloatArray(needed)

        // Compute target Y values and apply exponential smoothing (lerp)
        // This interpolates between ~43Hz waveform captures at display refresh rate
        val alpha = smooth
        for (i in 0..num) {
            val targetY = ((waveform[point * i].toInt() and 0xFF) - 128f) * -ampR
            if (!initialized) {
                smoothedY[i] = targetY
            } else {
                smoothedY[i] += (targetY - smoothedY[i]) * alpha
            }
        }
        initialized = true

        // Fill line segments from smoothed values
        var prevX = 0f
        var prevY = smoothedY[0]
        for (i in 1..num) {
            val x = sliceWidth * i
            val idx = (i - 1) * 4
            lines[idx] = prevX
            lines[idx + 1] = prevY
            lines[idx + 2] = x
            lines[idx + 3] = smoothedY[i]
            prevX = x
            prevY = smoothedY[i]
        }

        drawHelper(canvas, "a", 0f, .5f) { canvas.drawLines(lines, paint) }
    }
}
