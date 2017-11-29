package com.github.charleslzq.pacsdemo.binder.vo

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-11-24.
 */
data class PatientSeriesViewModel(
        var patientMetaInfo: DicomPatientMetaInfo = DicomPatientMetaInfo(),
        var studyMetaInfo: DicomStudyMetaInfo = DicomStudyMetaInfo(),
        var dicomSeriesMetaInfo: DicomSeriesMetaInfo = DicomSeriesMetaInfo(),
        var imageFramesViewModel: ImageFramesViewModel = ImageFramesViewModel(),
        var thumbUrl: URI? = null
)