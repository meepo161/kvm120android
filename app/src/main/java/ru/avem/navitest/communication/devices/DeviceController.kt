package ru.avem.navitest.communication.devices

interface DeviceController {
    fun read(vararg args: Any?)
    fun write(vararg args: Any?)

    fun needToRead(): Boolean
    fun setNeedToRead(needToRead: Boolean)
    fun resetAttempts()
    fun thereAreAttempts(): Boolean
    val isResponding: Boolean
    val isSynchronizedDevice: Boolean
    val isBusy: Boolean
    val isLocked: Boolean

    companion object {
        const val INPUT_BUFFER_SIZE = 256
        const val NUMBER_OF_ATTEMPTS = 5
        const val NET_NAME = "CP2103 USB to RS-485"
        const val BAUDRATE = 38400
        const val DEVICE_ID: Byte = 1
        const val NET_WRITE_TIMEOUT = 100
        const val NET_READ_TIMEOUT = 100
    }
}
