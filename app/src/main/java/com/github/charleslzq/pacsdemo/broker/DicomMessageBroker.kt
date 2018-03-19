package com.github.charleslzq.pacsdemo.broker

import com.github.charleslzq.dicom.data.ImageMeta
import com.github.charleslzq.dicom.data.Meta
import com.github.charleslzq.pacsdemo.broker.message.DicomMessageListener

/**
 * Created by charleslzq on 17-11-15
 * 消息代理接口.
 */
interface DicomMessageBroker<P : Meta, T : Meta, E : Meta, I : ImageMeta> {
    fun requirePatients(vararg patientId: String)
    fun refreshPatients(vararg patientId: String)
    fun requireFiles(imageDir: String, fileUris: List<String>)
    fun register(listener: DicomMessageListener<P, T, E, I>)
    fun cancel(listener: DicomMessageListener<P, T, E, I>)
    fun connect()
}