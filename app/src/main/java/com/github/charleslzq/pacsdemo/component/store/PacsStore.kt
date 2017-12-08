package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.pacsdemo.component.base.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsStore : WithReducer {
    var seriesList by ObservableStatus(mutableListOf<PatientSeriesModel>())
        private set
    var selected: Int by ObservableStatus(-1)
        private set
    var layoutOption: LayoutOption by ObservableStatus(LayoutOption.ONE_ONE)
        private set
    val imageCells: List<PatientSeriesStore> = (0..8).map { PatientSeriesStore(ImageFramesStore(it)) }

    init {
        reduce(this::seriesList) { state, event ->
            when (event) {
                is BindingEvent.SeriesListUpdated -> event.seriesList
                else -> state
            }
        }

        reduce(this::selected) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> -1
                is ClickEvent.ThumbListItemClicked -> if (layoutOption == LayoutOption.ONE_ONE) event.position else state
                else -> state
            }
        }

        reduce(this::layoutOption) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> LayoutOption.values()[event.layoutOrdinal]
                else -> state
            }
        }

        reduce(imageCells[0].imageFramesStore::measure, { layoutOption == LayoutOption.ONE_ONE }) { state, event ->
            when (event) {
                is ClickEvent.TurnToMeasureLine -> ImageFramesStore.Measure.LINE
                is ClickEvent.TurnToMeasureAngle -> ImageFramesStore.Measure.ANGEL
                else -> state
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