package com.github.charleslzq.pacsdemo.broker.message

import android.util.Log
import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.dicom.store.DicomDataStore
import java.nio.file.Paths

/**
 * Created by charleslzq on 17-11-15.
 */
class StoreMessageListener(
        private val dicomDataStore: DicomDataStore
) : DicomMessageListener {

    override fun onPatient(dicomPatientMessage: Message<DicomPatient>) {
        Log.i("receive", "${Thread.currentThread().id}")
        dicomDataStore.savePatient(dicomPatientMessage.payload)
        dicomDataStore.reload()
    }

    override fun onPatientMeta(dicomPatientMetaInfoMessage: Message<DicomPatientMetaInfo>) {
        val patient = DicomPatient(dicomPatientMetaInfoMessage.payload, emptyList<DicomStudy>().toMutableList())
        dicomDataStore.savePatient(patient)
        dicomDataStore.reload()
    }

    override fun onStudy(dicomStudyMessage: Message<DicomStudy>) {
        val patientId = checkAndGet(dicomStudyMessage.headers, MessageHeaders.PATIENT_ID)
        dicomDataStore.saveStudy(patientId, dicomStudyMessage.payload)
        dicomDataStore.reload()
    }

    override fun onStudyMeta(dicomStudyMetaInfoMessage: Message<DicomStudyMetaInfo>) {
        val patientId = checkAndGet(dicomStudyMetaInfoMessage.headers, MessageHeaders.PATIENT_ID)
        val study = DicomStudy(dicomStudyMetaInfoMessage.payload, emptyList<DicomSeries>().toMutableList())
        dicomDataStore.saveStudy(patientId, study)
        dicomDataStore.reload()
    }

    override fun onSeries(dicomSeriesMessage: Message<DicomSeries>) {
        val patientId = checkAndGet(dicomSeriesMessage.headers, MessageHeaders.PATIENT_ID)
        val studyId = checkAndGet(dicomSeriesMessage.headers, MessageHeaders.STUDY_ID)
        dicomDataStore.saveSeries(patientId, studyId, dicomSeriesMessage.payload)
        dicomDataStore.reload()
    }

    override fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<DicomSeriesMetaInfo>) {
        val patientId = checkAndGet(dicomSeriesMetaInfoMessage.headers, MessageHeaders.PATIENT_ID)
        val studyId = checkAndGet(dicomSeriesMetaInfoMessage.headers, MessageHeaders.STUDY_ID)
        val series = DicomSeries(dicomSeriesMetaInfoMessage.payload, emptyList<DicomImageMetaInfo>().toMutableList())
        dicomDataStore.saveSeries(patientId, studyId, series)
        dicomDataStore.reload()
    }

    override fun onImageMeta(dicomImageMetaInfoMessage: Message<DicomImageMetaInfo>) {
        val patientId = checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.PATIENT_ID)
        val studyId = checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.STUDY_ID)
        val seriesId = checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.SERIES_ID)
        Log.i("test", "image saved for $patientId, $studyId, $seriesId, $dicomImageMetaInfoMessage")
        dicomDataStore.saveImage(patientId, studyId, seriesId, dicomImageMetaInfoMessage.payload)
        dicomDataStore.reload()
    }

    override fun onFile(byteArrayMessage: Message<ByteArray>) {
        Log.i("receiveFile", "${Thread.currentThread().id}")
        val fileDir = checkAndGet(byteArrayMessage.headers, MessageHeaders.FILE_DIR)
        val fileName = checkAndGet(byteArrayMessage.headers, MessageHeaders.FILE_NAME)
        val file = Paths.get(fileDir, fileName).toFile()
        val content = byteArrayMessage.payload
        file.writeBytes(content)
    }

    private fun checkAndGet(headers: Map<String, String>, key: MessageHeaders): String {
        val result = headers[key.value]
        when (result) {
            null -> throw IllegalArgumentException("Required header ${key.value} not found")
            else -> return result
        }
    }
}