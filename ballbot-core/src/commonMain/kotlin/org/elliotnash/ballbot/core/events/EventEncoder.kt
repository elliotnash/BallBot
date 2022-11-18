package org.elliotnash.ballbot.core.events

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
sealed interface Event

@OptIn(ExperimentalSerializationApi::class)
object EventEncoder {
    // Register your event serializables here!
    private val protobuf = ProtoBuf {
        serializersModule = SerializersModule {
            polymorphic(Event::class) {
                subclass(GamepadUpdate::class)
                subclass(GamepadRumble::class)
            }
        }
    }
    fun encode(event: Event): ByteArray {
        return protobuf.encodeToByteArray(event)
    }
    fun decode(data: ByteArray): Event {
        return protobuf.decodeFromByteArray(data)
    }
}
