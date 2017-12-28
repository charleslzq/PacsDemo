package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent

/**
 * Created by charleslzq on 17-12-4.
 */
class PatientSeriesStore(
        val imageFramesStore: ImageFramesStore
) : WithReducer<PatientSeriesStore> {
    var patientSeriesModel by ObservableStatus(PatientSeriesModel())
        private set
    var hideMeta by ObservableStatus(true)
        private set

    init {
        reduce(PatientSeriesStore::patientSeriesModel) {
            on<BindingEvent.ModelSelected>(precondition = { imageFramesStore.layoutPosition == 0 }) {
                event.patientSeriesModel
            }
            on<BindingEvent.ModelDropped>(precondition = { it.layoutPosition == imageFramesStore.layoutPosition }) {
                event.patientSeriesModel
            }
            on<BindingEvent.SeriesListUpdated> { PatientSeriesModel() }
        }

        reduce(PatientSeriesStore::hideMeta) {
            on<ClickEvent.ImageClicked>(precondition = { it.layoutPosition == imageFramesStore.layoutPosition }) {
                !state
            }
            on<ClickEvent.ChangeLayout> {
                true
            }
            on<BindingEvent.ModelSelected>(precondition = { 0 == imageFramesStore.layoutPosition }) {
                false
            }
            on<BindingEvent.ModelDropped>(precondition = { it.layoutPosition == imageFramesStore.layoutPosition }) {
                false
            }
        }
    }
}