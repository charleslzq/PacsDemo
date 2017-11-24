package com.github.charleslzq.pacsdemo

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeries
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo

/**
 * Created by charleslzq on 17-11-24.
 */
class PatientSeriesViewModel(
        val patientMetaInfo: DicomPatientMetaInfo,
        val studyMetaInfo: DicomStudyMetaInfo,
        val dicomSeries: DicomSeries
) {
    val imageUrls = dicomSeries.images.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[DEFAULT] }
    val thumUrl = dicomSeries.images.sortedBy { it.instanceNumber?.toInt() }[0].files[THUMB]

    companion object {
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}