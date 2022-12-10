package org.elliotnash.ballbot.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.elliotnash.ballbot.server.plugins.configureRouting
import kotlin.time.Duration

class Server(
    clientPeriodic: Duration
) {
    var isStarted = false
    val networkEntryServer = NetworkEntryServer(this, clientPeriodic)
    fun start() {
        var test = embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = {
            configureRouting()
            networkEntryServer.configure(this)
        }).start(wait = false)
        println("STARTED")
        isStarted = true
    }
}
