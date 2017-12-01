package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsViewState {
    var seriesList by ObservableStatus(mutableListOf<PatientSeriesModel>())
    var selected: Int by ObservableStatus(0)
    var layoutOption: LayoutOption by ObservableStatus(LayoutOption.ONE_ONE)
    var imageCells: List<ImageFramesViewState> = (0..8).map { ImageFramesViewState(it) }

    fun resetImageStates() {
        imageCells.forEach {
            it.reset()
            it.framesModel = ImageFramesModel()
        }
    }

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}