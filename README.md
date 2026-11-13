<div align="center">
  <img alt="project's banner" src="https://raw.githubusercontent.com/N-Zik-Group/N-Zik/main/assets/design/ic_banner2.png" width="1080" />

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
  
  [![License: Apache 2.0](https://img.shields.io/github/license/N-Zik-Group/Next-Visualizer?style=flat&color=blue)](https://www.apache.org/licenses/LICENSE-2.0)
  [![CodeFactor](https://img.shields.io/codefactor/grade/github/N-Zik-Group/Next-Visualizer?style=flat&label=CodeFactor)](https://www.codefactor.io/repository/github/n-zik-group/next-visualizer)
  
</div>

<div align="center">

## 📚 Wiki

[![Ask DeepWiki](https://img.shields.io/badge/Ask-DeepWiki-8d5cff?style=flat)](https://deepwiki.com/N-Zik-Group/Next-Visualizer)

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

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, verification and pull requests.

# 👀 Status

[![Add to N-Zik RoadMap](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/mobile-roadmap.yml/badge.svg)](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/mobile-roadmap.yml)

[N-Zik mobile roadmap](https://github.com/orgs/N-Zik-Group/projects/1)

[![Refresh contributor grids](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/readme-contributors.yml/badge.svg)](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/readme-contributors.yml)

[![CodeQL](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/codeql.yml/badge.svg)](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/codeql.yml)  
[![Dependabot Updates](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/dependabot/dependabot-updates)  
[![Automatic Dependency Submission](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/dependency-graph/auto-submission/badge.svg)](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/dependency-graph/auto-submission)  
[![CodeQL](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/N-Zik-Group/Next-Visualizer/actions/workflows/github-code-scanning/codeql)

# 👥 Contributors

<!-- project-contributors:start -->
<p align="center">
<a href="https://github.com/NEVARLeVrai"><img src="https://avatars.githubusercontent.com/u/63956787?v=4" width="54" height="54" alt="NEVARLeVrai" title="NEVARLeVrai" /></a>
</p>
<!-- project-contributors:end -->

# 🎖️ Credits

See the [N-Zik credits](https://github.com/N-Zik-Group/N-Zik/blob/main/ComposeN-Zik/src/androidMain/res/raw/credits.md#android-extension-modules).

Made with ❤️ by the N-Zik Group community · Licensed under Apache 2.0 - see [LICENSE](LICENSE)
