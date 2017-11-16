package com.github.charleslzq.pacsdemo.service.impl

import android.os.Binder
import com.github.charleslzq.dicom.store.DicomDataStore
import com.github.charleslzq.pacsdemo.broker.DicomMessageBroker
import com.github.charleslzq.pacsdemo.service.DicomDataService

/**
 * Created by charleslzq on 17-11-15.
 */
class DicomDataServiceImpl(
        private val messageBroker: DicomMessageBroker,
        private val dataStore: DicomDataStore
): Binder(), DicomDataService {
    override fun getStore(): DicomDataStore {
        return dataStore
    }
}