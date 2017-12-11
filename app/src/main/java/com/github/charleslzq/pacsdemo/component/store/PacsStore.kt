package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsStore : WithReducer<PacsStore> {
    var seriesList by ObservableStatus(mutableListOf<PatientSeriesModel>())
        private set
    var selected: Int by ObservableStatus(-1)
        private set
    var layoutOption: LayoutOption by ObservableStatus(LayoutOption.ONE_ONE)
        private set
    val imageCells: List<PatientSeriesStore> = (0..8).map { PatientSeriesStore(ImageFramesStore(it)) }

    init {
        reduce(PacsStore::seriesList) { state, event ->
            when (event) {
                is BindingEvent.SeriesListUpdated -> event.seriesList
                else -> state
            }
        }

        reduce(PacsStore::selected) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> -1
                is ClickEvent.ThumbListItemClicked -> if (layoutOption == LayoutOption.ONE_ONE) event.position else state
                else -> state
            }
        }

        reduce(PacsStore::layoutOption) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> LayoutOption.values()[event.layoutOrdinal]
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