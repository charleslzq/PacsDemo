package com.github.charleslzq.pacsdemo.broker

import android.util.Log
import com.fatboyindustrial.gsonjodatime.Converters
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
    private val url: String,
    private val dicomMessageListener: DicomMessageListener,
    private val gson: Gson = Converters.registerLocalDateTime(GsonBuilder()).create()
) : RxScheduleSupport {
    private var webSocket: WebSocket? = null
    private val logTag = javaClass.name
    private val heartBeat = "@heart"

    fun connect() = runOnIo {
        AsyncHttpClient.getDefaultInstance().websocket(url, "dicom", ::onComplete)
    }

    fun isOpen() = webSocket?.isOpen ?: false

    fun send(message: String) = runOnIo {
        webSocket?.send(message)
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
            gson.fromJson<Message<Any>>(
                message,
                Message::class.java
            ).headers[MessageHeaders.TYPE_HEADER.value]?.let {
                when (ServerMessagePayloadType.valueOf(it)) {
                    ServerMessagePayloadType.PATIENT -> dicomMessageListener.onPatient(
                        toObject(message)
                    )
                    ServerMessagePayloadType.PATIENT_META -> dicomMessageListener.onPatientMeta(
                        toObject(message)
                    )
                    ServerMessagePayloadType.STUDY_META -> dicomMessageListener.onStudyMeta(
                        toObject(message)
                    )
                    ServerMessagePayloadType.SERIES_META -> dicomMessageListener.onSeriesMeta(
                        toObject(message)
                    )
                    ServerMessagePayloadType.IMAGE_META -> dicomMessageListener.onImageMeta(
                        toObject(message)
                    )
                    ServerMessagePayloadType.FILE -> dicomMessageListener.onFile(toObject(message))
                }
            }
        }
    }

    private inline fun <reified M> toObject(message: String) =
        gson.fromJson<M>(message, object : TypeToken<M>() {}.type)
}