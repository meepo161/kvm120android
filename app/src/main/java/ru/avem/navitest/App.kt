package ru.avem.navitest

import android.app.Application
import ru.avem.navitest.communication.devices.DevicesController
import ru.avem.navitest.database.AppDatabase

class App: Application() {
    lateinit var db: AppDatabase
    override fun onCreate() {
        super.onCreate()
//        DevicesController.init(this)
        db = AppDatabase.getInstance(this)
        instance = this
    }

    override fun onTerminate() {
        isAppRunning = false
        super.onTerminate()
    }

    companion object {
        var isAppRunning = true
        lateinit var instance: App
            private set
    }
}
