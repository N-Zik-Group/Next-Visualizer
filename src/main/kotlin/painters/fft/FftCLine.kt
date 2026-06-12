package painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import painters.Painter
import utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.PI
import kotlin.math.min

class FftCLine(
    val colorPaint: Int = Color.WHITE,
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPaint;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    //
    var startHz: Int = 0,
    var endHz: Int = 2000,
    //
    var num: Int = 64,
    var interpolator: String = "li",
    //
    var side: String = "a",
    var mirror: Boolean = false,
    var power: Boolean = true,
    //
    var radiusR: Float = .4f,
    var ampR: Float = 1f
) : Painter() {

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
        
        if (quiet) {
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
        val pts = FloatArray(4 * num)
        val shortest = min(canvas.width, canvas.height)

        drawHelper(canvas, side, .5f, .5f, {
            for (i in 0 until num) {
                val start =
                    toCartesian(shortest / 2f * radiusR, angle * i)
                val stop = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i
                )
                pts[4 * i] = start[0];pts[4 * i + 1] = start[1]
                pts[4 * i + 2] = stop[0];pts[4 * i + 3] = stop[1]
            }
            canvas.drawLines(pts, paint)
        }, {
            for (i in 0 until num) {
                val start =
                    toCartesian(shortest / 2f * radiusR, angle * i)
                val stop = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i
                )
                pts[4 * i] = start[0];pts[4 * i + 1] = start[1]
                pts[4 * i + 2] = stop[0];pts[4 * i + 3] = stop[1]
            }
            canvas.drawLines(pts, paint)
        }, {
            for (i in 0 until num) {
                val start =
                    toCartesian(
                        shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i
                    )
                val stop = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i
                )
                pts[4 * i] = start[0];pts[4 * i + 1] = start[1]
                pts[4 * i + 2] = stop[0];pts[4 * i + 3] = stop[1]
            }
            canvas.drawLines(pts, paint)
        })
    }
}


