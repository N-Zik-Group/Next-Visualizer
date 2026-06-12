package nextvisualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import nextvisualizer.painters.Painter
import nextvisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.PI
import kotlin.math.min

class FftCPoly(
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
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
    var gapX: Float = 0f,
    var ampR: Float = 1f
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

        val shortest = min(canvas.width, canvas.height)
        val gapTheta = gapX / (shortest / 2f * radiusR)
        val angle = 2 * PI.toFloat() / num - gapTheta

        drawHelper(canvas, side, .5f, .5f, {
            for (i in 0 until num) {
                val start1 =
                    toCartesian(shortest / 2f * radiusR, (angle + gapTheta) * i)
                val stop1 = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val start2 =
                    toCartesian(shortest / 2f * radiusR, (angle) * (i + 1) + gapTheta * i)
                val stop2 = toCartesian(
                    shortest / 2f * radiusR + psf.value((i + 1).toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )
                path.moveTo(start1[0], start1[1])
                path.lineTo(stop1[0], stop1[1])
                path.lineTo(stop2[0], stop2[1])
                path.lineTo(start2[0], start2[1])
                path.close()
            }
            canvas.drawPath(path, paint)
        }, {
            for (i in 0 until num) {
                val start1 =
                    toCartesian(shortest / 2f * radiusR, (angle + gapTheta) * i)
                val stop1 = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val start2 =
                    toCartesian(shortest / 2f * radiusR, (angle) * (i + 1) + gapTheta * i)
                val stop2 = toCartesian(
                    shortest / 2f * radiusR - psf.value((i + 1).toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )
                path.moveTo(start1[0], start1[1])
                path.lineTo(stop1[0], stop1[1])
                path.lineTo(stop2[0], stop2[1])
                path.lineTo(start2[0], start2[1])
                path.close()
            }
            canvas.drawPath(path, paint)
        }, {
            for (i in 0 until num) {
                val start1 =
                    toCartesian(
                        shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                    )
                val stop1 = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val start2 =
                    toCartesian(
                        shortest / 2f * radiusR + psf.value((i + 1).toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                    )
                val stop2 = toCartesian(
                    shortest / 2f * radiusR - psf.value((i + 1).toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )
                path.moveTo(start1[0], start1[1])
                path.lineTo(stop1[0], stop1[1])
                path.lineTo(stop2[0], stop2[1])
                path.lineTo(start2[0], start2[1])
                path.close()
            }
            canvas.drawPath(path, paint)
        })
        path.reset()
    }
}


