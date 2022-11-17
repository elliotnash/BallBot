package org.elliotnash.ballbot.client

import androidx.compose.runtime.*
import androidx.compose.web.events.SyntheticMouseEvent
import kotlinx.coroutines.*
import org.elliotnash.ballbot.core.printHello
import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.svg.*
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import kotlin.time.Duration.Companion.milliseconds

val LOOP_INTERVAL = 4.milliseconds

var gamepad by mutableStateOf<GenericGamepad?>(null)

val socket = WebSocket("ws://localhost:8080/ws")

fun gamepadLoop() {
    GamepadManager.updateGamepads()
    gamepad = if (GamepadManager.gamepads.isNotEmpty()) {
        val gamepad = GamepadManager.gamepads[0]
        gamepad
    } else {
        null
    }
    if (socket.readyState == 1.toShort()) {
        socket.send("HI")
    }
}

fun main() {
    printHello()
    setInterval(LOOP_INTERVAL, ::gamepadLoop)
    renderComposable(rootElementId = "root") {
        Div {
            if (gamepad != null) {
                Text("Gamepad connected: ${gamepad!!.id}")
                Br(); Br()
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                    }
                }) {
                    JoystickPreview(gamepad!!.axes[0], gamepad!!.axes[1])
                    Div(attrs = { style { width(10.px); display(DisplayStyle.InlineBlock) } })
                    JoystickPreview(gamepad!!.axes[2], gamepad!!.axes[3])
                }
                Br()
                CoolButton( onClick = {
                    GamepadManager.gamepads[0].vibrate()
                }) {
                    Text("rumble")
                }
            } else {
                Text("No gamepads connected! Please connect one and press any joystick button.")
            }
        }
    }
}

@OptIn(ExperimentalComposeWebSvgApi::class)
@Composable
fun JoystickPreview(
    xAxis: Double,
    yAxis: Double
) {
    Div(attrs = {
        style {
            width(200.px)
            height(200.px)
            display(DisplayStyle.InlineBlock)
        }
    }) {
        Svg(attrs = {
            viewBox("0 0 100 100")
        }) {
            val cursorSize = 3.px
            Circle(
                50+((50-cursorSize.value)*xAxis),
                50+((50-cursorSize.value)*yAxis),
                cursorSize.value, {
                    fill("#000000")
                }
            )
            Circle(50, 50, 50-cursorSize.value, {
                fill("none")
                attr("stroke", "#05336140")
                attr("stroke-width", "1px")
            })
            Line(cursorSize.value, 50, 100-cursorSize.value, 50, {
                fill("none")
                attr("stroke", "#05336140")
                attr("stroke-width", "1px")
            })
            Line(50, cursorSize.value, 50, 100-cursorSize.value, {
                fill("none")
                attr("stroke", "#05336140")
                attr("stroke-width", "1px")
            })
        }
    }
}

@Composable
fun CoolButton(
    onClick: ((SyntheticMouseEvent) -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Button(attrs = {
        onClick?.apply {
            this@Button.onClick(this)
        }
        style {
            border { style = LineStyle.None }
        }
    }) {
        content?.invoke()
    }
}
