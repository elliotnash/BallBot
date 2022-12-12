package org.elliotnash.ballbot.common.event

import kotlinx.serialization.Serializable

@Serializable
data class EnableEntry(
    val enabled: Boolean
) : NetworkEntry()
