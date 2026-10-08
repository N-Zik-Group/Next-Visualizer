<div align="center">
  <h1>NextVisualizer</h1>
  <p>
    NextVisualizer is an audio visualizer library extracted from the
    <a href="https://github.com/N-Zik-Group/N-Zik">N-Zik</a> project. It provides various
    visual effects and painters (FFT bars, waves, particles, etc.) that can be rendered
    dynamically on a canvas to visualize audio sessions.
  </p>
  <p>
    <strong>Note:</strong> This project is a fork of the Next Visualizer implementation
    originally created by <a href="https://github.com/jeffshee/NextGenVisualizer">jeffshee</a>.
  </p>
</div>

<br>

<div align="center">
  
  [![License: GPL v3](https://img.shields.io/github/license/N-Zik-Group/Next-Visualizer?color=blue)](https://www.gnu.org/licenses/gpl-3.0)
  [![CodeFactor](https://www.codefactor.io/repository/github/n-zik-group/next-visualizer/badge)](https://www.codefactor.io/repository/github/n-zik-group/next-visualizer)
  
</div>

<div align="center">

## 📚 Wiki

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/N-Zik-Group/Next-Visualizer)

<br>

## 🌍 Community

Join the N-Zik Discord:

<a href="https://discord.gg/bneHC7QRje">
  <img src="https://discord.com/api/guilds/1345079801324634193/widget.png?style=banner2" alt="Discord Server">
</a>

<br>

</div>

# 🎧 Features

- **FFT Painters**: Analog, Bar, Circle Bar, Line, Polygon, Wave, etc.
- **Modifiers**: Beat, Blend, Compose, Glitch, Move, Rotate, Scale, Shake, Zoom.
- **Miscellaneous**: Background, Gradient, SimpleText, Icon rendering.

# 📜 Integration

This library is designed to be added as a submodule in Android projects.

```groovy
// settings.gradle.kts
include(":nextvisualizer")

// build.gradle.kts (app)
implementation(projects.nextvisualizer)
```

# 📁 Structure

The source files are placed directly under `src/main/kotlin/` grouped by their feature
(`painters`, `utils`, `enums`, `views`).

# ⚙️ Requirements

- Android SDK 35+
- Apache Commons Math3
- Timber (for logging)

# 🤝 Contributing

## 🛠️ Improve the Module

Pull requests are welcome!
Feel free to fix bugs, enhance features, or suggest new ideas.

# 📦 Clone the repo

Use this command to clone the repo

```
git clone https://github.com/N-Zik-Group/Next-Visualizer.git
```

# 🫂 Acknowledgements

### 🛠 Based on / Inspired by:

- [**NextGenVisualizer**](https://github.com/jeffshee/NextGenVisualizer): The Next Visualizer implementation this project is based on.

Made with ❤️ by [NEVARLeVrai](https://github.com/NEVARLeVrai)
Licensed under GPLv3 - see [LICENSE](LICENSE)
