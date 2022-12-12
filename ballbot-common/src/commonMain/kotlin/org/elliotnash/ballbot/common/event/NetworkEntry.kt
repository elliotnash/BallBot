package org.elliotnash.ballbot.common.event

import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.reflect.KClass

@Serializable
abstract class NetworkEntry {
    init {
        this::class.register()
    }
    fun encode(): ByteArray {
        return NetworkEntryEncoder.encode(this)
    }
}

fun<T : NetworkEntry> KClass<T>.register() {
    NetworkEntryEncoder.register(this)
}

fun ByteArray.decodeNetworkEntry(): NetworkEntry {
    return NetworkEntryEncoder.decode(this)
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
object NetworkEntryEncoder {
    private var protobuf = ProtoBuf {}
    // Register your event serializables here!
    init {
        ClientConfiguration::class.register()
        EnableEntry::class.register()
        GamepadUpdate::class.register()
        GamepadRumble::class.register()
    }
    fun<T : NetworkEntry> register(clazz: KClass<T>) {
        protobuf = ProtoBuf(protobuf) {
            serializersModule = protobuf.serializersModule + SerializersModule {
                polymorphic(NetworkEntry::class) {
                    subclass(clazz, clazz.serializer())
                }
            }
        }
    }
    fun encode(networkEntry: NetworkEntry): ByteArray {
        return protobuf.encodeToByteArray(PolymorphicSerializer(NetworkEntry::class), networkEntry)
    }
    fun decode(data: ByteArray): NetworkEntry {
        // register(ClientConfiguration::class)
        return protobuf.decodeFromByteArray(PolymorphicSerializer(NetworkEntry::class), data)
    }
}
