package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo

/**
 * Created by charleslzq on 17-11-24.
 */
data class PatientSeriesModel(
        var patientMetaInfo: DicomPatientMetaInfo = DicomPatientMetaInfo(),
        var studyMetaInfo: DicomStudyMetaInfo = DicomStudyMetaInfo(),
        var dicomSeriesMetaInfo: DicomSeriesMetaInfo = DicomSeriesMetaInfo(),
        var imageFramesModel: ImageFramesModel = ImageFramesModel()
)