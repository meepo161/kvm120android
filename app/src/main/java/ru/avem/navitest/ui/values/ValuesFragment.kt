package ru.avem.navitest.ui.values

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_values.*
import ru.avem.navitest.R
import ru.avem.navitest.communication.devices.kvm.KvmController
import ru.avem.navitest.communication.devices.kvm.Values
import ru.avem.navitest.model.Model
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
            //TODO записать
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
                    }
                    KvmController.Action.VOLTAGE_AMP -> {
                        etAmp.setText(String.format("%.4f", value))
                    }
                    KvmController.Action.VOLTAGE_AVR -> {
                        etAvr.setText(String.format("%.4f", value))
                    }
                    KvmController.Action.VOLTAGE_RMS -> {
                        etRms.setText(String.format("%.4f", value))
                    }
                    KvmController.Action.FREQUENCY -> {
                        etFreq.setText(String.format("%.4f", value))
                    }
                    KvmController.Action.RAZMAH -> {
                    }
                    KvmController.Action.CHTO_ESHE -> {
                    }
                    KvmController.Action.COEFFICENT_AMP -> {
                        etCoefficentAmp.setText(String.format("%.4f", value))
                    }
//                KvmController.Action.COEFFICENT_FORM -> {
//                    etCoefficentForm.setText(String.format("%.4f", value))
//                }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Model.addObserver(this)
    }

    override fun onPause() {
        Model.deleteObserver(this)
        super.onPause()
    }
}
