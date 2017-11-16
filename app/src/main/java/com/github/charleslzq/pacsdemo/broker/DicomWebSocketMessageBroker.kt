package com.github.charleslzq.pacsdemo.broker

import com.github.charleslzq.pacsdemo.broker.message.*
import com.google.gson.Gson

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomWebSocketMessageBroker(
        url: String,
        private val clientId: String,
        private val gson: Gson = Gson()
) : DicomMessageBroker {
    private val dicomMessageListener = CompositeDicomMessageListener()
    private val dicomClient = DicomWebsocketClient(url, dicomMessageListener, gson)

    override fun requirePatients(vararg patientId: String) {
        val headers = mapOf<String, String>(
                MessageHeaders.CLIENT_ID.value to clientId,
                MessageHeaders.TYPE_HEADER.value to PayloadType.PATIENT.name
        ).toMutableMap()
        val idList = listOf(*patientId)
        val message = Message(headers, idList)
        connect()
        dicomClient.send(gson.toJson(message))
    }

    override fun refreshPatients(vararg patientId: String) {
        val headers = mapOf<String, String>(
                MessageHeaders.CLIENT_ID.value to clientId,
                MessageHeaders.TYPE_HEADER.value to PayloadType.PATIENT_REFRESH.name
        ).toMutableMap()
        val idList = listOf(*patientId)
        val message = Message(headers, idList)
        connect()
        dicomClient.send(gson.toJson(message))
    }

    override fun requireFiles(imageDir: String, fileUris: List<String>) {
        val headers = mapOf<String, String>(
                MessageHeaders.TYPE_HEADER.value to PayloadType.FILE.name,
                MessageHeaders.IMG_DIR.value to imageDir
        ).toMutableMap()
        val message = Message(headers, fileUris)
        connect()
        dicomClient.send(gson.toJson(message))
    }

    override fun register(listener: DicomMessageListener) {
        dicomMessageListener.register(listener)
    }

    override fun cancel(listener: DicomMessageListener) {
        dicomMessageListener.cancel(listener)
    }

    override fun connect() {
        if (!dicomClient.isOpen()) {
            dicomClient.init()
        }
    }
}