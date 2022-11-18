package org.elliotnash.ballbot.server.plugins

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import org.elliotnash.ballbot.core.events.EventEncoder
import org.elliotnash.ballbot.core.events.GamepadRumble
import org.elliotnash.ballbot.core.events.GamepadUpdate
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/ws") { // websocketSession
            var lastGamepadUpdate: GamepadUpdate? = null
            for (frame in incoming) {
                if (frame is Frame.Binary) {
                    val data = frame.readBytes()
                    val event = EventEncoder.decode(data)
                    if (event is GamepadUpdate) {
                        if (event.buttons[0].pressed && lastGamepadUpdate?.buttons?.get(0)?.pressed == false) {
                            println("BUTTON PRESSED")
                            send(EventEncoder.encode(GamepadRumble(0.milliseconds, 200.milliseconds, 1.0, 1.0)))
                        }
                        lastGamepadUpdate = event
                    }
                }
            }
        }
    }
}
