package org.elliotnash.ballbot.client

import kotlinx.browser.window
import kotlinx.coroutines.*
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
