package com.github.charleslzq.pacsdemo.service.impl

import android.content.SharedPreferences
import android.os.Binder
import com.github.charleslzq.dicom.data.DicomPatient
import com.github.charleslzq.dicom.store.DicomDataStore
import com.github.charleslzq.pacsdemo.broker.DicomMessageBroker
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackground
import com.github.charleslzq.pacsdemo.support.MemCache
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomDataServiceImpl(
        private val messageBroker: DicomMessageBroker,
        private val dataStore: DicomDataStore,
        private val sharedPreferences: SharedPreferences
) : Binder(), DicomDataService, RxScheduleSupport {
    private val cache = MemCache(DicomPatient::class.java, 5)

    override fun findPatient(patientId: String) = cache.load(patientId) {
        callOnIo {
            listOfNotNull(dataStore.getPatient(patientId)).apply {
                if (isEmpty()) {
                    requirePatients(patientId)
                }
            }
        }.firstOrNull()
    }

    override fun requirePatients(vararg patientId: String) = runOnIo {
        val patients = sharedPreferences.getStringSet(DicomDataServiceBackground.PATIENTS, emptySet()).toMutableSet()
        patients.addAll(patientId)

        val editor = sharedPreferences.edit()
        editor.putStringSet(DicomDataServiceBackground.PATIENTS, patients)
        editor.apply()

        messageBroker.requirePatients(*patientId)
    }

    override fun refreshPatient(vararg patientId: String) = runOnIo {
        dataStore.clearData()

        val editor = sharedPreferences.edit()
        editor.putStringSet(DicomDataServiceBackground.PATIENTS, setOf(*patientId))
        editor.apply()

        messageBroker.refreshPatients(*patientId)
    }

    override fun setUrl(url: String) = runOnIo {
        val editor = sharedPreferences.edit()
        editor.putString(DicomDataServiceBackground.WS_URL, url)
        editor.apply()
    }
}