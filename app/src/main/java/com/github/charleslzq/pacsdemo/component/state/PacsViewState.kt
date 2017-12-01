package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsViewState {
    var seriesList by ObservablePropertyWithObservers(mutableListOf<PatientSeriesModel>())
    var selected: Int by ObservablePropertyWithObservers(0)
    var layoutOption: LayoutOption by ObservablePropertyWithObservers(LayoutOption.ONE_ONE)
    var imageCells: List<ImageFramesViewState> = (1..9).map { ImageFramesViewState() }

    fun resetImageStates() {
        imageCells.forEach { it.reset() }
    }

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}