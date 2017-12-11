package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent

/**
 * Created by charleslzq on 17-12-4.
 */
class PatientSeriesStore(
        val imageFramesStore: ImageFramesStore
) : WithReducer<PatientSeriesStore> {
    var patientSeriesModel by ObservableStatus(PatientSeriesModel())
        private set
    var selected by ObservableStatus(false)
        private set
    var selectable = false
        private set

    init {
        reduce(PatientSeriesStore::patientSeriesModel) { state, event ->
            when (event) {
                is BindingEvent.ModelSelected -> {
                    if (imageFramesStore.layoutPosition == 0) {
                        event.patientSeriesModel
                    } else {
                        state
                    }
                }
                is BindingEvent.ModelDropped -> {
                    if (event.layoutPosition == imageFramesStore.layoutPosition) {
                        event.patientSeriesModel
                    } else {
                        state
                    }
                }
                is BindingEvent.SeriesListUpdated -> PatientSeriesModel()
                else -> state
            }
        }

        reduce(PatientSeriesStore::selectable) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> event.layoutOrdinal != 0
                else -> state
            }
        }

        reduce(
                property = PatientSeriesStore::selected,
                guard = { selectable }
        ) { state, event ->
            when (event) {
                is ClickEvent.ImageCellClicked -> {
                    if (event.layoutPosition == imageFramesStore.layoutPosition) {
                        !state
                    } else {
                        state
                    }
                }
                is BindingEvent.ModelSelected, is BindingEvent.ModelDropped, is BindingEvent.SeriesListUpdated -> false
                else -> state
            }
        }
    }
}