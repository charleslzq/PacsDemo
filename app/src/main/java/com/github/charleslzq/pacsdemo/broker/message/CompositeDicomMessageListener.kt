package com.github.charleslzq.pacsdemo.broker.message

import com.github.charleslzq.dicom.data.*

/**
 * Created by charleslzq on 17-11-15.
 * 将多个DicomMessageListener合成一个
 */
class CompositeDicomMessageListener<P : Meta, T : Meta, E : Meta, I : ImageMeta> : DicomMessageListener<P, T, E, I> {
    private val listeners: MutableList<DicomMessageListener<P, T, E, I>> =
        emptyList<DicomMessageListener<P, T, E, I>>().toMutableList()

    fun register(listener: DicomMessageListener<P, T, E, I>) = listeners.add(listener)

    fun cancel(listener: DicomMessageListener<P, T, E, I>) = listeners.remove(listener)

    override fun onPatient(dicomPatientMessage: Message<DicomPatient<P, T, E, I>>) =
        listeners.forEach { it.onPatient(dicomPatientMessage) }

    override fun onPatientMeta(dicomPatientMetaInfoMessage: Message<P>) =
        listeners.forEach { it.onPatientMeta(dicomPatientMetaInfoMessage) }

    override fun onStudyMeta(dicomStudyMetaInfoMessage: Message<T>) =
        listeners.forEach { it.onStudyMeta(dicomStudyMetaInfoMessage) }

    override fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<E>) =
        listeners.forEach { it.onSeriesMeta(dicomSeriesMetaInfoMessage) }

    override fun onImageMeta(dicomImageMetaInfoMessage: Message<I>) =
        listeners.forEach { it.onImageMeta(dicomImageMetaInfoMessage) }

    override fun onFile(byteArrayMessage: Message<ByteArray>) =
        listeners.forEach { it.onFile(byteArrayMessage) }
}