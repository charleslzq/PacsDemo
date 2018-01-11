package com.github.charleslzq.pacsdemo.broker.message

import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.dicom.store.DicomDataStore
import java.io.File

/**
 * Created by charleslzq on 17-11-15.
 */
class StoreMessageListener(private val dicomDataStore: DicomDataStore) : DicomMessageListener {

    override fun onPatient(dicomPatientMessage: Message<DicomPatient>) = dicomDataStore.savePatient(dicomPatientMessage.payload)

    override fun onPatientMeta(dicomPatientMetaInfoMessage: Message<DicomPatientMetaInfo>) = dicomDataStore.savePatientMeta(dicomPatientMetaInfoMessage.payload)

    override fun onStudyMeta(dicomStudyMetaInfoMessage: Message<DicomStudyMetaInfo>)
            = dicomDataStore.saveStudyMeta(checkAndGet(dicomStudyMetaInfoMessage.headers, MessageHeaders.PATIENT_ID), dicomStudyMetaInfoMessage.payload)

    override fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<DicomSeriesMetaInfo>)
            = dicomDataStore.saveSeriesMeta(checkAndGet(dicomSeriesMetaInfoMessage.headers, MessageHeaders.PATIENT_ID),
            checkAndGet(dicomSeriesMetaInfoMessage.headers, MessageHeaders.STUDY_ID),
            dicomSeriesMetaInfoMessage.payload)

    override fun onImageMeta(dicomImageMetaInfoMessage: Message<DicomImageMetaInfo>)
            = dicomDataStore.saveImage(checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.PATIENT_ID),
            checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.STUDY_ID),
            checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.SERIES_ID),
            dicomImageMetaInfoMessage.payload)

    override fun onFile(byteArrayMessage: Message<ByteArray>) {
        val fileDir = checkAndGet(byteArrayMessage.headers, MessageHeaders.FILE_DIR)
        val fileName = checkAndGet(byteArrayMessage.headers, MessageHeaders.FILE_NAME)
        val file = File(fileDir + File.separator + fileName)
        val content = byteArrayMessage.payload
        file.writeBytes(content)
    }

    private fun checkAndGet(headers: Map<String, String>, key: MessageHeaders)
            = headers[key.value] ?: throw IllegalArgumentException("Required header ${key.value} not found")
}