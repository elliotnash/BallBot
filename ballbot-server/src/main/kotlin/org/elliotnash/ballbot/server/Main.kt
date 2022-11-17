package org.elliotnash.ballbot.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.elliotnash.ballbot.core.printHello
import org.elliotnash.ballbot.server.plugins.configureRouting
import org.elliotnash.ballbot.server.plugins.configureSockets

fun main() {
    printHello()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureRouting()
}
