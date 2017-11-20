package com.github.charleslzq.pacsdemo.broker.message

/**
 * Created by charleslzq on 17-11-14.
 */
enum class ServerMessagePayloadType {
    PATIENT,
    PATIENT_META,
    STUDY_META,
    SERIES_META,
    IMAGE_META,
    FILE
}