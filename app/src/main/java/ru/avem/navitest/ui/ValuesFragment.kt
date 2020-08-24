package ru.avem.navitest.ui

import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_values.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.avem.navitest.App
import ru.avem.navitest.R
import ru.avem.navitest.communication.devices.kvm.KvmController
import ru.avem.navitest.communication.devices.kvm.Values
import ru.avem.navitest.database.dot.ProtocolDot
import ru.avem.navitest.model.Model
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random.Default.nextFloat


class ValuesFragment : Fragment(), Observer {
    private val handler = Handler()
    private var mIsViewInitiated = false
    var t = Thread()
    var isPaused = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_values, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mIsViewInitiated = true

        btnApplyTimeAveraging.setOnClickListener {
            //TODO перенести из КВМ120
            handler.post {
                etAmp.setText(String.format("%.4f", nextFloat()))
                etAvr.setText(String.format("%.4f", nextFloat()))
                etRms.setText(String.format("%.4f", nextFloat()))
                etFreq.setText(String.format("%.4f", nextFloat()))
                etCoefficentAmp.setText(String.format("%.4f", nextFloat()))
                etCoefficentForm.setText(String.format("%.4f", nextFloat()))
                etTimeAveraging.setText(String.format("%.1f", nextFloat()))
            }
        }
        btnSaveDot.setOnClickListener {
            saveProtocolDotToDB()
        }

    }

    override fun update(observable: Observable, values: Any) {
        if (mIsViewInitiated) {
            handler.post {
                val values = (values as Array<*>)
                val action = values[0]
                val value = values[1]
                when (action as KvmController.Action) {
                    KvmController.Action.IS_RESPONDING -> {
                    }
                    KvmController.Action.ALL -> {
                        val allValues = value as Values
                        etAmp.setText(String.format("%.4f", nextFloat()))
                        etAvr.setText(String.format("%.4f", nextFloat()))
                        etRms.setText(String.format("%.4f", nextFloat()))
                        etFreq.setText(String.format("%.4f", nextFloat()))
                        etCoefficentAmp.setText(String.format("%.4f", nextFloat()))
                        etCoefficentForm.setText(String.format("%.4f", nextFloat()))
                        etTimeAveraging.setText(String.format("%.1f", nextFloat()))
                    }
                }
            }
        }
    }

    override fun onResume() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        etRms.isVisible = (prefs.getBoolean("tvRms", false))
        tvRms.isVisible = (prefs.getBoolean("tvRms", false))

        etAvr.isVisible = (prefs.getBoolean("tvAvr", false))
        tvAvr.isVisible = (prefs.getBoolean("tvAvr", false))

        etAmp.isVisible = (prefs.getBoolean("tvAmp", false))
        tvAmp.isVisible = (prefs.getBoolean("tvAmp", false))

        etFreq.isVisible = (prefs.getBoolean("tvFreq", false))
        tvFreq.isVisible = (prefs.getBoolean("tvFreq", false))

        etCoefficentAmp.isVisible = (prefs.getBoolean("tvCoefficentAmp", false))
        tvCoefficentAmp.isVisible = (prefs.getBoolean("tvCoefficentAmp", false))

        etCoefficentForm.isVisible = (prefs.getBoolean("tvCoefficentForm", false))
        tvCoefficentForm.isVisible = (prefs.getBoolean("tvCoefficentForm", false))


        t = Thread(Runnable {
            while (!isPaused) {
                try {
                    requireActivity().runOnUiThread {
                        etAmp.setText(String.format("%.4f", nextFloat()))
                        etAvr.setText(String.format("%.4f", nextFloat()))
                        etRms.setText(String.format("%.4f", nextFloat()))
                        etFreq.setText(String.format("%.4f", nextFloat()))
                        etCoefficentAmp.setText(String.format("%.4f", nextFloat()))
                        etCoefficentForm.setText(String.format("%.4f", nextFloat()))
                        etTimeAveraging.setText(String.format("%.1f", nextFloat()))
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                sleep(100)
            }
        })
        t.isDaemon = true
        t.start()

        super.onResume()
        Model.addObserver(this)
    }

    override fun onPause() {
        isPaused = true
        Model.deleteObserver(this)
        super.onPause()
    }

    private fun saveProtocolDotToDB() {
        val dateFormatter = SimpleDateFormat("dd.MM.y", Locale.US)
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)

        val unixTime = System.currentTimeMillis()
        GlobalScope.launch(Dispatchers.IO) {
            App.instance.db.protocolDotDao().insert(
                ProtocolDot(
                    dateDot = dateFormatter.format(unixTime).toString(),
                    timeDot = timeFormatter.format(unixTime).toString(),
                    rms = etRms.text.toString(),
                    avr = etAvr.text.toString(),
                    amp = etAmp.text.toString(),
                    freq = etFreq.text.toString(),
                    coefamp = etCoefficentAmp.text.toString(),
                    coefform = etCoefficentForm.text.toString()
                )
            )
        }
    }
}
