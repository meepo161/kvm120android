package ru.avem.navitest.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
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
import ru.avem.navitest.App
import ru.avem.navitest.MainActivity.Companion.ORIENTATION_LANDSCAPE
import ru.avem.navitest.MainActivity.Companion.ORIENTATION_PORTRAIT
import ru.avem.navitest.R
import ru.avem.navitest.communication.devices.kvm.KvmController
import ru.avem.navitest.communication.devices.kvm.Values
import ru.avem.navitest.database.graph.ProtocolGraph
import ru.avem.navitest.model.Model
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random.Default.nextFloat


class GraphFragment : Fragment(), Observer {

    private var timeRecord: Int = 60
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


    private var listOfValues = mutableListOf<String>()

    private var isStartRecord = false

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
            lineChart.minimumHeight = 400
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

        btnPause.isEnabled = false
        btnStop.isEnabled = false
        btnStopRecord.isEnabled = false
        btnStartRecord.isEnabled = false

        btnStop.setOnClickListener {
            handleStop()
        }
        btnPause.setOnClickListener {
            handlePause()
        }
        btnStart.setOnClickListener {
            handleStart()
        }
        btnStartRecord.setOnClickListener {
            handleStartRecord()
        }
        btnStopRecord.setOnClickListener {
            handleStopRecord()
        }
        mIsViewInitiated = true
    }

    private fun handleStart() {
        toStart()
        if (spNeedValue.selectedItem.toString() == form) {
            btnStart.isEnabled = false
            btnStop.isEnabled = false
            do {
                val listOfDots = listOf(0f) /*CommunicationModel.avem4VoltmeterController.readDotsF()*/
                drawGraphFormVoltage(listOfDots)
                recordFormGraphInDB(listOfDots)
                sleep(2000)
            } while (cbAuto.isSelected)
        } else {
            showGraph()
        }
    }

    private fun handlePause() {
        toPause()
        isPause = true
    }

    private fun handleStop() {
        toStop()
        isPause = false
        isStop = true
        isStartRecord = false
    }

    private fun handleStartRecord() {
        toStartRecord()
        isStartRecord = true
        recordGraphInDB()
    }

    private fun handleStopRecord() {
        isStartRecord = false
        toStopRecord()
    }

    private fun recordFormGraphInDB(list: List<Float>) {
        listOfValues.clear()
        for (i in list.indices) {
            listOfValues.add(list[i].toString())
        }
        saveProtocolToDB(listOfValues)
    }

    private fun recordGraphInDB() {
        listOfValues.clear()
        thread {
            var realTime = 0.0
            while (isStartRecord) {
                listOfValues.add(etValue.text.toString())
                sleep(100)
                realTime += 0.1
                if (realTime > timeRecord) {
                    isStartRecord = false
                    GlobalScope.launch(Dispatchers.Main) {
                        handleStopRecord() //как вызвать не из потока
                    }
                }
            }
            saveProtocolToDB(listOfValues)
        }
    }

    private fun saveProtocolToDB(list: List<String>) {
        val dateFormatter = SimpleDateFormat("dd.MM.y", Locale.US)
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)

        val unixTime = System.currentTimeMillis()
        GlobalScope.launch(Dispatchers.IO) {
            App.instance.db.protocolGraphDao().insert(
                ProtocolGraph(
                    date = dateFormatter.format(unixTime).toString(),
                    time = timeFormatter.format(unixTime).toString(),
                    typeOfValue = spNeedValue.selectedItem.toString(),
                    values = list.toString()
                )
            )
        }
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
                drawGraph(entries)
                realTime += 0.1f
                delay(100)
            }
            spNeedValue.isEnabled = true
        }
    }

    private fun drawGraph(entries: MutableList<Entry>) {
        if (!isPause) {
            etValue.text = nextFloat().toString()
        } else {
            etValue.text = 0.0f.toString()
        }
        entries.add(Entry(realTime, etValue.text.toString().replace(',', '.').toFloat()))
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

    private fun drawGraphFormVoltage(list1: List<Float>) {
        etValue.text = String.format("%4f", coefForm)
//        resetLineChart()
//        for (element in list1) {
//            series.data.add(XYChart.Data(realTime++, element))
//        }
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
        timeRecord = PreferenceManager.getDefaultSharedPreferences(activity).getString("timeRecord", "1")!!.toInt() * 60
        super.onResume()
        Model.addObserver(this)
        drawGraph(mutableListOf())
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
            lineChart.minimumHeight = 400
        }
    }

    private fun toStart() {
        btnStart.isEnabled = false
        btnPause.isEnabled = true
        btnStop.isEnabled = true
        if (!isPause) {
            btnStartRecord.isEnabled = true
        }
    }

    private fun toPause() {
        btnStart.isEnabled = true
        btnPause.isEnabled = false
        btnStop.isEnabled = true
    }

    private fun toStop() {
        handleStopRecord()
        btnStart.isEnabled = true
        btnPause.isEnabled = false
        btnStop.isEnabled = false
        btnStartRecord.isEnabled = false
    }

    private fun toStartRecord() {
        btnStartRecord.isEnabled = false
        btnStopRecord.isEnabled = true
    }

    private fun toStopRecord() {
        btnStartRecord.isEnabled = true
        btnStopRecord.isEnabled = false
    }
}