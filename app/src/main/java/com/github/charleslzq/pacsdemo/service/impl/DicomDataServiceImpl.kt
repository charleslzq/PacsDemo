package com.github.charleslzq.pacsdemo.service.impl

import android.content.SharedPreferences
import android.os.Binder
import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.dicom.store.DicomDataStore
import com.github.charleslzq.pacsdemo.broker.DicomMessageBroker
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackground
import com.github.charleslzq.pacsdemo.support.MemCache
import com.github.charleslzq.pacsdemo.support.callOnIo
import com.github.charleslzq.pacsdemo.support.runOnIo

/**
 * Created by charleslzq on 17-11-15.
 * 本地dicom数据服务实现
 * @param messageBroker 本地消息代理
 * @param dataStore 本地数据存储服务
 * @param sharedPreferences android共享数据库,存储clientId等
 */
class DicomDataServiceImpl(
    private val messageBroker: DicomMessageBroker,
    private val dataStore: DicomDataStore<DicomPatientMetaInfo, DicomStudyMetaInfo, DicomSeriesMetaInfo, DicomImageMetaInfo>,
    private val sharedPreferences: SharedPreferences
) : Binder(), DicomDataService {
    private val cache = MemCache(DicomPatient::class.java, 5)

    @Suppress("UNCHECKED_CAST")
    override fun findPatient(patientId: String) = cache.load(patientId) {
        callOnIo {
            listOfNotNull(dataStore.getPatient(patientId)).apply {
                if (isEmpty()) {
                    requirePatients(patientId)
                }
            }
        }.firstOrNull()
    } as? DicomPatient<DicomPatientMetaInfo, DicomStudyMetaInfo, DicomSeriesMetaInfo, DicomImageMetaInfo>

    override fun requirePatients(vararg patientId: String) = runOnIo {
        val patients =
            sharedPreferences.getStringSet(DicomDataServiceBackground.PATIENTS, emptySet())
                .toMutableSet()
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