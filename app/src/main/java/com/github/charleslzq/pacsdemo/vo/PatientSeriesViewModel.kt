package com.github.charleslzq.pacsdemo.vo

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-11-24.
 */
data class PatientSeriesViewModel(
        val patientMetaInfo: DicomPatientMetaInfo,
        val studyMetaInfo: DicomStudyMetaInfo,
        val dicomSeriesMetaInfo: DicomSeriesMetaInfo,
        val imageFramesViewModel: ImageFramesViewModel,
        val thumbUrl: URI
) {
    companion object {
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}