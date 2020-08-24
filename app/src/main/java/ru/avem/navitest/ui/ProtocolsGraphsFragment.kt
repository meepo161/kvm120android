package ru.avem.navitest.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import kotlinx.android.synthetic.main.fragment_protocol_graphs.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.avem.navitest.App
import ru.avem.navitest.MainActivity
import ru.avem.navitest.R
import ru.avem.navitest.database.graph.ProtocolGraph
import ru.avem.navitest.protocol.LoggingGraph
import java.lang.Thread.sleep
import java.util.*

class ProtocolsGraphsFragment : Fragment() {
    private val handler = Handler()
    private var mIsViewInitiated = false
    private var entries = mutableListOf<Entry>()
    private lateinit var values: List<Double>
    private lateinit var listOfProtocolsGraphs: List<ProtocolGraph>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_protocol_graphs, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (resources.configuration.orientation == MainActivity.ORIENTATION_PORTRAIT) {
            lineChartProtocol.minimumHeight = 1200
        } else if (resources.configuration.orientation == MainActivity.ORIENTATION_LANDSCAPE) {
            lineChartProtocol.minimumHeight = 500
        }

        GlobalScope.launch(Dispatchers.IO) {
            listOfProtocolsGraphs = App.instance.db.protocolGraphDao().getAll()
            mIsViewInitiated = true
        }

        while (!mIsViewInitiated) {
            sleep(10)
        }

        spinner_protocol_graphs.adapter =
            ArrayAdapter(requireContext(), R.layout.list_item_graphs, listOfProtocolsGraphs)

        GlobalScope.launch(Dispatchers.IO) {
            spinner_protocol_graphs.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapter: AdapterView<*>,
                    v: View,
                    i: Int,
                    lng: Long
                ) {
                    initGraph()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        btnOpen.setOnClickListener {
            LoggingGraph.preview(
                this.requireActivity(),
                spinner_protocol_graphs.selectedItem as ProtocolGraph
            )
        }

        btnSave.setOnClickListener {

        }

        btnDelete.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                App.instance.db.protocolGraphDao()
                    .deleteById((spinner_protocol_graphs.selectedItem as ProtocolGraph?)?.id ?: -1)
            }
            val listOfProtocolsGraphs = GlobalScope.async(Dispatchers.IO) {
                App.instance.db.protocolGraphDao().getAll()
            }

            GlobalScope.launch(Dispatchers.Main) {
                spinner_protocol_graphs.adapter =
                    ArrayAdapter(requireContext(), R.layout.list_item_graphs, listOfProtocolsGraphs.await())
            }
        }
    }

    private fun initGraph() {
        handler.post {
            entries = mutableListOf()
            values =
                (spinner_protocol_graphs.selectedItem as ProtocolGraph?)!!.values.removePrefix("[").removeSuffix("]")
                    .split(", ").map { it.replace(',', '.') }.map(String::toDouble) ?: listOf(0.0)
            var realTime = 0.0f
            for (i in values.indices) {
                if (!values[i].isNaN()) {
                    entries.add(
                        Entry(
                            realTime,
                            values[i].toFloat()
                            /*etValue.text.toString().replace(',', '.').toFloat()*/
                        )
                    )
                    realTime += 0.1f
                    Collections.sort(entries, EntryXComparator())
                    lineChartProtocol.data = LineData(
                        LineDataSet(
                            entries,
                            spinner_protocol_graphs.selectedItem.toString()
                        ).also { lineDataSet ->
                            lineDataSet.setDrawCircles(false)
                            lineDataSet.setDrawValues(false)
                            lineDataSet.color = resources.getColor(R.color.colorAccent)
                        })
                    lineChartProtocol.invalidate()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == MainActivity.ORIENTATION_PORTRAIT) {
            lineChartProtocol.minimumHeight = 1200
        } else if (newConfig.orientation == MainActivity.ORIENTATION_LANDSCAPE) {
            lineChartProtocol.minimumHeight = 500
        }
    }
}