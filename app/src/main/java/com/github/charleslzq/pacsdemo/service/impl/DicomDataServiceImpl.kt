package com.github.charleslzq.pacsdemo.service.impl

import android.content.SharedPreferences
import android.os.Binder
import com.github.charleslzq.dicom.data.DicomPatient
import com.github.charleslzq.dicom.store.DicomDataStore
import com.github.charleslzq.pacsdemo.broker.DicomMessageBroker
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomDataServiceImpl(
        private val messageBroker: DicomMessageBroker,
        private val dataStore: DicomDataStore,
        private val sharedPreferences: SharedPreferences
) : Binder(), DicomDataService {

    override fun findPatient(patientId: String): DicomPatient? {
        val patientInStore = dataStore.getPatient(patientId)
        if (patientInStore == null) {
            requirePatients(patientId)
        }
        return patientInStore
    }

    override fun requirePatients(vararg patientId: String) {
        val patients = sharedPreferences.getStringSet(DicomDataServiceBackgroud.PATIENTS, emptySet()).toMutableSet()
        patients.addAll(patientId)

        val editor = sharedPreferences.edit()
        editor.putStringSet(DicomDataServiceBackgroud.PATIENTS, patients)
        editor.apply()

        messageBroker.requirePatients(*patientId)
        dataStore.reload()
    }

    override fun refreshPatient(vararg patientId: String) {
        dataStore.clearData()

        val editor = sharedPreferences.edit()
        editor.putStringSet(DicomDataServiceBackgroud.PATIENTS, setOf(*patientId))
        editor.apply()

        messageBroker.refreshPatients(*patientId)
        dataStore.reload()
    }

    override fun setUrl(url: String) {
        val editor = sharedPreferences.edit()
        editor.putString(DicomDataServiceBackgroud.WS_URL, url)
        editor.apply()
    }
}