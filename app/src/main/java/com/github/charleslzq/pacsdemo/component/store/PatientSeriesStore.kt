package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.MiddleWare

/**
 * Created by charleslzq on 17-12-4.
 */
class PatientSeriesStore(
        val imageFramesStore: ImageFramesStore
) : Store<PatientSeriesStore>(MiddleWare.debugLog, buildThunk<PatientSeriesStore>()) {
    var patientSeriesModel by ObservableStatus(PatientSeriesModel())
        private set

    init {
        reduce(PatientSeriesStore::patientSeriesModel) {
            on<ModelDropped> {
                event.patientSeriesModel
            }
        }
    }

    data class ModelDropped(val patientSeriesModel: PatientSeriesModel)
}