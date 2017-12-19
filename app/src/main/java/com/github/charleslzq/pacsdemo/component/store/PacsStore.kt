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
        reduce(PacsStore::seriesList) {
            on<BindingEvent.SeriesListUpdated> { event.seriesList }
        }

        reduce(PacsStore::selected) {
            on<ClickEvent.ChangeLayout> { -1 }
            on<BindingEvent.SeriesListUpdated> { -1 }
            on<ClickEvent.ThumbListItemClicked>(precondition = { layoutOption == LayoutOption.ONE_ONE }) {
                event.position
            }
        }

        reduce(PacsStore::layoutOption) {
            on<ClickEvent.ChangeLayout> { LayoutOption.values()[event.layoutOrdinal] }
        }
    }

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}