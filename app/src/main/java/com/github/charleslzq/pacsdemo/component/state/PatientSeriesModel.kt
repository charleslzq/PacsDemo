package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo

/**
 * Created by charleslzq on 17-11-24.
 */
data class PatientSeriesModel(
        val patientMetaInfo: DicomPatientMetaInfo = DicomPatientMetaInfo(),
        val studyMetaInfo: DicomStudyMetaInfo = DicomStudyMetaInfo(),
        val dicomSeriesMetaInfo: DicomSeriesMetaInfo = DicomSeriesMetaInfo(),
        val imageFramesModel: ImageFramesModel = ImageFramesModel()
) {
    fun clone() = copy(
            patientMetaInfo = patientMetaInfo.copy(),
            studyMetaInfo = studyMetaInfo.copy(),
            dicomSeriesMetaInfo = dicomSeriesMetaInfo.copy(),
            imageFramesModel = imageFramesModel.copy()
    )
}