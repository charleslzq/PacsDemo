package com.github.charleslzq.pacsdemo.broker

import com.github.charleslzq.pacsdemo.broker.message.DicomMessageListener

/**
 * Created by charleslzq on 17-11-15.
 */
interface DicomMessageBroker {
    fun requirePatients(vararg patientId: String)
    fun refreshPatients(vararg patientId: String)
    fun requireFiles(imageDir: String, fileUris: List<String>)
    fun register(listener: DicomMessageListener)
    fun cancel(listener: DicomMessageListener)
    fun connect()
}