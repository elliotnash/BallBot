package org.elliotnash.ballbot.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.elliotnash.ballbot.common.event.GamepadUpdate
import org.elliotnash.ballbot.server.plugins.configureRouting
import kotlin.time.Duration

class Server(
    clientPeriodic: Duration
) {
    var isStarted = false
        internal set
    val networkEntryServer = NetworkEntryServer(this, clientPeriodic)
    val state = ServerState()
    fun start() {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = {
            configureRouting()
            networkEntryServer.configure(this)
        }).start(wait = false)
        isStarted = true
    }
}

class ServerState {
    var enabled = false
        internal set
    var gamepad: GamepadUpdate? = null
        internal set
}
