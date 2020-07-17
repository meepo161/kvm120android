package ru.avem.navitest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import ru.avem.navitest.communication.devices.DevicesController
import ru.avem.navitest.utils.Logger
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        const val ACTION_INIT_USB_PERMISSION = "ru.avem.navitest.INIT_USB_PERMISSION"
        const val ACTION_USB_PERMISSION = "ru.avem.navitest.USB_PERMISSION"
        const val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        const val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
        const val RS485_DEVICE_NAME = "CP2103 USB to RS-485"
    }

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val mUnauthorizedDevices: Deque<UsbDevice> = ArrayDeque()
    private var mCurrentUnauthorizedUsbDevice: UsbDevice? = null
    private var mNeedToAuthorizeDevices = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
//            Snackbar.make(view, "Отправьте нам ", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@avem.ru"))
            email.putExtra(Intent.EXTRA_SUBJECT, "КВМ120")
            email.putExtra(Intent.EXTRA_TEXT, "")
            email.type = "message/rfc822"
            startActivity(Intent.createChooser(email, "Выберите почту:"))
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_values, R.id.nav_graph, R.id.nav_dopValues
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setBroadcastReceiver()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setBroadcastReceiver() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(ACTION_INIT_USB_PERMISSION)
        filter.addAction(ACTION_USB_ATTACHED)
        filter.addAction(ACTION_USB_DETACHED)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (ACTION_INIT_USB_PERMISSION == action) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            when (device.productName) {
                                RS485_DEVICE_NAME -> DevicesController.reinit()
                            }
                            Logger.withTag("UNAUTHORIZED")
                                .log("broadcastReceiver: receivePermissionGranted: " + device.productName)
                            if (device.productName == mCurrentUnauthorizedUsbDevice!!.productName) {
                                mCurrentUnauthorizedUsbDevice = null
                                accessForNextUnauthorizedDevice()
                            }
                        }
                    } else {
                        if (device != null) {
                            Logger.withTag("UNAUTHORIZED")
                                .log("broadcastReceiver: receivePermissionUngranted: " + device.productName)
                            if (device.productName == mCurrentUnauthorizedUsbDevice!!.productName) {
                                accessForNextUnauthorizedDevice()
                            }
                        }
                    }
                } else if (ACTION_USB_ATTACHED == action) {
                    Logger.withTag("USB_DEVICES").log("Attached:" + device!!.productName)
                    val usbManager = getSystemService(USB_SERVICE) as UsbManager
                    if (!usbManager.hasPermission(device)) {
                        mUnauthorizedDevices.add(device)
                        Logger.withTag("UNAUTHORIZED").log("added: " + device.productName)
                        accessForNextUnauthorizedDevice()
                    }
                } else if (ACTION_USB_DETACHED == action) {
                    Logger.withTag("USB_DEVICES").log("Detached:" + device!!.productName)
                }
            }
        }
        registerReceiver(broadcastReceiver, filter)
    }

    private fun accessForNextUnauthorizedDevice() {
        if (mNeedToAuthorizeDevices) {
            Logger.withTag("UNAUTHORIZED").log("access")
            val usbManager = getSystemService(USB_SERVICE) as UsbManager
            Logger.withTag("UNAUTHORIZED").log("access: usbManager != null")
            if (!mUnauthorizedDevices.isEmpty() || mCurrentUnauthorizedUsbDevice != null) {
                if (!mUnauthorizedDevices.isEmpty() && mCurrentUnauthorizedUsbDevice == null) {
                    mCurrentUnauthorizedUsbDevice = mUnauthorizedDevices.pollFirst()
                    Logger.withTag("UNAUTHORIZED")
                        .log("access: poll: " + mCurrentUnauthorizedUsbDevice!!.productName)
                }
                val pi = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_INIT_USB_PERMISSION),
                    0
                )
                usbManager.requestPermission(mCurrentUnauthorizedUsbDevice, pi)
                Logger.withTag("UNAUTHORIZED")
                    .log("access: requestPermission: " + mCurrentUnauthorizedUsbDevice!!.productName)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mNeedToAuthorizeDevices = true
        searchForUnauthorizedDevices()
    }

    private fun searchForUnauthorizedDevices() {
        Logger.withTag("UNAUTHORIZED").log("search")
        mUnauthorizedDevices.clear()
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        for (usbDevice in usbManager.deviceList.values) {
            if (!usbManager.hasPermission(usbDevice)) {
                mUnauthorizedDevices.add(usbDevice)
                Logger.withTag("UNAUTHORIZED").log("added: " + usbDevice.productName)
            }
        }
        accessForNextUnauthorizedDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
