package com.github.charleslzq.pacsdemo.broker.message

import com.github.charleslzq.dicom.data.*

/**
 * Created by charleslzq on 17-11-15.
 * 处理服务器端发过来的dicom消息的接口
 */
interface DicomMessageListener<P : Meta, T : Meta, E : Meta, I : ImageMeta> {
    fun onPatient(dicomPatientMessage: Message<DicomPatient<P, T, E, I>>)
    fun onPatientMeta(dicomPatientMetaInfoMessage: Message<P>)
    fun onStudyMeta(dicomStudyMetaInfoMessage: Message<T>)
    fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<E>)
    fun onImageMeta(dicomImageMetaInfoMessage: Message<I>)
    fun onFile(byteArrayMessage: Message<ByteArray>)
}