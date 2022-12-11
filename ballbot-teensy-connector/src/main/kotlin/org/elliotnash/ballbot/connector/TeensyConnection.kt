package org.elliotnash.ballbot.connector

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.ByteArrayInputStream
import java.util.HexFormat

const val END = 0x00
const val READY = 0x01
const val FUNCTION_HEADER = 0x02
const val RETURN_HEADER = 0x03

private val logger = KotlinLogging.logger {}

class TeensyConnection(
    commPortAddress: String
) {
    companion object {
        init {
            System.setProperty("fazecast.jSerialComm.appid", "org.elliotnash.ballbot.api")
        }
    }

    private val serial: SerialPort
    init {
        serial = SerialPort.getCommPort(commPortAddress)
        serial.setComPortParameters(9600, 8, 1, 0)
        serial.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0)
    }

    fun start(scope: CoroutineScope) {
        // register shutdown hook
        SerialPort.addShutdownHook(Thread { runBlocking { shutdown() } })

        if (serial.openPort()) {
            logger.debug {"Serial port successfully opened"}
        } else {
            throw SerialPortInvalidPortException("Could not open serial port at ${serial.portDescription}", null)
        }

        serial.outputStream.write(READY)

        val firstByte = serial.inputStream.read()
        if (firstByte != READY) {
            // TODO restart teensy?
            throw RuntimeException("Invalid first byte $firstByte (maybe already started")
        }

        logger.info {"Connected to Teensy"}
        active = true

        scope.launch {
            readLoop()
        }

        // set led on so we know we've connected
        setLed(true)
    }

    private var active = false
    private fun readLoop() {
        while (true) {
            val eventData = ByteArray(1)
            serial.readBytes(eventData, 1)
            when (eventData.first().toInt()) {
                FUNCTION_HEADER -> {
                    val funcLen = serial.inputStream.read()
                    val funcName = String(serial.inputStream.readNBytes(funcLen))
                    val dataLen = serial.inputStream.readUShortLE().toInt()
                    val data = serial.inputStream.readNBytes(dataLen)
                    serial.inputStream.read()

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
                    logger.warn {"Received invalid serial event."}
                }
            }
        }
    }

    fun setLed(on: Boolean) {
        call("set_led", byteArrayOf(on.toByte()))
    }

    private fun reset() {
        call("reset", byteArrayOf())
    }

    private fun call(function: String, data: ByteArray) {
        if (serial.isOpen) {
            serial.outputStream.write(FUNCTION_HEADER)
            serial.outputStream.write(function.length)
            serial.outputStream.write(function.encodeToByteArray())
            serial.outputStream.write(data.size.toUShort().toByteArrayLE())
            serial.outputStream.write(data)
            serial.outputStream.write(END)
        }
    }

    private val teensyLogger = LoggerFactory.getLogger("Teensy-Rust")
    private fun log(data: ByteArrayInputStream) {
        val levelLen = data.read()
        val level = String(data.readNBytes(levelLen))

        val fileLen = data.readUShortLE().toInt()
        val file = String(data.readNBytes(fileLen))

        val line = data.readUIntLE()

        val messageLen = data.readUShortLE().toInt()
        val message = String(data.readNBytes(messageLen))

        teensyLogger.atLevel(Level.valueOf(level)).log("($file:$line) - $message")
    }

    private suspend fun shutdown() {
        logger.debug {"shutting down teensy service"}
        reset()
        delay(100)
    }

}
