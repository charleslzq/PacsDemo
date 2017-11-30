package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers

/**
 * Created by charleslzq on 17-11-27.
 */
data class PacsViewState(
        var seriesList: MutableList<PatientSeriesModel> = mutableListOf()
) {
    var selected: Int by ObservablePropertyWithObservers(0)
    var layoutOption: LayoutOption by ObservablePropertyWithObservers(LayoutOption.ONE_ONE)
    var imageCells: MutableList<ImageFramesViewState?> = arrayOfNulls<ImageFramesViewState>(9).toMutableList()
    var singleBinding by ObservablePropertyWithObservers(false)

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}