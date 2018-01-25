package com.github.charleslzq.pacsdemo.service

import com.github.charleslzq.dicom.data.*

/**
 * Created by charleslzq on 17-11-16.
 * 本地dicom数据服务接口
 */
interface DicomDataService {
    /**
     * @param url 服务器端websocket端点
     */
    fun setUrl(url: String)

    /**
     * 向服务器端请求对应patient的dicom图像数据
     * @param patientId 病人id列表
     */
    fun requirePatients(vararg patientId: String)

    /**
     * 删除本地所有数据并向服务器端请求对应patient的dicom图像数据, 会重置服务器端订阅列表
     * @param patientId 病人id列表
     */
    fun refreshPatient(vararg patientId: String)

    /**
     * 在本地数据中寻找对应病人的dicom数据
     * @param patientId 病人id
     */
    fun findPatient(patientId: String): DicomPatient<DicomPatientMetaInfo, DicomStudyMetaInfo, DicomSeriesMetaInfo, DicomImageMetaInfo>?
}