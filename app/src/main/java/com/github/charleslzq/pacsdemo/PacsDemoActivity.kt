package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.charleslzq.pacsdemo.component.PacsMain
import com.github.charleslzq.pacsdemo.component.state.ImageFramesModel
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesModel
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private lateinit var pacsMainViewBinder: PacsMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pacs_demo)
        Log.d("PacsDemoActivity", "onCreate execute")

        pacsMainViewBinder = PacsMain(pacsPanel)
        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        refresh()
        refreshButton.setOnClickListener { refresh() }
    }

    private fun refresh() {
        val patient = dicomDataService?.findPatient(patientId)
        if (patient != null) {
            pacsMainViewBinder.state = PacsViewState(patient.studies.flatMap { study ->
                study.series.sortedBy { it.metaInfo.instanceUID }.map {
                    PatientSeriesModel(
                            patient.metaInfo,
                            study.metaInfo,
                            it.metaInfo,
                            ImageFramesModel(it.images.sortedBy { it.instanceNumber?.toInt() })
                    )
                }
            }.toMutableList())
        }
    }
}
