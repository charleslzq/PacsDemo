package com.github.charleslzq.pacsdemo.broker

import android.util.Log
import com.fatboyindustrial.gsonjodatime.Converters
import com.github.charleslzq.dicom.data.ImageMeta
import com.github.charleslzq.dicom.data.Meta
import com.github.charleslzq.pacsdemo.broker.message.DicomMessageListener
import com.github.charleslzq.pacsdemo.broker.message.Message
import com.github.charleslzq.pacsdemo.broker.message.MessageHeaders
import com.github.charleslzq.pacsdemo.broker.message.ServerMessagePayloadType
import com.github.charleslzq.pacsdemo.support.runOnIo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket

/**
 * Created by charleslzq on 17-11-15.
 * websocket客户端
 * @param url 服务器端websocket端点
 * @param dicomMessageListener 消息处理器
 * @param gson 将消息转换成对象
 */
class DicomWebSocketClient<P : Meta, T : Meta, E : Meta, I : ImageMeta>(
    private val url: String,
    private val dicomMessageListener: DicomMessageListener<P, T, E, I>,
    private val gson: Gson = Converters.registerLocalDateTime(GsonBuilder()).create()
) {
    private var webSocket: WebSocket? = null
    private val logTag = javaClass.name
    private val heartBeat = "@heart"

    /**
     * 连接服务器
     */
    fun connect() = runOnIo {
        AsyncHttpClient.getDefaultInstance().websocket(url, "dicom", ::onComplete)
    }

    /**
     * 是否连接到了服务器
     */
    fun isOpen() = webSocket?.isOpen ?: false

    /**
     * 发送消息,未连接是没有任何操作
     */
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