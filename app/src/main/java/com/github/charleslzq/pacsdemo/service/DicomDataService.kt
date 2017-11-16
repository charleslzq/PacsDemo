package com.github.charleslzq.pacsdemo.service

import com.github.charleslzq.dicom.store.DicomDataStore

/**
 * Created by charleslzq on 17-11-16.
 */
interface DicomDataService {
    fun getStore(): DicomDataStore
}