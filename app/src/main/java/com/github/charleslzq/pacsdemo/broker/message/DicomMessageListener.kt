package com.github.charleslzq.pacsdemo.broker.message

import com.github.charleslzq.dicom.data.*

/**
 * Created by charleslzq on 17-11-15.
 */
interface DicomMessageListener {
    fun onPatient(dicomPatientMessage: Message<DicomPatient<DicomPatientMetaInfo, DicomStudyMetaInfo, DicomSeriesMetaInfo, DicomImageMetaInfo>>)
    fun onPatientMeta(dicomPatientMetaInfoMessage: Message<DicomPatientMetaInfo>)
    fun onStudyMeta(dicomStudyMetaInfoMessage: Message<DicomStudyMetaInfo>)
    fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<DicomSeriesMetaInfo>)
    fun onImageMeta(dicomImageMetaInfoMessage: Message<DicomImageMetaInfo>)
    fun onFile(byteArrayMessage: Message<ByteArray>)
}