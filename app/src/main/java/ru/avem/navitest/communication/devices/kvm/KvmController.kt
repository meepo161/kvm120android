package ru.avem.navitest.communication.devices.kvm

import ru.avem.navitest.communication.devices.DeviceController.Companion.DEVICE_ID
import ru.avem.navitest.communication.protocol.modbus.ModbusController
import ru.avem.navitest.communication.protocol.modbus.ModbusController.RequestStatus
import java.nio.ByteBuffer

class KvmController(
    private val modbusController: ModbusController
) {
    enum class Action {
        IS_RESPONDING,
        ALL,
        VOLTAGE_AMP,
        VOLTAGE_AVR,
        VOLTAGE_RMS,
        FREQUENCY,
        RAZMAH,
        CHTO_ESHE,
        COEFFICENT_AMP;
    }

    companion object {
        private const val U_REGISTER: Short = 0x1010
        private const val TIME_MEASURE: Short = 0x10C8
        private const val SERIAL_NUMBER: Short = 0x1108
    }

    private var isEntered: Boolean = false  //TODO DETACHED
    private val modbusAddress = DEVICE_ID
    private val INPUT_BUFFER_SIZE = 255

    fun read(): Values {
        val inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE)
        val status = modbusController.readInputRegisters(
            modbusAddress, U_REGISTER, 16, inputBuffer
        )
        return if (status == RequestStatus.FRAME_RECEIVED) {
            Values(
                true,
                voltageAmp = inputBuffer.float,
                voltageAvr = inputBuffer.float,
                voltageRms = inputBuffer.float,
                frequency = inputBuffer.float,
                razmah = inputBuffer.float,
                chtoEshe = inputBuffer.float,
                coefficentAmp = inputBuffer.float,
                coefficentForm = inputBuffer.float
            )
        } else {
            Values(false)
        }
    }

    fun readTimeAveraging(): Float {
        val inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE)
        val status = modbusController.readInputRegisters(
            modbusAddress, TIME_MEASURE, 2, inputBuffer
        )
        return if (status == RequestStatus.FRAME_RECEIVED) {
            inputBuffer.float
        } else {
            Float.NaN
        }
    }

    fun readSerialNumber(): Pair<Short, Short>? {
        val inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE)
        val status = modbusController.readInputRegisters(
            modbusAddress, SERIAL_NUMBER, 2, inputBuffer
        )
        return (if (status == RequestStatus.FRAME_RECEIVED) {
            Pair(inputBuffer.short, inputBuffer.short)
        } else {
            null
        })
    }

    fun write(vararg args: Any): Boolean {
        val inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE)
        var value = ByteArray(1)
        if (args[1] is Int) {
            value = intToByteArray(args[1] as Int)
        } else if (args[1] is Float) {
            value = floatToByteArray(args[1] as Float)
        }
        val status = modbusController.writeSingleHoldingRegister(
            modbusAddress,
            args[0] as Short, value, inputBuffer
        )
        if (status == RequestStatus.FRAME_RECEIVED) {
            return true
        } else {
            write(*args)
        }
        return false
    }

    fun reportSlaveID(): Boolean {
        var deviceID = 0.toByte()
        var versionSoftware = 0.toByte()
        var versionHardware = 0.toByte()
        var serialNumber = 0
        val inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE)
        var status = modbusController.reportSlaveID(
            modbusAddress,
            deviceID, versionSoftware, versionHardware, serialNumber, inputBuffer
        )
        return if (status == RequestStatus.FRAME_RECEIVED) {
            inputBuffer.position(2)
            deviceID = inputBuffer.get()
            versionSoftware = inputBuffer.get()
            versionHardware = inputBuffer.get()
            serialNumber = inputBuffer.int
            status = modbusController.reportSlaveID(
                modbusAddress,
                deviceID, versionSoftware, versionHardware, serialNumber, inputBuffer
            )
            status == RequestStatus.FRAME_RECEIVED
        } else {
            false
        }
    }

    private fun intToByteArray(i: Int): ByteArray {
        val convertBuffer = ByteBuffer.allocate(4)
        convertBuffer.clear()
        return convertBuffer.putInt(i).array()
    }

    private fun floatToByteArray(f: Float): ByteArray {
        val convertBuffer = ByteBuffer.allocate(4)
        convertBuffer.clear()
        return convertBuffer.putFloat(f).array()
    }

    private fun shortToByteArray(s: Short): ByteArray {
        val convertBuffer = ByteBuffer.allocate(2)
        convertBuffer.clear()
        return convertBuffer.putShort(s).array()
    }

    private fun floatToShorts(f: Float): Pair<Short, Short> {
        val convertBuffer = ByteBuffer.allocate(4).putFloat(f).flip() as ByteBuffer
        return Pair(convertBuffer.short, convertBuffer.short)
    }

    fun setTimeAveraging(timeAveraging: Float) {
        if (!isEntered) {
            isEntered = entryConfigurationMod()
        }
        if (isEntered) {
            val floatToShorts = floatToShorts(timeAveraging)
            modbusController.writeSingleHoldingRegister(
                modbusAddress,
                0x10C9,
                shortToByteArray(floatToShorts.first),
                ByteBuffer.allocate(INPUT_BUFFER_SIZE)
            )
            modbusController.writeSingleHoldingRegister(
                modbusAddress,
                0x10C8,
                shortToByteArray(floatToShorts.second),
                ByteBuffer.allocate(INPUT_BUFFER_SIZE)
            )
        }
    }

    private fun entryConfigurationMod(): Boolean {
        val inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE)
        readSerialNumber()?.let {
            modbusController.writeSingleHoldingRegister(
                modbusAddress,
                SERIAL_NUMBER,
                shortToByteArray(it.first),
                inputBuffer
            )
            modbusController.writeSingleHoldingRegister(
                modbusAddress,
                (SERIAL_NUMBER + 1).toShort(),
                shortToByteArray(it.second),
                inputBuffer
            )
            return true
        }
        return false
    }
}
