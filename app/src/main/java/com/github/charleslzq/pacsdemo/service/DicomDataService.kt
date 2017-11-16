package com.github.charleslzq.pacsdemo.service

import com.github.charleslzq.dicom.data.DicomPatient

/**
 * Created by charleslzq on 17-11-16.
 */
interface DicomDataService {
    fun setUrl(url: String)
    fun requirePatients(vararg patientId: String)
    fun refreshPatient(vararg patientId: String)
    fun findPatient(patientId: String): DicomPatient?
}