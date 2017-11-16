package com.github.charleslzq.pacsdemo.broker.message

/**
 * Created by charleslzq on 17-11-14.
 */
enum class PayloadType {
    PATIENT,
    PATIENT_META,
    PATIENT_REFRESH,
    STUDY,
    STUDY_META,
    SERIES,
    SERIES_META,
    IMAGE_META,
    FILE,
    OTHER
}