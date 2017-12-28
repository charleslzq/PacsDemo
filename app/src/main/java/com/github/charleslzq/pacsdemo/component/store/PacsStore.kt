package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.MiddleWare
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsStore : Store<PacsStore>(MiddleWare.debugLog, buildThunk<PacsStore>()), RxScheduleSupport {
    var seriesList by ObservableStatus(mutableListOf<PatientSeriesModel>())
        private set
    var selected: Int by ObservableStatus(-1)
        private set
    var layoutOption: LayoutOption by ObservableStatus(LayoutOption.ONE_ONE)
        private set
    val imageCells: List<PatientSeriesStore> = callOnIo { (0..8).map { PatientSeriesStore(ImageFramesStore(it)) } }

    init {
        reduce(PacsStore::seriesList) {
            on<SeriesListUpdated> { event.seriesList }
        }

        reduce(PacsStore::selected) {
            on<ChangeLayout> { -1 }
            on<SeriesListUpdated> { -1 }
            on<ThumbListItemClicked>(precondition = { layoutOption == LayoutOption.ONE_ONE }) {
                event.position
            }
        }

        reduce(PacsStore::layoutOption) {
            on<ChangeLayout> { LayoutOption.values()[event.layoutOrdinal] }
        }
    }

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }

    data class SeriesListUpdated(val seriesList: MutableList<PatientSeriesModel>)
    data class ChangeLayout(val layoutOrdinal: Int)
    data class ThumbListItemClicked(val position: Int)
}