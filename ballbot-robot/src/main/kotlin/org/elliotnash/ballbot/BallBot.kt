package org.elliotnash.ballbot

import mu.KotlinLogging
import org.elliotnash.ballbot.api.Robot
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

class BallBot : Robot(
    serverPeriodic = 500.milliseconds,
    teensyPort = "/dev/cu.usbmodem79891201"
) {
    override fun init() {
        logger.info {"Robot initialized"}
    }

    override fun enabled() {
    }

    override fun disabled() {
    }

    override fun shutdown() {
    }

    override fun periodic() {
    }
}

fun main(args: Array<String>) {
    logger.info {"starting ballbot"}
    BallBot().start()
}
