package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.pacsdemo.component.base.Store
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsStore : Store<PacsStore>() {
    var seriesList by ObservableStatus(mutableListOf<PatientSeriesModel>())
        private set
    var selected: Int by ObservableStatus(-1)
        private set
    var layoutOption: LayoutOption by ObservableStatus(LayoutOption.ONE_ONE)
        private set
    val imageCells: List<PatientSeriesStore> = (0..8).map { PatientSeriesStore(ImageFramesStore(it)) }

    init {
        reduce(this::seriesList) {
            when (it.second) {
                is BindingEvent.SeriesListUpdated -> (it.second as BindingEvent.SeriesListUpdated).seriesList
                else -> it.first
            }
        }

        reduce(imageCells[0].imageFramesStore::measure, { layoutOption == LayoutOption.ONE_ONE }) {
            when (it.second) {
                is ClickEvent.TurnToMeasureLine -> ImageFramesStore.Measure.LINE
                is ClickEvent.TurnToMeasureAngle -> ImageFramesStore.Measure.ANGEL
                else -> it.first
            }
        }

        reduce(this::selected, { layoutOption == LayoutOption.ONE_ONE }) {
            when (it.second) {
                is ClickEvent.ThumbListItemClicked -> (it.second as ClickEvent.ThumbListItemClicked).position
                else -> it.first
            }
        }
    }

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}