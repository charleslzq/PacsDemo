package com.github.charleslzq.pacsdemo.broker.message

/**
 * Created by charleslzq on 17-11-14.
 * 消息类,包含消息头和负载
 */
data class Message<T>(
    var headers: MutableMap<String, String> = emptyMap<String, String>().toMutableMap(),
    var payload: T
)

/**
 * 消息头枚举
 */
enum class MessageHeaders(val value: String) {
    CLIENT_ID("clientId"),
    PATIENT_ID("patientId"),
    STUDY_ID("studyId"),
    SERIES_ID("seriesId"),
    IMG_DIR("imageDirectory"),
    TYPE_HEADER("TYPE_HEADER"),
    FILE_DIR("fileDirectory"),
    FILE_NAME("fileName")
}

/**
 * 客户端消息类型
 */
enum class ClientMessagePayloadType {
    PATIENT,
    PATIENT_REFRESH,
    FILE
}

/**
 * 服务器端消息类型
 */
enum class ServerMessagePayloadType {
    PATIENT,
    PATIENT_META,
    STUDY_META,
    SERIES_META,
    IMAGE_META,
    FILE
}