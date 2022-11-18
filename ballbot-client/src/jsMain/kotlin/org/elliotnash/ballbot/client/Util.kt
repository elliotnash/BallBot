package org.elliotnash.ballbot.client

import kotlinx.browser.window
import kotlinx.coroutines.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(DelicateCoroutinesApi::class)
fun setInterval(time: Duration, handler: () -> Unit) = GlobalScope.launch {
    var nextTime = window.performance.now().milliseconds
    while (isActive) {
        nextTime += time
        val timeUntilNext = nextTime - window.performance.now().milliseconds
        delay(timeUntilNext)
        CoroutineScope(Dispatchers.Main).launch {
            handler()
        }
    }
}

fun ArrayBuffer.asByteArray(byteOffset: Int = 0, length: Int = this.byteLength): ByteArray =
    Int8Array(this, byteOffset, length).unsafeCast<ByteArray>()

fun ByteArray.asInt8Array(): Int8Array = this.unsafeCast<Int8Array>()
