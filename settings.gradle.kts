
rootProject.name = "ballbot"

include(
    "ballbot-core",
    "ballbot-client",
    "ballbot-server"
)

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
        kotlin("jvm").version(extra["kotlin.version"] as String)
        id("io.ktor.plugin").version(extra["ktor.version"] as String)
        kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
    }
}
