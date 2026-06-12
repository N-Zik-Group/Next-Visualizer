package utils

import android.graphics.*
import painters.Painter
import painters.fft.FftBar
import painters.fft.FftCLine
import painters.fft.FftCWaveRgb
import painters.misc.Background
import painters.misc.Icon
import painters.modifier.Compose
import painters.modifier.Rotate
import painters.modifier.Scale
import painters.modifier.Shake

class Preset {
    companion object {

        /**
         * Feel free to add your awesome preset here ;)
         * Hint: You can use `Compose` painter to group multiple painters together as a single painter
         */
        fun getPreset(name: String): Painter {
            return when (name) {
                "debug" -> FftBar()
                else -> FftBar()
            }
        }

        fun getPresetWithBitmap(name: String, bitmap: Bitmap): Painter {
            return when (name) {
                "cIcon" -> Compose(Rotate(FftCLine()), Icon(Icon.getCircledBitmap(bitmap)))
                "cWaveRgbIcon" -> Compose(
                    Rotate(FftCWaveRgb()),
                    Icon(Icon.getCircledBitmap(bitmap))
                )
                "liveBg" -> Scale(Shake(Background(bitmap)), scaleX = 1.02f, scaleY = 1.02f)
                "debug" -> Icon(bitmap)
                else -> Icon(bitmap)
            }
        }
    }
}


