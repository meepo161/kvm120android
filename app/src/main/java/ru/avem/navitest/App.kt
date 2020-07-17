package ru.avem.navitest

import android.app.Application
import ru.avem.navitest.communication.devices.DevicesController

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DevicesController.init(this)
    }

    override fun onTerminate() {
        isAppRunning = false
        super.onTerminate()
    }

    companion object {
        var isAppRunning = true
    }
}
