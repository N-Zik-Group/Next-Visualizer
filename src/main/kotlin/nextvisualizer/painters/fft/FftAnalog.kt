package nextvisualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import nextvisualizer.painters.Painter
import nextvisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

class FftAnalog(
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    //
    var startHz: Int = 0,
    var endHz: Int = 2000,
    //
    var num: Int = 128,
    var interpolator: String = "li",
    //
    var mirror: Boolean = false,
    var power: Boolean = false,
    //
    var ampR: Float = 1f
) : Painter() {

    private var points = Array(0) { GravityModel() }
    private val path = Path()
    private var skipFrame = false
    private val fft = DoubleArray(256)
    lateinit var psf: PolynomialSplineFunction

    override fun calc(helper: VisualizerHelper) {
        val filled = helper.fillFftMagnitudeRange(startHz, endHz, fft)
        var processingFft = fft
        var validSize = filled

        var quiet = true
        for (i in 0 until validSize) {
            if (processingFft[i] > 5f) {
                quiet = false
                break
            }
        }
        
        if (quiet) {
            skipFrame = true
            return
        } else skipFrame = false

        if (power) applyPowerFft(processingFft, validSize)
        
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
        val gapWidth = width / num

        drawHelper(canvas, "a", 0f, .5f) {
            for (i in 0 until num) {
                if (i % 2 == 0)
                    if (i == 0)
                        path.moveTo(gapWidth * i, -psf.value(i.toDouble()).toFloat())
                    else
                        path.lineTo(gapWidth * i, -psf.value(i.toDouble()).toFloat())
                else
                    path.lineTo(gapWidth * i, psf.value(i.toDouble()).toFloat())
            }
            canvas.drawPath(path, paint)
        }
        path.reset()
    }
}


