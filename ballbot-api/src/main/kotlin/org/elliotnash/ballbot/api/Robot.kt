package org.elliotnash.ballbot.api

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.elliotnash.ballbot.connector.TeensyConnection
import org.elliotnash.ballbot.server.Server
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

abstract class Robot(
    val serverPeriodic: Duration,
    clientPeriodic: Duration = serverPeriodic,
    teensyPort: String,
) {
    internal var server: Server = Server(clientPeriodic)
    internal var teensy: TeensyConnection = TeensyConnection(teensyPort)
    fun start() {
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info {"Stopping robot services"}
            shutdown()
        })
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
    abstract fun shutdown()
    abstract fun periodic()
}
