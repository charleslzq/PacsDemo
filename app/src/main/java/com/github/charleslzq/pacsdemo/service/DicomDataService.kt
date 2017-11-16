package com.github.charleslzq.pacsdemo.service

import com.github.charleslzq.dicom.data.DicomPatient
import com.github.charleslzq.dicom.store.DicomDataStore

/**
 * Created by charleslzq on 17-11-16.
 */
interface DicomDataService {
    fun getStore(): DicomDataStore
    fun setUrl(url: String)
    fun requirePatients(vararg patientId: String)
    fun refreshPatient(vararg patientId: String)
    fun findPatient(patientId: String): DicomPatient?
}