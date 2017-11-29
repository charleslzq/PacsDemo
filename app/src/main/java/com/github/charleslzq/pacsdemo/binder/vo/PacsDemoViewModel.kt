package com.github.charleslzq.pacsdemo.binder.vo

import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers

/**
 * Created by charleslzq on 17-11-27.
 */
data class PacsDemoViewModel(
        var seriesList: MutableList<PatientSeriesViewModel> = emptyList<PatientSeriesViewModel>().toMutableList()
) {
    var selected: Int by ObservablePropertyWithObservers(0)
    var layoutOption: LayoutOption by ObservablePropertyWithObservers(LayoutOption.ONE_ONE)
    var imageCells: MutableList<ImageFramesViewModel> = emptyList<ImageFramesViewModel>().toMutableList()

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}