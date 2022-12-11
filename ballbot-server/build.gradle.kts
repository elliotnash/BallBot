plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin")
}

application {
    mainClass.set("org.elliotnash.ballbot.server.MainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:${project.parent!!.extra["ktor.version"]}")
    implementation("io.ktor:ktor-server-websockets-jvm:${project.parent!!.extra["ktor.version"]}")
    implementation("io.ktor:ktor-server-netty-jvm:${project.parent!!.extra["ktor.version"]}")

    api(project(":ballbot-common"))

    testImplementation("io.ktor:ktor-server-tests-jvm:${project.parent!!.extra["ktor.version"]}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${project.parent!!.extra["kotlin.version"]}")
}