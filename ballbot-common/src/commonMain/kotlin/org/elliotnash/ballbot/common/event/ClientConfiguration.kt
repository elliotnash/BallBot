package org.elliotnash.ballbot.common.event

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class ClientConfiguration(
    val periodicInterval: Duration
) : NetworkEntry()
