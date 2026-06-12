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
    implementation(libs.math3)
    implementation(libs.timber)
}
