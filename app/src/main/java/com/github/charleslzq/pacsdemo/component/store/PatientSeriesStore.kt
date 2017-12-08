package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.pacsdemo.component.base.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus

/**
 * Created by charleslzq on 17-12-4.
 */
class PatientSeriesStore(
        val imageFramesStore: ImageFramesStore
) : WithReducer {
    var patientSeriesModel by ObservableStatus(PatientSeriesModel())
        private set
    var selected by ObservableStatus(false)
        private set

    init {
        reduce(this::patientSeriesModel) { state, event ->
            when (event) {
                is BindingEvent.ModelSelected -> {
                    if (imageFramesStore.layoutPosition == 0) {
                        event.patientSeriesModel
                    } else {
                        state
                    }
                }
                is BindingEvent.SeriesListUpdated -> PatientSeriesModel()
                else -> state
            }
        }

        reduce(this::selected) { state, event ->
            when (event) {
                is ClickEvent.ImageCellClicked -> {
                    if (event.layoutPosition == imageFramesStore.layoutPosition) {
                        !state
                    } else {
                        state
                    }
                }
                is BindingEvent.ModelSelected -> false
                is BindingEvent.ModelDropped -> false
                is BindingEvent.SeriesListUpdated -> false
                else -> state
            }
        }
    }
}