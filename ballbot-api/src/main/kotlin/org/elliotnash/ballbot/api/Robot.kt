package org.elliotnash.ballbot.api

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.elliotnash.ballbot.connector.TeensyConnection
import org.elliotnash.ballbot.server.Server
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

abstract class Robot(
    val serverPeriodic: Duration,
    clientPeriodic: Duration = serverPeriodic,
    teensyPort: String,
) {
    internal var server: Server = Server(clientPeriodic)
    internal var teensy: TeensyConnection = TeensyConnection(teensyPort)

    private var lastEnabled = false
    val isEnabled
        get() = server.state.enabled
    fun start() {
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info {"Stopping robot services"}
            shutdown()
        })
        runBlocking {
            teensy.start(this)
            server.start()

            // Robot initialized.
            logger.info {"Robot services initialized"}

            // Create led timer.
            var ledTimer: Timer? = null

            init()
            fixedRateTimer(period = serverPeriodic.inWholeMilliseconds) {
                if (isEnabled != lastEnabled) {
                    if (isEnabled) {
                        logger.info {"Robot enabling"}
                        enabled()
                        ledTimer = fixedRateTimer(period = 500) {
                            teensy.toggleLed()
                        }
                    } else {
                        logger.info {"Robot disabling"}
                        try {
                            disabled()
                        } finally {
                            ledTimer?.cancel()
                            teensy.setLed(true)
                        }
                    }
                    lastEnabled = isEnabled
                }
                // Event loop
                if (isEnabled) {
                    enabledPeriodic()
                } else {
                    disabledPeriodic()
                }
            }
        }
    }
    open fun init() {}
    open fun enabled() {}
    open fun disabled() {}
    open fun shutdown() {}
    open fun disabledPeriodic() {}
    open fun enabledPeriodic() {}
}
