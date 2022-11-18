package org.elliotnash.ballbot.core.events

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class GamepadUpdate(
    val id: String,
    val axes: Array<Double>,
    val buttons: Array<ButtonUpdate>
) : Event {
    @Serializable
    data class ButtonUpdate (
        val pressed: Boolean,
        val touched: Boolean,
        val value: Double
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GamepadUpdate

        if (id != other.id) return false
        if (!axes.contentEquals(other.axes)) return false
        if (!buttons.contentEquals(other.buttons)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + axes.contentHashCode()
        result = 31 * result + buttons.contentHashCode()
        return result
    }
}

@Serializable
data class GamepadRumble(
    val startDelay: Duration,
    val duration: Duration,
    val weakMagnitude: Double,
    val strongMagnitude: Double
) : Event
