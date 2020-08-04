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
import java.text.SimpleDateFormat
import java.util.*

class ValuesFragment : Fragment(), Observer {
    private val handler = Handler()
    private var mIsViewInitiated = false

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
                        etAmp.setText(String.format("%.4f", allValues.voltageAmp))
                        etAvr.setText(String.format("%.4f", allValues.voltageAvr))
                        etRms.setText(String.format("%.4f", allValues.voltageRms))
                        etFreq.setText(String.format("%.4f", allValues.frequency))
                        etCoefficentAmp.setText(String.format("%.4f", allValues.coefficentAmp))
                        etCoefficentForm.setText(String.format("%.4f", allValues.coefficentForm))
                        etTimeAveraging.setText(String.format("%.1f", allValues.timeAveraging))
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
        super.onResume()
        Model.addObserver(this)
    }

    override fun onPause() {
        Model.deleteObserver(this)
        super.onPause()
    }

    fun saveProtocolDotToDB() {
        val dateFormatter = SimpleDateFormat("dd.MM.y", Locale.US)
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)

        val unixTime = System.currentTimeMillis()
        GlobalScope.launch(Dispatchers.IO) {
            App.instance.db.protocolGraphDot().insert(
                ProtocolDot(
                    dateDot = dateFormatter.format(unixTime).toString(),
                    timeDot = timeFormatter.format(unixTime).toString(),
                    rms = tvRms.text.toString(),
                    avr = tvAvr.text.toString(),
                    amp = tvAmp.text.toString(),
                    freq = tvFreq.text.toString(),
                    coefamp = tvCoefficentAmp.text.toString(),
                    coefform = tvCoefficentForm.text.toString()
                )
            )
        }
    }
}