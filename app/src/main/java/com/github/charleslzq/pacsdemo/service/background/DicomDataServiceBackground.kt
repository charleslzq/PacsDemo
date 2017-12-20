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
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.impl.DicomDataServiceImpl
import com.github.charleslzq.pacsdemo.support.ObservableService
import java.io.File
import java.util.*

class DicomDataServiceBackground : Service() {
    private val logTag = this.javaClass.name
    private var messageBroker: DicomMessageBroker? = null
    private var dataStore: DicomDataStore? = null
    private var sharedPreferces: SharedPreferences? = null
    private lateinit var clientId: String
    private lateinit var patients: Set<String>
    private lateinit var wsUrl: String

    override fun onBind(intent: Intent): IBinder? {
        return ObservableService<DicomDataService> {
            if (messageBroker == null || dataStore == null || sharedPreferces == null) {
                val components = createServiceComponents()
                messageBroker = components.first
                dataStore = components.second
                sharedPreferces = components.third
            }
            DicomDataServiceImpl(messageBroker!!, dataStore!!, sharedPreferces!!)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(logTag, "Service Created")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(logTag, "Service Started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(logTag, "Service destroyed")
        super.onDestroy()
    }

    private fun createServiceComponents(): Triple<DicomMessageBroker, DicomDataStore, SharedPreferences> {
        val preferces = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
        clientId = preferces.getString(CLIENT_ID, UUID.randomUUID().toString().toUpperCase())
        patients = preferces.getStringSet(PATIENTS, setOf("03117795"))
        wsUrl = preferces.getString(WS_URL, "ws://10.0.2.2:8080/pacs")

        val editor = preferces.edit()
        editor.putString(CLIENT_ID, clientId)
        editor.putString(WS_URL, wsUrl)
        editor.putStringSet(PATIENTS, patients)
        editor.apply()

        if (patients.isNotEmpty()) {
            messageBroker?.refreshPatients(*patients.toTypedArray())
        }

        val broker = DicomWebSocketMessageBroker(wsUrl, clientId)
        val saveHandler = RemoteSaveHandler(broker)

        val storeRoot = Environment.getExternalStorageDirectory().absolutePath + STORE_BASE
        val file = File(storeRoot)
        when (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            true -> file.mkdirs()
            false -> {
                Log.e(logTag, "SD Storage Not Mounted, Can't Start This Service")
                stopSelf()
            }
        }
        val store = DicomDataFileStore(storeRoot, saveHandler)

        val messageListener = StoreMessageListener(store)
        broker.register(messageListener)

        return Triple(broker, store, preferces)
    }

    companion object {
        val CLIENT_ID = "clientId"
        val WS_URL = "wsUrl"
        val PATIENTS = "patients"
        val STORE_BASE = "/Pacs/Dicom/Store"
        val SHARED_PREFERENCE = "com.github.charleslzq.pacsdemo"
    }
}
