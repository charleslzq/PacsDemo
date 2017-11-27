package com.github.charleslzq.pacsdemo.vo

import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers

/**
 * Created by charleslzq on 17-11-27.
 */
data class PacsDemoViewModel(
        var seriesList: MutableList<PatientSeriesViewModel>
) {
    var selected: Int by ObservablePropertyWithObservers(-1)
    var layoutOption: LayoutOption by ObservablePropertyWithObservers(LayoutOption.ONE_ONE)

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}