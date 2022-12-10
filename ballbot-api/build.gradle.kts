plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    implementation(project(":ballbot-server"))
    implementation(project(":ballbot-teensy-connector"))
}
