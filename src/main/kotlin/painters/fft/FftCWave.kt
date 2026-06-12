package painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import painters.Painter
import utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.PI
import kotlin.math.min

class FftCWave(
    var colorPaint: Int = Color.TRANSPARENT,
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
    var power: Boolean = true,
    //
    var radiusR: Float = .4f,
    var ampR: Float = .6f
) : Painter() {

    private val path = Path()
    private var points = Array(0) { GravityModel() }
    private var skipFrame = false
    private val fft = DoubleArray(260)
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
        
        if (quiet && paint.style == Paint.Style.STROKE) {
            skipFrame = true
            return
        } else skipFrame = false

        if (power) applyPowerFft(fft, filled)
        
        var processingFft = fft
        var validSize = filled

        if (mirror) {
             val temp = fft.copyOfRange(0, validSize)
             processingFft = getMirrorFft(temp)
             validSize = processingFft.size
        } else {
             validSize = fillCircleFft(fft, validSize, fft)
             processingFft = fft
        }

        if (points.size != validSize) points = Array(validSize) { GravityModel(0f) }
        points.forEachIndexed { index, bar -> bar.update(processingFft[index].toFloat() * ampR) }

        psf = interpolateFftCircle(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        if (skipFrame) return

        val angle = 2 * PI.toFloat() / num
        val shortest = min(canvas.width, canvas.height)

        drawHelper(canvas, side, .5f, .5f, {
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()
            canvas.drawPath(path, paint)
            path.reset()
        }, {
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR, angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()
            path.fillType = Path.FillType.EVEN_ODD
            canvas.drawPath(path, paint)
            path.reset()
        }, {
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()
            path.fillType = Path.FillType.EVEN_ODD
            canvas.drawPath(path, paint)
            path.reset()
        })
    }


}


