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
        reduce(this::patientSeriesModel) {
            when (it.second) {
                is BindingEvent.ModelSelected -> if (imageFramesStore.layoutPosition == 0) (it.second as BindingEvent.ModelSelected).patientSeriesModel else it.first
                is BindingEvent.SeriesListUpdated -> PatientSeriesModel()
                else -> it.first
            }
        }

        reduce(this::selected) {
            when (it.second) {
                is ClickEvent.ImageCellClicked -> if ((it.second as ClickEvent.ImageCellClicked).layoutPosition == imageFramesStore.layoutPosition) !it.first else it.first
                is BindingEvent.ModelSelected -> false
                is BindingEvent.ModelDropped -> false
                is BindingEvent.SeriesListUpdated -> false
                else -> it.first
            }
        }
    }
}