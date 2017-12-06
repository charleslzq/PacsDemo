package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus

/**
 * Created by charleslzq on 17-12-4.
 */
class PatientSeriesViewState(
        val imageFramesViewState: ImageFramesViewState
) {
    var patientSeriesModel by ObservableStatus(PatientSeriesModel())
    var selected by ObservableStatus(false)
}