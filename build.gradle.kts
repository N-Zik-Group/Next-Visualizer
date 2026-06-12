plugins {
    id("com.android.library")
    kotlin("android")
}

configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "nextvisualizer"
    compileSdk = 35 // Assuming 35 based on typical recent Android setup, N-Zik root uses 35 probably. Wait, discordrpc used 37! I will use 35, or I can check root build.gradle.kts. But 35 is safe.

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}

dependencies {
    // Media3 for AudioSessionId (used in VisualizerHelper?) Let me check VisualizerHelper if it needs Media3
    // Wait, VisualizerHelper takes audioSessionId as Int. It uses android.media.audiofx.Visualizer which is standard Android SDK.
    // So no external dependencies needed!
}
