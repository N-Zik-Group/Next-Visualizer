package painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import painters.Painter
import utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

class FlatWave(
    val colorPaint: Int = Color.WHITE,
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPaint
        style = Paint.Style.STROKE
    },
    var startHz: Int = 0,
    var endHz: Int = 2000,
    var num: Int = 16,
    var ampR: Float = 4f
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

        if (points.size != filled) points = Array(filled) { GravityModel(0f) }
            
        points.forEachIndexed { index, bar -> bar.update(fft[index].toFloat() * ampR) }
        psf = interpolateFft(points, num, "sp")
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        if (skipFrame) return
        val width = canvas.width.toFloat()
        
        val steps = 150
        val sliceWidth = width / steps
        val extraSteps = 4 // Just enough to overflow cleanly into the rounded corners
        
        var first = true
        for (i in -extraSteps..(steps + extraSteps)) {
            val progress = i.toDouble() / steps
            // Map step to the spline range (0..num) and clamp to avoid OutOfRangeException
            val x = (progress * num).coerceIn(0.0, num.toDouble())
            
            // We use cosine to smoothly alternate the sign of the wave between positive and negative
            val alternatingSign = kotlin.math.cos(progress * num * kotlin.math.PI).toFloat()
            
            // The amplitude is defined completely by the music (psf.value(x))
            val value = psf.value(x).toFloat() * alternatingSign
            
            val pointX = sliceWidth * i
            
            if (first) {
                path.moveTo(pointX, value)
                first = false
            } else {
                path.lineTo(pointX, value)
            }
        }
        
        // drawHelper with yR = 0.5f centers the path vertically
        drawHelper(canvas, "a", 0f, 0.5f) { canvas.drawPath(path, paint) }
        path.reset()
    }
}
