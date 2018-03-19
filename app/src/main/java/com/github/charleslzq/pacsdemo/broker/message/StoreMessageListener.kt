package com.github.charleslzq.pacsdemo.broker.message

import com.github.charleslzq.dicom.data.*
import com.github.charleslzq.dicom.store.DicomDataStore
import java.io.File

/**
 * Created by charleslzq on 17-11-15.
 * 将服务器端发过来的dicom消息的负载保存下来
 * @param dicomDataStore 本地dicom数据存储库
 */
class StoreMessageListener<P : Meta, T : Meta, E : Meta, I : ImageMeta>(
        private val dicomDataStore: DicomDataStore<P, T, E, I>
) : DicomMessageListener<P, T, E, I> {

    override fun onPatient(dicomPatientMessage: Message<DicomPatient<P, T, E, I>>) =
        dicomDataStore.savePatient(dicomPatientMessage.payload)

    override fun onPatientMeta(dicomPatientMetaInfoMessage: Message<P>) =
        dicomDataStore.savePatientMeta(dicomPatientMetaInfoMessage.payload)

    override fun onStudyMeta(dicomStudyMetaInfoMessage: Message<T>) =
        dicomDataStore.saveStudyMeta(
            checkAndGet(
                dicomStudyMetaInfoMessage.headers,
                MessageHeaders.PATIENT_ID
            ), dicomStudyMetaInfoMessage.payload
        )

    override fun onSeriesMeta(dicomSeriesMetaInfoMessage: Message<E>) =
        dicomDataStore.saveSeriesMeta(
            checkAndGet(dicomSeriesMetaInfoMessage.headers, MessageHeaders.PATIENT_ID),
            checkAndGet(dicomSeriesMetaInfoMessage.headers, MessageHeaders.STUDY_ID),
            dicomSeriesMetaInfoMessage.payload
        )

    override fun onImageMeta(dicomImageMetaInfoMessage: Message<I>) =
        dicomDataStore.saveImage(
            checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.PATIENT_ID),
            checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.STUDY_ID),
            checkAndGet(dicomImageMetaInfoMessage.headers, MessageHeaders.SERIES_ID),
            dicomImageMetaInfoMessage.payload
        )

    override fun onFile(byteArrayMessage: Message<ByteArray>) {
        val fileDir = checkAndGet(byteArrayMessage.headers, MessageHeaders.FILE_DIR)
        val fileName = checkAndGet(byteArrayMessage.headers, MessageHeaders.FILE_NAME)
        val file = File(fileDir + File.separator + fileName)
        val content = byteArrayMessage.payload
        file.writeBytes(content)
    }

    private fun checkAndGet(headers: Map<String, String>, key: MessageHeaders) = headers[key.value]
            ?: throw IllegalArgumentException("Required header ${key.value} not found")
}