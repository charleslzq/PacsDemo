package com.github.charleslzq.pacsdemo.broker

import android.util.Log
import com.fatboyindustrial.gsonjodatime.Converters
import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.pacsdemo.broker.message.DicomMessageListener
import com.github.charleslzq.pacsdemo.broker.message.Message
import com.github.charleslzq.pacsdemo.broker.message.MessageHeaders
import com.github.charleslzq.pacsdemo.broker.message.ServerMessagePayloadType
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomWebSocketClient(
        private var url: String,
        private val dicomMessageListener: DicomMessageListener,
        private val gson: Gson = Converters.registerLocalDateTime(GsonBuilder()).create()
) : RxScheduleSupport {
    private var webSocket: WebSocket? = null
    private val logTag = this.javaClass.name
    private val heartBeat = "@heart"

    fun setUrl(newUrl: String) {
        url = newUrl
        connect()
    }

    fun connect() {
        runOnIo {
            AsyncHttpClient.getDefaultInstance().websocket(url, "dicom", this::onComplete)
        }
    }

    fun isOpen(): Boolean {
        return webSocket?.isOpen ?: false
    }

    fun send(message: String) {
        runOnIo {
            webSocket?.send(message)
        }
    }

    private fun onComplete(ex: Exception?, webSocket: WebSocket?) {
        when (ex == null && webSocket != null) {
            true -> {
                webSocket!!.setStringCallback { onMessage(it) }
                this.webSocket = webSocket
            }
            false -> {
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
                    ServerMessagePayloadType.PATIENT -> dicomMessageListener.onPatient(toObject(message))
                    ServerMessagePayloadType.PATIENT_META -> dicomMessageListener.onPatientMeta(toObject(message))
                    ServerMessagePayloadType.STUDY_META -> dicomMessageListener.onStudyMeta(toObject(message))
                    ServerMessagePayloadType.SERIES_META -> dicomMessageListener.onSeriesMeta(toObject(message))
                    ServerMessagePayloadType.IMAGE_META -> dicomMessageListener.onImageMeta(toObject(message))
                    ServerMessagePayloadType.FILE -> dicomMessageListener.onFile(toObject(message))
                }
            }
        }
    }

    private inline fun <reified T> toObject(message: String) =
            gson.fromJson<T>(message, object : TypeToken<T>(){}.type)
}