package com.github.charleslzq.pacsdemo.service.background

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.github.charleslzq.dicom.store.DicomDataFileStore
import com.github.charleslzq.dicom.store.DicomDataStore
import com.github.charleslzq.pacsdemo.broker.DicomMessageBroker
import com.github.charleslzq.pacsdemo.broker.DicomWebSocketMessageBroker
import com.github.charleslzq.pacsdemo.broker.RemoteSaveHandler
import com.github.charleslzq.pacsdemo.broker.message.StoreMessageListener
import com.github.charleslzq.pacsdemo.service.impl.DicomDataServiceImpl
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import java.io.File
import java.util.*

class DicomDataServiceBackground : Service(), RxScheduleSupport {
    private val logTag = this.javaClass.name
    private lateinit var messageBroker: DicomMessageBroker
    private lateinit var dataStore: DicomDataStore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clientId: String
    private lateinit var patients: Set<String>
    private lateinit var wsUrl: String

    override fun onBind(intent: Intent): IBinder? {
        return DicomDataServiceImpl(messageBroker, dataStore, sharedPreferences)
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = callOnIo {
            getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
        }

        callOnIo {
            Triple(
                    sharedPreferences.getString(CLIENT_ID, UUID.randomUUID().toString().toUpperCase()),
                    sharedPreferences.getStringSet(PATIENTS, setOf("03117795")),
                    sharedPreferences.getString(WS_URL, "ws://10.0.2.2:8080/pacs")
            )
        }.apply {
            clientId = first
            patients = second
            wsUrl = third
        }

        runOnIo {
            val editor = sharedPreferences.edit()
            editor.putString(CLIENT_ID, clientId)
            editor.putString(WS_URL, wsUrl)
            editor.putStringSet(PATIENTS, patients)
            editor.apply()
        }

        messageBroker = DicomWebSocketMessageBroker(wsUrl, clientId)
        if (patients.isNotEmpty()) {
            messageBroker.refreshPatients(*patients.toTypedArray())
        }
        val saveHandler = RemoteSaveHandler(messageBroker)

        dataStore = callOnIo {
            val storeRoot = Environment.getExternalStorageDirectory().absolutePath + STORE_BASE
            val file = File(storeRoot)
            when (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                true -> file.mkdirs()
                false -> {
                    Log.e(logTag, "SD Storage Not Mounted, Can't Start This Service")
                    stopSelf()
                }
            }
            DicomDataFileStore(storeRoot, saveHandler)
        }

        messageBroker.register(StoreMessageListener(dataStore))

        Log.i(logTag, "Service Created")
    }

    companion object {
        val CLIENT_ID = "clientId"
        val WS_URL = "wsUrl"
        val PATIENTS = "patients"
        val STORE_BASE = "/Pacs/Dicom/Store"
        val SHARED_PREFERENCE = "com.github.charleslzq.pacsdemo"
    }
}
