package org.elliotnash.ballbot

import org.elliotnash.ballbot.api.Robot
import kotlin.time.Duration.Companion.milliseconds

class Ballbot : Robot(
    serverPeriodic = 4.milliseconds,
    arduinoPort = "/dev/cu.usbmodem79891201"
) {
    override fun init() {
        println("Robot initialized")
    }

    override fun enabled() {
    }

    override fun disabled() {
    }

    override fun periodic() {
//        println("Periodic called")
    }
}

fun main(args: Array<String>) {
    Ballbot().start()
}
