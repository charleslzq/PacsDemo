package com.github.charleslzq.pacsdemo.broker.message

import com.github.charleslzq.dicom.data.*

/**
 * Created by charleslzq on 17-11-15.
 */
class CompositeDicomMessageListener : DicomMessageListener {
    private val listeners: MutableList<DicomMessageListener> = emptyList<DicomMessageListener>().toMutableList()

    fun register(listener: DicomMessageListener) {
        listeners.add(listener)
    }

    fun cancel(listener: DicomMessageListener) {
        listeners.remove(listener)
    }

    override fun onPatient(dicomPatientMessage: Message<DicomPatient>) {
        listeners.forEach { it.onPatient(dicomPatientMessage) }
    }

    override fun onPatientMeta(dicomPatientMetaInfoMessage: Message<DicomPatientMetaInfo>) {
        listeners.forEach { it.onPatientMeta(dicomPatientMetaInfoMessage) }
    }

    override fun onStudyMeta(dicomStudyMetaInfoMessage: Message<DicomStudyMetaInfo>) {
        listeners.forEach { it.onStudyMeta(dicomStudyMetaInfoMessage) }
    }

    override fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<DicomSeriesMetaInfo>) {
        listeners.forEach { it.onSeriesMeta(dicomSeriesMetaInfoMessage) }
    }

    override fun onImageMeta(dicomImageMetaInfoMessage: Message<DicomImageMetaInfo>) {
        listeners.forEach { it.onImageMeta(dicomImageMetaInfoMessage) }
    }

    override fun onFile(byteArrayMessage: Message<ByteArray>) {
        listeners.forEach { it.onFile(byteArrayMessage) }
    }
}