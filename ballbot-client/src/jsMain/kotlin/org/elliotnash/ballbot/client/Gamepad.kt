package org.elliotnash.ballbot.client

import kotlinx.browser.window
import org.w3c.dom.events.Event

abstract external class Gamepad {
    val id: String
    val index: Int
    val connected: Boolean
    val timestamp: Double
    val mapping: String
    val axes: Array<Double>
    val buttons: Array<GamepadButton>
    // only on firefox
    val hapticActuators: Array<GamepadHapticActuator>
    // only on chromium
    val vibrationActuator: GamepadHapticActuator
}

abstract external class GamepadButton {
    val pressed: Boolean
    val touched: Boolean
    val value: Double
}

abstract external class GamepadHapticActuator {
    //TODO enums
    val type: String
    // only firefox
    fun pulse(intensity: Double, duration: Int)
    // only chromium
    fun playEffect(type: String, params: dynamic)
    fun reset()
}

private enum class GamepadVibrationType {
    FIREFOX,
    CHROMIUM,
    UNSUPPORTED
}

fun GamepadVibrationParams(
    startDelay: Int,
    duration: Int,
    weakMagnitude: Double,
    strongMagnitude: Double
): dynamic {
    return js("({startDelay: startDelay, duration: duration, weakMagnitude: weakMagnitude, strongMagnitude: strongMagnitude})")
}

class GenericGamepad(private val gamepad: Gamepad) {
    val id get() = gamepad.id
    val index get() = gamepad.index
    val connected get() = gamepad.connected
    val timestamp get() = gamepad.timestamp
    val mapping get() = gamepad.mapping
    val axes get() = gamepad.axes
    val buttons get() = gamepad.buttons
    fun vibrate() {
        println("VIBRATE CALLED")
        println(gamepadType)
        if (gamepadType == GamepadVibrationType.FIREFOX) {
            gamepad.hapticActuators[0].pulse(1.0, 200)
        } else if (gamepadType == GamepadVibrationType.CHROMIUM) {
            gamepad.vibrationActuator.playEffect(
                gamepad.vibrationActuator.type,
                GamepadVibrationParams(0, 200, 1.0, 1.0)
            )
        }
    }
    private val gamepadType = run {
        fun jsGetPropertyNames(o: Any): Array<String> {
            return js("Object.keys(Object.getPrototypeOf(o))") as Array<String>
        }
        val properties = jsGetPropertyNames(gamepad)
        if (properties.contains("hapticActuators")) {
            return@run GamepadVibrationType.FIREFOX
        } else if (properties.contains("vibrationActuator")) {
            return@run GamepadVibrationType.CHROMIUM
        } else {
            return@run GamepadVibrationType.UNSUPPORTED
        }
    }
}

object GamepadManager {
    var gamepads = jsGetGamepads()
    private fun jsGetGamepads(): List<GenericGamepad> {
        val gamepads = js("navigator.getGamepads()") as Array<Gamepad?>
        return gamepads.filterNotNull().map {
            GenericGamepad(it)
        }
    }
    init {
        window.addEventListener(type = "gamepadconnected", callback = { event ->
            updateGamepads()
            connectListeners.forEach { it(event) }
        })
        window.addEventListener(type = "gamepaddisconnected", callback = { event ->
            updateGamepads()
            disconnectListeners.forEach { it(event) }
        })
    }

    private val connectListeners = mutableListOf<(Event) -> Unit>()
    fun addConnectListener(listener: (Event) -> Unit) {
        connectListeners.add(listener)
    }
    fun removeConnectListener(listener: (Event) -> Unit) {
        connectListeners.remove(listener)
    }

    private val disconnectListeners = mutableListOf<(Event) -> Unit>()
    fun addDisconnectListener(listener: (Event) -> Unit) {
        disconnectListeners.add(listener)
    }
    fun removeDisconnectListener(listener: (Event) -> Unit) {
        disconnectListeners.remove(listener)
    }

    fun updateGamepads() {
        gamepads = jsGetGamepads()
    }
}
