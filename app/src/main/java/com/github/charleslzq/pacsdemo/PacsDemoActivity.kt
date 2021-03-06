package com.github.charleslzq.pacsdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.charleslzq.dicom.data.DicomStudy
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.PacsMain
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFramesModel
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesModel
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackground
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity(), RxScheduleSupport {
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            dicomDataService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            dicomDataService = service as DicomDataService
            val patientId = intent.getStringExtra(PATIENT_ID) ?: patientId
            val studyId = intent.getStringExtra(STUDY_ID) ?: studyId
            val seriesId = intent.getStringExtra(SERIES_ID) ?: seriesId
            val imageNum = intent.getStringExtra(IMAGE_NUM) ?: imageNum
            load(patientId, studyId, seriesId, imageNum)
        }

    }
    private var dicomDataService: DicomDataService? = null
    private val patientId = "03117795"
    private val studyId = "1.2.840.113619.186.388521824370.20111208084338939.716"
    private val seriesId = "1.3.12.2.1107.5.1.4.54473.30000011120623540295300033580"
    private val imageNum = "62"
    private lateinit var pacs: PacsMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pacs_demo)
        Log.d("PacsActivity", "onCreate execute")

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        }

        pacs = PacsMain(pacsPanel, PacsStore())
        backButton.setOnClickListener { this.finish() }

        bindService(Intent(this, DicomDataServiceBackground::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    fun load(patientId: String?, studyId: String?, seriesId: String?, imageNum: String?) {
        Observable.create<MutableList<PatientSeriesModel>> {
            val patient = patientId?.let { dicomDataService?.findPatient(it) }
            it.onNext(when (patient == null) {
                true -> mutableListOf()
                false -> {
                    val filter: (DicomStudy) -> Boolean = if (studyId == null) {
                        { true }
                    } else {
                        { it.metaInfo.instanceUID == studyId }
                    }
                    patient!!.studies.filter(filter).flatMap { study ->
                        study.series.sortedBy { it.metaInfo.instanceUID }.map {
                            PatientSeriesModel(
                                    patient.metaInfo,
                                    study.metaInfo,
                                    it.metaInfo,
                                    ImageFramesModel(it.metaInfo.instanceUID!!, it.images.sortedBy { it.instanceNumber?.toInt() })
                            )
                        }
                    }.toMutableList()
                }
            })
        }.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe {
                    EventBus.post(BindingEvent.SeriesListUpdated(it))
                    if (seriesId != null) {
                        it.indices.find { index -> it[index].dicomSeriesMetaInfo.instanceUID == seriesId }?.let { index ->
                            EventBus.post(ClickEvent.ThumbListItemClicked(index))
                            EventBus.post(BindingEvent.ModelSelected(it[index]))
                            if (imageNum != null && imageNum.toInt() in (1..it[index].imageFramesModel.size)) {
                                EventBus.post(ImageDisplayEvent.IndexChange(0, imageNum.toInt() - 1, false))
                            }
                        }
                    } else if (it.isNotEmpty()) {
                        EventBus.post(ClickEvent.ThumbListItemClicked(0))
                        EventBus.post(BindingEvent.ModelSelected(it[0]))
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
