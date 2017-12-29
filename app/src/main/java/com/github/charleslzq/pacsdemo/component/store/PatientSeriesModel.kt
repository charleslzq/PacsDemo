package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-11-24.
 */
data class PatientSeriesModel(
        val modId: String = "",
        val patientMetaInfo: DicomPatientMetaInfo = DicomPatientMetaInfo(),
        val studyMetaInfo: DicomStudyMetaInfo = DicomStudyMetaInfo(),
        val seriesMetaInfo: DicomSeriesMetaInfo = DicomSeriesMetaInfo(),
        val frames: List<ImageFrameModel> = emptyList(),
        val thumb: URI? = null
)