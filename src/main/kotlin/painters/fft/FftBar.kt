package painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import painters.Painter
import utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

class FftBar(
    val colorPaint: Int = Color.WHITE,
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPaint;style = Paint.Style.FILL;strokeWidth = 2f
    },
    //
    var startHz: Int = 0,
    var endHz: Int = 2000,
    //
    var num: Int = 128,
    var interpolator: String = "li",
    //
    var side: String = "a",
    var mirror: Boolean = false,
    var power: Boolean = false,
    //
    var gapX: Float = 4f,
    var ampR: Float = 1f
) : Painter() {

    private val path = Path()
    private var points = Array(0) { GravityModel() }
    private var skipFrame = false
    private val fft = DoubleArray(256)
    private lateinit var psf: PolynomialSplineFunction

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
        } else {
            skipFrame = false
        }

        if (power) {
            applyPowerFft(fft, filled)
        }
        
        var processingFft: DoubleArray
        var validSize: Int

        if (mirror) {
            val temp = fft.copyOfRange(0, filled)
            val mirrored = getMirrorFft(temp)
            processingFft = mirrored
            validSize = mirrored.size
        } else {
            processingFft = fft
            validSize = filled
        }

        if (points.size != validSize) {
            points = Array(validSize) { GravityModel(0f) }
        }
            
        points.forEachIndexed { index, bar -> bar.update(processingFft[index].toFloat() * ampR) }

        psf = interpolateFft(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        if (skipFrame) return

        val width = canvas.width.toFloat()
        val barWidth = (width - (num + 1) * gapX) / num

        drawHelper(canvas, side, 0f, .5f, {
            for (i in 0 until num) {
                path.moveTo(barWidth * i + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), 0f)
                path.lineTo(barWidth * i + gapX * (i + 1), 0f)
                path.close()
            }
            canvas.drawPath(path, paint)
        }, {
            for (i in 0 until num) {
                path.moveTo(barWidth * i + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * i + gapX * (i + 1), psf.value(i.toDouble()).toFloat())
                path.close()
            }
            canvas.drawPath(path, paint)
        })
        path.reset()
    }
}



