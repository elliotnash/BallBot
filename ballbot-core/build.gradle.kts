plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) { browser { binaries.executable() } }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.4.1")
            }
        }
    }
}
