package org.elliotnash.ballbot.client

import androidx.compose.runtime.*
import androidx.compose.web.events.SyntheticMouseEvent
import kotlinx.coroutines.*
import org.elliotnash.ballbot.common.event.*
import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.svg.*
import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.WebSocket
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.time.Duration.Companion.milliseconds

// TODO fix this mess

val socket = WebSocket("ws://localhost:8080/networkentries").apply {
    onmessage = {
        //val event = EventEncoder.decode((it.data as Blob))
        val reader = FileReader()
        reader.readAsArrayBuffer(it.data as Blob)
        reader.onloadend = {
            val data = (reader.result as ArrayBuffer).asByteArray()
            handleEntry(data.decodeNetworkEntry())
        }
        Unit
    }
    onclose = {
        println("Websocket closed, cancelling event loop")
        eventLoopJob?.cancel()
        eventLoopJob = null
        Unit
    }
}

fun WebSocket.send(entry: NetworkEntry) {
    send(entry.encode().asInt8Array())
}

class ClientState {
    var enabled by mutableStateOf(false)
    var gamepad by mutableStateOf<GenericGamepad?>(null)
}

val state = ClientState()

fun handleEntry(entry: NetworkEntry) {
    if (entry is ClientConfiguration) {
        println("We got client config: $entry")
        eventLoopJob = setInterval(entry.periodicInterval, ::eventLoop)
    }
    if (entry is GamepadRumble) {
        state.gamepad?.vibrate(entry)
    }
    if (entry is EnableEntry) {
        state.enabled = entry.enabled
    }
}

var eventLoopJob: Job? = null
fun eventLoop() {
    GamepadManager.updateGamepads()
    state.gamepad = if (GamepadManager.gamepads.isNotEmpty()) {
        val gamepad = GamepadManager.gamepads[0]
        if (socket.readyState == 1.toShort()) {
            socket.send(gamepad.getUpdate())
        }
        gamepad
    } else {
        null
    }
}

fun main() {
    renderComposable(rootElementId = "root") {
        Div {
            if (state.gamepad != null) {
                Text("Gamepad connected: ${state.gamepad!!.id}")
                Br(); Br()
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                    }
                }) {
                    JoystickPreview(state.gamepad!!.axes[0], state.gamepad!!.axes[1])
                    Div(attrs = { style { width(10.px); display(DisplayStyle.InlineBlock) } })
                    JoystickPreview(state.gamepad!!.axes[2], state.gamepad!!.axes[3])
                }
                Br()
                CoolButton(
                    onClick = {
                        // GamepadManager.gamepads[0].vibrate(GamepadRumble(0.milliseconds, 200.milliseconds, 1.0, 1.0))
                        socket.send(EnableEntry(!state.enabled))
                    },
                    color = if (state.enabled) {Color("green")} else {Color("red")}
                ) {
                    Text(if (state.enabled) {"Disable"} else {"Enable"})
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
    color: CSSColorValue = Color("#888888"),
    content: (@Composable () -> Unit)? = null
) {
    Button(attrs = {
        onClick?.apply {
            this@Button.onClick(this)
        }
        style {
            border { style = LineStyle.None }
            padding(6.px)
            borderRadius(8.px)
            backgroundColor(color)
        }
    }) {
        content?.invoke()
    }
}
