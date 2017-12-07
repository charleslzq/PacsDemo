package com.github.charleslzq.pacsdemo.component.event

import com.github.charleslzq.pacsdemo.component.store.PatientSeriesModel

/**
 * Created by charleslzq on 17-12-7.
 */
class BindingEvent {
    data class ModelSelected(val patientSeriesModel: PatientSeriesModel) : Event
    data class ModelDropped(val layoutPosition: Int, val patientSeriesModel: PatientSeriesModel) : Event
    data class SeriesListUpdated(val seriesList: MutableList<PatientSeriesModel>) : Event
}