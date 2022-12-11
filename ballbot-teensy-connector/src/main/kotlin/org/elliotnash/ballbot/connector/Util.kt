package org.elliotnash.ballbot.connector

import java.io.ByteArrayInputStream
import java.io.InputStream

fun ByteArray.toUShortLE(): UShort {
    require(this.size == 2) { "length must be 2, got ${this.size}" }
    return this[0].toUShort() or (this[1].toUInt() shl 8).toUShort()
}

fun ByteArrayInputStream.readUShortLE(): UShort {
    return this.readNBytes(2).toUShortLE()
}

fun InputStream.readUShortLE(): UShort {
    return this.readNBytes(2).toUShortLE()
}

fun ByteArray.toUIntLE(): UInt {
    require(this.size == 4) { "length must be 2, got ${this.size}" }
    return this[0].toUInt() or (this[1].toUInt() shl 8) or (this[2].toUInt() shl 16) or (this[3].toUInt() shl 24)
}

fun ByteArrayInputStream.readUIntLE(): UInt {
    return this.readNBytes(4).toUIntLE()
}

fun InputStream.readUIntLE(): UInt {
    return this.readNBytes(4).toUIntLE()
}

fun UShort.toByteArrayLE(): ByteArray {
    return byteArrayOf((this.toInt() and 0x00FF).toByte(), ((this.toInt() and 0xFF00) shr 8).toByte())
}

fun Boolean.toByte(): Byte {
    return if (this) {0x01} else {0x00}
}
