package ru.avem.navitest.communication.devices

import android.content.Context
import com.hoho.android.usbserial.driver.UsbSerialPort
import ru.avem.navitest.App.Companion.isAppRunning
import ru.avem.navitest.communication.devices.DeviceController.Companion.BAUDRATE
import ru.avem.navitest.communication.devices.DeviceController.Companion.NET_NAME
import ru.avem.navitest.communication.devices.DeviceController.Companion.NET_READ_TIMEOUT
import ru.avem.navitest.communication.devices.DeviceController.Companion.NET_WRITE_TIMEOUT
import ru.avem.navitest.communication.devices.kvm.KvmController
import ru.avem.navitest.communication.protocol.modbus.RTUController
import ru.avem.navitest.communication.serial.SerialConnection
import ru.avem.navitest.model.Model
import java.io.IOException
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

object DevicesController : Observable() {
    init {
        addObserver(Model)
    }

    fun init(ctx: Context) = try {
        val connection = SerialConnection(
            ctx,
            NET_NAME,
            BAUDRATE,
            UsbSerialPort.DATABITS_8,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE,
            NET_WRITE_TIMEOUT,
            NET_READ_TIMEOUT
        ).apply {
            initSerialPort()
        }

        val kvmController = KvmController(RTUController(connection))
        thread {
            while (isAppRunning) {
                notice(KvmController.Action.ALL, kvmController.read())
                sleep(1)
            }
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    private fun notice(param: Any, value: Any?) {
        setChanged()
        notifyObservers(arrayOf(param, value))
    }

    fun reinit() {
        //TODO
    }
}
