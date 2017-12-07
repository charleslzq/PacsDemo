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
import java.io.File
import java.util.*

class DicomDataServiceBackgroud : Service() {
    private val logTag = this.javaClass.name
    private lateinit var messageBroker: DicomMessageBroker
    private lateinit var dataStore: DicomDataStore
    private lateinit var sharedPreferces: SharedPreferences
    private lateinit var clientId: String
    private lateinit var patients: Set<String>
    private lateinit var wsUrl: String

    override fun onBind(intent: Intent): IBinder? {
        return DicomDataServiceImpl(messageBroker, dataStore, sharedPreferces)
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferces = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
        clientId = sharedPreferces.getString(CLIENT_ID, UUID.randomUUID().toString().toUpperCase())
        patients = sharedPreferces.getStringSet(PATIENTS, setOf("03117795"))
        wsUrl = sharedPreferces.getString(WS_URL, "ws://10.0.2.2:8080/pacs")

        val editor = sharedPreferces.edit()
        editor.putString(CLIENT_ID, clientId)
        editor.putString(WS_URL, wsUrl)
        editor.putStringSet(PATIENTS, patients)
        editor.apply()

        messageBroker = DicomWebSocketMessageBroker(wsUrl, clientId)
        val saveHandler = RemoteSaveHandler(messageBroker)

        val storeRoot = Environment.getExternalStorageDirectory().absolutePath + STORE_BASE
        val file = File(storeRoot)
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !file.exists() || !file.isDirectory) {
            file.mkdirs()
        }
        when (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            true -> file.mkdirs()
            false -> {
                Log.e(logTag, "SD Storage Not Mounted, Can't Start This Service")
                stopSelf()
            }
        }
        dataStore = DicomDataFileStore(storeRoot, saveHandler)

        val messageListener = StoreMessageListener(dataStore)
        messageBroker.register(messageListener)

        Log.i(logTag, "Service Created")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(logTag, "Service Started")
        if (patients.isNotEmpty()) {
            messageBroker.refreshPatients(*patients.toTypedArray())
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(logTag, "Service destroyed")
        super.onDestroy()
    }

    companion object {
        val CLIENT_ID = "clientId"
        val WS_URL = "wsUrl"
        val PATIENTS = "patients"
        val STORE_BASE = "/Pacs/Dicom/Store"
        val SHARED_PREFERENCE = "com.github.charleslzq.pacsdemo"
    }
}
