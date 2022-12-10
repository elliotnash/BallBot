package org.elliotnash.ballbot.api

import kotlinx.coroutines.*
import org.elliotnash.ballbot.connector.TeensyConnection
import org.elliotnash.ballbot.server.Server
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration

abstract class Robot(
    val serverPeriodic: Duration,
    clientPeriodic: Duration = serverPeriodic,
    arduinoPort: String,
) {
    internal var server: Server = Server(clientPeriodic)
    internal var teensy: TeensyConnection = TeensyConnection(arduinoPort)
    fun start() {
        runBlocking {
            teensy.start(this)
            server.start()

            // robot initialized
            init()
            fixedRateTimer(period = serverPeriodic.inWholeMilliseconds) {
                // Event loop
                periodic()
            }
        }
    }
    abstract fun init()
    abstract fun enabled()
    abstract fun disabled()
    abstract fun periodic()
}
