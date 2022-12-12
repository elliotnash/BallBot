package org.elliotnash.ballbot.server

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import mu.KotlinLogging
import org.elliotnash.ballbot.common.event.*
import java.time.Duration
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

//TODO move this
suspend fun WebSocketSession.send(entry: NetworkEntry) {
    send(entry.encode())
}

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
    private var activeConnection: Connection? = null
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

                        for (frame in incoming) {
                            if (frame is Frame.Binary) {
                                try {
                                    val event = frame.readBytes().decodeNetworkEntry()
                                    if (event is EnableEntry) {
                                        if (event.enabled) {
                                            if (!server.state.enabled) {
                                                logger.debug {"Received enable event"}
                                                activeConnection = connection
                                                server.state.enabled = true
                                                send(EnableEntry(true))
                                            } else {
                                                // TODO send feedback that we're already enabled
                                                logger.info {"Received enable event, but already enabled!"}
                                            }
                                        } else {
                                            if (connection == activeConnection) {
                                                logger.debug {"Received disable event."}
                                                activeConnection = null
                                                server.state.enabled = false
                                                send(EnableEntry(false))
                                            } else {
                                                // TODO send feedback that only the active connection can disable
                                                logger.debug {"Received disable event, but client is not active"}
                                            }
                                        }
                                    }
                                    if (connection == activeConnection) {
                                        if (event is GamepadUpdate) {
                                            logger.trace {"Received gamepad update event: $event"}
//                                            if (event.buttons[0].pressed && server.state.gamepad?.buttons?.get(0)?.pressed == false) {
//                                                println("BUTTON PRESSED")
//                                                send(GamepadRumble(0.milliseconds, 200.milliseconds, 1.0, 1.0))
//                                            }
                                            server.state.gamepad = event
                                        }
                                    }
                                } catch (e: Exception) {
                                    logger.error {e}
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error {e}
                    } finally {
                        // disable robot if active connection disconnects
                        if (connection == activeConnection) {
                            server.state.enabled = false
                        }
                        connections -= connection
                    }
                }
            }
        }
    }
}
