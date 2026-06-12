# NextVisualizer

NextVisualizer is an audio visualizer library extracted from the N-Zik project. It provides various visual effects and painters (FFT bars, waves, particles, etc.) that can be rendered dynamically on a canvas to visualize audio sessions.

> **Note:** This project is a fork of the Next Visualizer implementation originally created by **[jeffshee](https://github.com/jeffshee/NextGenVisualizer)**. 


## Features
- **FFT Painters**: Analog, Bar, Circle Bar, Line, Polygon, Wave, etc.
- **Modifiers**: Beat, Blend, Compose, Glitch, Move, Rotate, Scale, Shake, Zoom.
- **Miscellaneous**: Background, Gradient, SimpleText, Icon rendering.

## Integration
This library is designed to be added as a submodule in Android projects.

```groovy
// settings.gradle.kts
include(":nextvisualizer")

// build.gradle.kts (app)
implementation(projects.nextvisualizer)
```

## Structure
The source files are placed directly under `src/main/kotlin/` grouped by their feature (`painters`, `utils`, `enums`, `views`).

## Requirements
- Android SDK 35+
- Apache Commons Math3
- Timber (for logging)
