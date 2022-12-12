package org.elliotnash.ballbot.common

// TODO create jvm common module

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
