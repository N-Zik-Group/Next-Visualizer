package nextvisualizer.enums

import app.n_zik.android.R

enum class VisualizerWhiteColorOption(val text: Int) {
    White(R.string.color_white),
    Theme(R.string.bg_colors_background_from_theme),
    Cover(R.string.bg_colors_background_from_cover),
    Custom(R.string.color_custom)
}
