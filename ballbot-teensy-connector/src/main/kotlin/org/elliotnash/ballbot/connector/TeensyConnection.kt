package org.elliotnash.ballbot.connector

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.slf4j.event.Level
import java.io.ByteArrayInputStream

const val END = 0x00
const val READY = 0x01
const val DISCONNECT = 0x02
const val FUNCTION_HEADER = 0x03
const val RETURN_HEADER = 0x04

val LOGGER = KotlinLogging.logger("Arduino")

class TeensyConnection(
    commPortAddress: String
) {
    companion object {
        init {
            System.setProperty("fazecast.jSerialComm.appid", "org.elliotnash.ballbot.api")
        }
    }

    private val commPort: SerialPort
    init {
        commPort = SerialPort.getCommPort(commPortAddress)
        commPort.setComPortParameters(9600, 8, 1, 0)
        commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0)
    }

    fun start(scope: CoroutineScope) {
        if (commPort.openPort()) {
            println("Port successfully opened: "+commPort.isOpen)
        } else {
            throw RuntimeException("There is a big error. It is bad.")
        }

        commPort.outputStream.write(READY)

        val firstByte = commPort.inputStream.read()
        if (firstByte != READY) {
            // TODO restart teensy?
            throw RuntimeException("Invalid first byte $firstByte (maybe already started")
        } else {
            LOGGER.debug {"Teensy started"}
        }

        scope.launch {
            readLoop()
        }
    }

    private fun readLoop() {
        while (true) {
            val eventData = ByteArray(1)
            commPort.readBytes(eventData, 1)
            when (eventData.first().toInt()) {
                FUNCTION_HEADER -> {
                    val funcLen = commPort.inputStream.read()
                    val funcName = String(commPort.inputStream.readNBytes(funcLen))
                    val dataLen = commPort.inputStream.readU16LE().toInt()
                    val data = commPort.inputStream.readNBytes(dataLen)
                    commPort.inputStream.read()

                    // TODO add function registry
                    when (funcName) {
                        "log" -> log(data.inputStream())
                    }
                }
                RETURN_HEADER -> {

                }
                END -> {

                }
                else -> {
                    LOGGER.warn {"Received invalid serial event."}
                }
            }
        }
    }

    private fun log(data: ByteArrayInputStream) {
        val levelLen = data.read()
        val level = String(data.readNBytes(levelLen))
        val messageLen = data.readU16LE().toInt()
        val message = String(data.readNBytes(messageLen))
        LOGGER.atLevel(Level.valueOf(level)).log {message}
    }

}
