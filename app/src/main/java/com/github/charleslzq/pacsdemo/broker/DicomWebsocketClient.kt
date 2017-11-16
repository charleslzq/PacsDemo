package com.github.charleslzq.pacsdemo.broker

import android.util.Log
import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.pacsdemo.broker.message.DicomMessageListener
import com.github.charleslzq.pacsdemo.broker.message.Message
import com.github.charleslzq.pacsdemo.broker.message.MessageHeaders
import com.github.charleslzq.pacsdemo.broker.message.PayloadType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomWebsocketClient(
        private val url: String,
        private val dicomMessageListener: DicomMessageListener,
        private val gson: Gson = Gson()
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
                when (PayloadType.valueOf(type)) {
                    PayloadType.PATIENT -> {
                        val patientMessage = gson.fromJson<Message<DicomPatient>>(message, object : TypeToken<Message<DicomPatient>>() {
                        }.type)
                        dicomMessageListener.onPatient(patientMessage)
                    }
                    PayloadType.PATIENT_META -> {
                        val patientMessage = gson.fromJson<Message<DicomPatientMetaInfo>>(message, object : TypeToken<Message<DicomPatientMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onPatientMeta(patientMessage)
                    }
                    PayloadType.PATIENT_REFRESH -> Log.i(logTag, "Unexpected message type: ${PayloadType.PATIENT_REFRESH}")
                    PayloadType.STUDY -> {
                        val studyMessage = gson.fromJson<Message<DicomStudy>>(message, object : TypeToken<Message<DicomStudy>>() {
                        }.type)
                        dicomMessageListener.onStudy(studyMessage)
                    }
                    PayloadType.STUDY_META -> {
                        val studyMessage = gson.fromJson<Message<DicomStudyMetaInfo>>(message, object : TypeToken<Message<DicomStudyMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onStudyMeta(studyMessage)
                    }
                    PayloadType.SERIES -> {
                        val seriesMessage = gson.fromJson<Message<DicomSeries>>(message, object : TypeToken<Message<DicomSeries>>() {
                        }.type)
                        dicomMessageListener.onSeries(seriesMessage)
                    }
                    PayloadType.SERIES_META -> {
                        val seriesMessage = gson.fromJson<Message<DicomSeriesMetaInfo>>(message, object : TypeToken<Message<DicomSeriesMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onSeriesMeta(seriesMessage)
                    }
                    PayloadType.IMAGE_META -> {
                        val imageMessage = gson.fromJson<Message<DicomImageMetaInfo>>(message, object : TypeToken<Message<DicomImageMetaInfo>>() {
                        }.type)
                        dicomMessageListener.onImageMeta(imageMessage)
                    }
                    PayloadType.FILE -> {
                        val fileMessage = gson.fromJson<Message<ByteArray>>(message, object : TypeToken<Message<ByteArray>>() {
                        }.type)
                        dicomMessageListener.onFile(fileMessage)
                    }
                    PayloadType.OTHER -> Log.i(logTag, "Don't know how to handle this message")
                }
            }
        }
    }
}