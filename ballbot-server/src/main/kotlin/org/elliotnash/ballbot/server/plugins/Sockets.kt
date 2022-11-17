package org.elliotnash.ballbot.server.plugins

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.util.date.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/ws") { // websocketSession
            var num = 0
            var firstTime = getTimeMillis()
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    if (num == 0) {
                        firstTime = getTimeMillis()
                    } else if (num == 250) {
                        println("Should be one second: ${getTimeMillis()-firstTime}")
                        num = -1
                    }
                    num++
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }
}
