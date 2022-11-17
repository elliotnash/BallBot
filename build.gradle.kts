plugins {
    kotlin("multiplatform").apply(false)
    id("org.jetbrains.compose").apply(false)
    kotlin("jvm").apply(false)
    id("io.ktor.plugin").apply(false)
}

repositories {
    mavenCentral()
}

allprojects {
    group = "org.elliotnash.ballbot"
    version = "0.0.1"
}
