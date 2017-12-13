package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.PacsMain
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFramesModel
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesModel
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import com.github.charleslzq.pacsdemo.support.SimpleServiceConnection
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set, this::refresh)
    private var dicomDataService: DicomDataService? = null
    private var patientId = "03117795"
    private var studyId = "1.2.840.113619.186.388521824370.20111208084338939.716"
    private var seriesId = "1.3.12.2.1107.5.1.4.54473.30000011120623540295300033580"
    private var imageNum = "62"
    private lateinit var pacs: PacsMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pacs_demo)
        Log.d("PacsActivity", "onCreate execute")

        patientId =  intent.getStringExtra(PATIENT_ID) ?: this.patientId
        studyId = intent.getStringExtra(STUDY_ID) ?: this.studyId
        seriesId = intent.getStringExtra(SERIES_ID) ?: this.seriesId
        imageNum = intent.getStringExtra(IMAGE_NUM) ?: this.imageNum

        pacs = PacsMain(pacsPanel, PacsStore())
        refreshButton.setOnClickListener { refresh() }
        backButton.setOnClickListener { this.finish() }

        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    private fun refresh() {
        val patient = dicomDataService!!.findPatient(patientId)
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
            pacs.store.seriesList.forEachIndexed { index, it ->
                if (it.dicomSeriesMetaInfo.instanceUID == seriesId && it.studyMetaInfo.instanceUID == studyId) {
                    if (imageNum.toInt() in (1..it.imageFramesModel.size)) {
                        EventBus.post(BindingEvent.ModelSelected(it))
                        EventBus.post(ImageDisplayEvent.IndexChange(0, imageNum.toInt()-1))
                    }
                }
            }
        }
    }

    companion object {
        val PATIENT_ID = "patientId"
        val STUDY_ID = "studyId"
        val SERIES_ID = "seriesId"
        val IMAGE_NUM = "imageNum"
    }
}
