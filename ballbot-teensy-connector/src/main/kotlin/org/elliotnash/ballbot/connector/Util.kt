package org.elliotnash.ballbot.connector

import java.io.ByteArrayInputStream
import java.io.InputStream

fun ByteArray.u16LE(): UShort {
    require(this.size == 2) { "length must be 2, got ${this.size}" }
    return this[0].toUShort() or (this[1].toUInt() shl 8).toUShort()
}

fun ByteArrayInputStream.readU16LE(): UShort {
    return this.readNBytes(2).u16LE()
}

fun InputStream.readU16LE(): UShort {
    return this.readNBytes(2).u16LE()
}
