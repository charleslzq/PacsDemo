package com.github.charleslzq.pacsdemo.broker

import android.util.Log
import com.fatboyindustrial.gsonjodatime.Converters
import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.pacsdemo.broker.message.DicomMessageListener
import com.github.charleslzq.pacsdemo.broker.message.Message
import com.github.charleslzq.pacsdemo.broker.message.MessageHeaders
import com.github.charleslzq.pacsdemo.broker.message.ServerMessagePayloadType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomWebsocketClient(
        private val url: String,
        private val dicomMessageListener: DicomMessageListener,
        private val gson: Gson = Converters.registerLocalDateTime(GsonBuilder()).create()
) {
    private var webSocket: WebSocket? = null
    private val logTag = this.javaClass.name
    private val heartBeat = "@heart"

    fun init() {
        val webSocketFuture = AsyncHttpClient.getDefaultInstance().websocket(url, "dicom", this::onComplete)
        webSocket = webSocketFuture.get()
    }

    fun isOpen(): Boolean {
        val result = webSocket?.isOpen
        return result != null && result
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    private fun onComplete(ex: Exception?, webSocket: WebSocket) {
        when (ex) {
            null -> {
                webSocket.setStringCallback { onMessage(it) }
            }
            else -> {
                Log.e(logTag, "Error when connecting $url", ex)
            }
        }
    }

    private fun onMessage(message: String?) {
        if (message != null && message != heartBeat) {
            val anyMessage = gson.fromJson<Message<Any>>(message, Message::class.java)
            val headers = anyMessage.headers
            val type = headers[MessageHeaders.TYPE_HEADER.value]
            if (type != null) {
                when (ServerMessagePayloadType.valueOf(type)) {
                    ServerMessagePayloadType.PATIENT -> {
                        val patientMessage = gson.fromJson<Message<DicomPatient>>(message, object : TypeToken<Message<DicomPatient>>() {
                        }.type)
                        dicomMessageListener.onPatient(patientMessage)
                    }
                    ServerMessagePayloadType.PATIENT_META -> {
                        val patientMessage = gson.fromJson<Message<DicomPatientMetaInfo>>(message, object : TypeToken<Message<DicomPatientMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onPatientMeta(patientMessage)
                    }
                    ServerMessagePayloadType.STUDY_META -> {
                        val studyMessage = gson.fromJson<Message<DicomStudyMetaInfo>>(message, object : TypeToken<Message<DicomStudyMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onStudyMeta(studyMessage)
                    }
                    ServerMessagePayloadType.SERIES_META -> {
                        val seriesMessage = gson.fromJson<Message<DicomSeriesMetaInfo>>(message, object : TypeToken<Message<DicomSeriesMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onSeriesMeta(seriesMessage)
                    }
                    ServerMessagePayloadType.IMAGE_META -> {
                        val imageMessage = gson.fromJson<Message<DicomImageMetaInfo>>(message, object : TypeToken<Message<DicomImageMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onImageMeta(imageMessage)
                    }
                    ServerMessagePayloadType.FILE -> {
                        val fileMessage = gson.fromJson<Message<ByteArray>>(message, object : TypeToken<Message<ByteArray>>() {
                        }.type)
                        dicomMessageListener.onFile(fileMessage)
                    }
                }
            }
        }
    }
}