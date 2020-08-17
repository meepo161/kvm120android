package ru.avem.navitest.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_protocol_dots.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.avem.navitest.App
import ru.avem.navitest.R
import ru.avem.navitest.database.dot.ProtocolDot
import ru.avem.navitest.protocol.LoggingDot
import java.lang.Thread.sleep

class ProtocolsDotsFragment : Fragment() {
    private val handler = Handler()
    private var mIsViewInitiated = false
    private lateinit var listOfProtocolsDots: List<ProtocolDot>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_protocol_dots, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            listOfProtocolsDots = App.instance.db.protocolDotDao().getAll()
            mIsViewInitiated = true
        }

        while (!mIsViewInitiated) {
            sleep(10)
        }

        spinner_protocol_dots.adapter =
            ArrayAdapter(requireContext(), R.layout.list_item_dots, listOfProtocolsDots)

        GlobalScope.launch(Dispatchers.IO) {
            spinner_protocol_dots.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapter: AdapterView<*>,
                    v: View,
                    i: Int,
                    lng: Long
                ) {
                    handler.post {
                        etRms.setText(listOfProtocolsDots[i].rms)
                        etAvr.setText(listOfProtocolsDots[i].avr)
                        etAmp.setText(listOfProtocolsDots[i].amp)
                        etFreq.setText(listOfProtocolsDots[i].freq)
                        etCoefficentForm.setText(listOfProtocolsDots[i].coefform)
                        etCoefficentAmp.setText(listOfProtocolsDots[i].coefamp)
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                }
            }
        }


        btnOpen.setOnClickListener {
            LoggingDot.preview(
                this.requireActivity(),
                spinner_protocol_dots.selectedItem as ProtocolDot
            )
        }

        btnSave.setOnClickListener {
            //TODO
        }

        btnDelete.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                App.instance.db.protocolDotDao()
                    .deleteById((spinner_protocol_dots.selectedItem as ProtocolDot?)?.id ?: -1)
            }
            val listOfProtocolsDots = GlobalScope.async(Dispatchers.IO) {
                App.instance.db.protocolDotDao().getAll()
            }

            GlobalScope.launch(Dispatchers.Main) {
                spinner_protocol_dots.adapter =
                    ArrayAdapter(
                        requireContext(),
                        R.layout.list_item_dots,
                        listOfProtocolsDots.await()
                    )
            }
        }
    }
}
