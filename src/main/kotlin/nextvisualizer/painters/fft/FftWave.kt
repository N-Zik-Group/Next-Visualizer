package nextvisualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import nextvisualizer.painters.Painter
import nextvisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

class FftWave(
    val colorPaint: Int = Color.WHITE,
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPaint
    },
    //
    var startHz: Int = 0,
    var endHz: Int = 2000,
    //
    var num: Int = 128,
    var interpolator: String = "sp",
    //
    var side: String = "a",
    var mirror: Boolean = false,
    var power: Boolean = false,
    //
    var ampR: Float = 1f
) : Painter() {

    private val path = Path()
    private var points = Array(0) { GravityModel() }
    private var skipFrame = false
    private val fft = DoubleArray(256)
    lateinit var psf: PolynomialSplineFunction

    override fun calc(helper: VisualizerHelper) {
        val filled = helper.fillFftMagnitudeRange(startHz, endHz, fft)
        
        var quiet = true
        for (i in 0 until filled) {
            if (fft[i] > 5f) {
                quiet = false
                break
            }
        }
        
        if (quiet) {
            skipFrame = true
            return
        } else skipFrame = false

        if (power) applyPowerFft(fft, filled)
        
        var processingFft = fft
        var validSize = filled

        if (mirror) {
            val temp = processingFft.copyOfRange(0, validSize)
            val mirrored = getMirrorFft(temp)
            processingFft = mirrored
            validSize = mirrored.size
        }

        if (points.size != validSize) points =
            Array(validSize) { GravityModel(0f) }
            
        points.forEachIndexed { index, bar -> bar.update(processingFft[index].toFloat() * ampR) }

        psf = interpolateFft(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        if (skipFrame) return

        val width = canvas.width.toFloat()

        val sliceWidth = width / num
        if (paint.style == Paint.Style.STROKE) {
            path.moveTo(0f, -psf.value(0.0).toFloat())
            for (i in 1..num) {
                path.lineTo(sliceWidth * i, -psf.value(i.toDouble()).toFloat())
            }
        } else {
            path.moveTo(0f, 1f)
            for (i in 0..num) {
                path.lineTo(sliceWidth * i, -psf.value(i.toDouble()).toFloat())
            }
            path.lineTo(width, 1f)
            path.close()
        }
        drawHelper(canvas, side, 0f, .5f) { canvas.drawPath(path, paint) }
        path.reset()
    }
}


