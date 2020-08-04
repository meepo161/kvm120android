package ru.avem.navitest.ui

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import kotlinx.android.synthetic.main.fragment_graph.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.avem.navitest.MainActivity.Companion.ORIENTATION_LANDSCAPE
import ru.avem.navitest.MainActivity.Companion.ORIENTATION_PORTRAIT
import ru.avem.navitest.R
import ru.avem.navitest.communication.devices.kvm.KvmController
import ru.avem.navitest.communication.devices.kvm.Values
import ru.avem.navitest.model.Model
import java.util.*
import kotlin.random.Random.Default.nextFloat


class GraphFragment : Fragment(), Observer {

    private var mIsViewInitiated = false

    private var rms = "Действующее"
    private var avr = "Среднее"
    private var amp = "Амплитудное"
    private var form = "Форма"
    private var freq = "Частота"
    private var coefAmp = "Коэффицент амплитуды"
    private var coefForm = "Коэффицент формы"

    private val handler = Handler()
    private var isStop = false
    private var isPause = false
    private var realTime = 0.0f
    private val entries = mutableListOf<Entry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            lineChart.minimumHeight = 1200
        } else if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
            lineChart.minimumHeight = 500
        }
        var selectedItem = ""

        spNeedValue.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapter: AdapterView<*>,
                v: View,
                i: Int,
                lng: Long
            ) {
                selectedItem = adapter.getItemAtPosition(i).toString()
                cbAuto.isVisible = selectedItem == form
                btnPause.isVisible = selectedItem != form
                btnStop.isVisible = selectedItem != form
                btnStartRecord.isVisible = selectedItem != form
                btnStopRecord.isVisible = selectedItem != form
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        btnStop.setOnClickListener {
            isStop = true
        }
        btnPause.setOnClickListener {
            isPause = true
        }

        btnStart.setOnClickListener {
            if (spNeedValue.selectedItem.toString() == form) {

            } else {
                showGraph()
            }
        }

        mIsViewInitiated = true
    }

    private fun showGraph() {
        spNeedValue.isEnabled = false
        if (isStop) {
            entries.clear()
            realTime = 0.0f
        }
        isStop = false
        isPause = false
        lineChart.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.Main) {
            while (!isStop) {
                if (isPause) {
                    //TODO вместо 0 новый график рисовать
                } else {
                    drawGraph(entries)
                }
                realTime += 0.1f
                delay(100)
            }
            spNeedValue.isEnabled = true
        }
    }

    private fun drawGraph(entries: MutableList<Entry>) {
        entries.add(
            Entry(
                realTime,
                nextFloat()
                /*etValue.text.toString().replace(',', '.').toFloat()*/
            )
        )
        Collections.sort(entries, EntryXComparator())
        lineChart.data = LineData(
            LineDataSet(
                entries,
                spNeedValue.selectedItem.toString()
            ).also { lineDataSet ->
                lineDataSet.setDrawCircles(false)
                lineDataSet.setDrawValues(false)
                lineDataSet.color = resources.getColor(R.color.colorAccent)
            })
        lineChart.invalidate()
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
                        when (spNeedValue.selectedItem.toString()) {
                            rms -> {
                                etValue.text = String.format("%.4f", allValues.voltageRms)
                            }
                            avr -> {
                                etValue.text = String.format("%.4f", allValues.voltageAvr)
                            }
                            amp -> {
                                etValue.text = String.format("%.4f", allValues.voltageAmp)
                            }
                            freq -> {
                                etValue.text = String.format("%.4f", allValues.frequency)
                            }
                            coefAmp -> {
                                etValue.text = String.format("%.4f", allValues.coefficentAmp)
                            }
                            coefForm -> {
                                etValue.text = String.format("%.4f", allValues.coefficentForm)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Model.addObserver(this)
    }

    override fun onPause() {
        isStop = true
        Model.deleteObserver(this)
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ORIENTATION_PORTRAIT) {
            lineChart.minimumHeight = 1200
        } else if (newConfig.orientation == ORIENTATION_LANDSCAPE) {
            lineChart.minimumHeight = 500
        }
    }
}