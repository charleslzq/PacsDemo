package com.github.charleslzq.pacsdemo.broker.message

/**
 * Created by charleslzq on 17-11-14.
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