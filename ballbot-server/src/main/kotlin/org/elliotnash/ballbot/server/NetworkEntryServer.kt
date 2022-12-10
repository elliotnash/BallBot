package org.elliotnash.ballbot.server

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.elliotnash.ballbot.common.event.*
import java.time.Duration
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

class NetworkEntryServer(
    private val server: Server,
    private val clientPeriodic: kotlin.time.Duration
) {
    class Connection(val session: DefaultWebSocketSession) {
        companion object {
            val lastId = AtomicInteger(0)
        }
        val id = lastId.getAndIncrement()
    }
    private val connections = Collections.synchronizedCollection<Connection>(LinkedHashSet())
    fun configure(application: Application) {
        application.apply {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                webSocket("/networkentries") {
                    // add the connection to the list
                    val connection = Connection(this)
                    connections += connection

                    try {
                        // send configuration to client
                        send(ClientConfiguration(clientPeriodic).encode())

                        var lastGamepadUpdate: GamepadUpdate? = null
                        for (frame in incoming) {
                            if (frame is Frame.Binary) {
                                try {
                                    val event = frame.readBytes().decodeNetworkEntry()
                                    if (event is GamepadUpdate) {
                                        if (event.buttons[0].pressed && lastGamepadUpdate?.buttons?.get(0)?.pressed == false) {
                                            println("BUTTON PRESSED")
                                            send(GamepadRumble(0.milliseconds, 200.milliseconds, 1.0, 1.0).encode())
                                        }
                                        lastGamepadUpdate = event
                                    }
                                } catch (e: Exception) {
                                    println(e.localizedMessage)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println(e.localizedMessage)
                    } finally {
                        connections -= connection
                    }
                }
            }
        }
    }
}
