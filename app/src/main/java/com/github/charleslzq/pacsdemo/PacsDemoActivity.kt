package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.charleslzq.pacsdemo.component.PacsMain
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.store.ImageFramesModel
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesModel
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import com.github.charleslzq.pacsdemo.support.SimpleServiceConnection
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private lateinit var pacs: PacsMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pacs_demo)
        Log.d("PacsDemoActivity", "onCreate execute")

        pacs = PacsMain(pacsPanel, PacsStore())
        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        refreshButton.setOnClickListener { refresh() }
    }

    private fun refresh() {
        val patient = dicomDataService?.findPatient(patientId)
        if (patient != null) {
            EventBus.post(BindingEvent.SeriesListUpdated(
                    patient.studies.flatMap { study ->
                        study.series.sortedBy { it.metaInfo.instanceUID }.map {
                            PatientSeriesModel(
                                    patient.metaInfo,
                                    study.metaInfo,
                                    it.metaInfo,
                                    ImageFramesModel(it.images.sortedBy { it.instanceNumber?.toInt() })
                            )
                        }
                    }.toMutableList()
            ))
        }
    }
}
