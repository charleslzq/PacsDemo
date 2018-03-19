package com.github.charleslzq.pacsdemo.broker

import com.fatboyindustrial.gsonjodatime.Converters
import com.github.charleslzq.dicom.data.ImageMeta
import com.github.charleslzq.dicom.data.Meta
import com.github.charleslzq.pacsdemo.broker.message.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Created by charleslzq on 17-11-15.
 * 消息代理websocket实现
 */
class DicomWebSocketMessageBroker<P : Meta, T : Meta, E : Meta, I : ImageMeta>(
    url: String,
    private val clientId: String,
    private val gson: Gson = Converters.registerLocalDateTime(GsonBuilder()).create()
) : DicomMessageBroker<P, T, E, I> {
    private val dicomMessageListener = CompositeDicomMessageListener<P, T, E, I>()
    private val dicomClient = DicomWebSocketClient(url, dicomMessageListener, gson)

    override fun requirePatients(vararg patientId: String) {
        val headers = mapOf(
            MessageHeaders.CLIENT_ID.value to clientId,
            MessageHeaders.TYPE_HEADER.value to ClientMessagePayloadType.PATIENT.name
        ).toMutableMap()
        val idList = listOf(*patientId)
        val message = Message(headers, idList)
        connect()
        dicomClient.send(gson.toJson(message))
    }

    override fun refreshPatients(vararg patientId: String) {
        val headers = mapOf(
            MessageHeaders.CLIENT_ID.value to clientId,
            MessageHeaders.TYPE_HEADER.value to ClientMessagePayloadType.PATIENT_REFRESH.name
        ).toMutableMap()
        val idList = listOf(*patientId)
        val message = Message(headers, idList)
        connect()
        dicomClient.send(gson.toJson(message))
    }

    override fun requireFiles(imageDir: String, fileUris: List<String>) {
        val headers = mapOf(
            MessageHeaders.TYPE_HEADER.value to ClientMessagePayloadType.FILE.name,
            MessageHeaders.IMG_DIR.value to imageDir
        ).toMutableMap()
        val message = Message(headers, fileUris)
        connect()
        dicomClient.send(gson.toJson(message))
    }

    override fun register(listener: DicomMessageListener<P, T, E, I>) {
        dicomMessageListener.register(listener)
    }

    override fun cancel(listener: DicomMessageListener<P, T, E, I>) {
        dicomMessageListener.cancel(listener)
    }

    override fun connect() {
        if (!dicomClient.isOpen()) {
            dicomClient.connect()
        }
    }
}