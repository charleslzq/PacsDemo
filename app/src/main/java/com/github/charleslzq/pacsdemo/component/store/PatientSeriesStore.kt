package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.MiddleWare

/**
 * Created by charleslzq on 17-12-4.
 */
class PatientSeriesStore(
        val imageFramesStore: ImageFramesStore
) : Store<PatientSeriesStore>(MiddleWare.debugLog, thunk) {
    var patientSeriesModel by ObservableStatus(PatientSeriesModel())
        private set

    init {
        reduce(PatientSeriesStore::patientSeriesModel) {
            on<ModelDropped> {
                event.patientSeriesModel
            }
        }

//        reduce(PatientSeriesStore::hideMeta) {
//            on<ClickEvent.ImageClicked> {
//                !state
//            }
//            on<BindingEvent.ModelDropped> {
//                false
//            }
//        }
    }

    data class ModelDropped(val patientSeriesModel: PatientSeriesModel)
}