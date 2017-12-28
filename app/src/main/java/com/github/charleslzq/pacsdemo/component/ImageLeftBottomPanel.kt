package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftBottomPanel(
        panelView: View,
        patientSeriesStore: PatientSeriesStore
) : Component<View, PatientSeriesStore>(panelView, patientSeriesStore) {
    private val xRayInfo: TextView = view.findViewById(R.id.xRayInfo)
    private val timeInfo: TextView = view.findViewById(R.id.time)

    init {
        renderByAll(store.imageFramesStore::imageDisplayModel, store.imageFramesStore::imageFramesModel) {
            xRayInfo.visibility = View.GONE

            store.imageFramesStore.getCurrentFrameMeta()?.let {
                buildString {
                    it.xRayTubCurrent?.let { append("$it mA ") }
                    it.kvp?.let { append("$it KV") }
                }.takeIf { it.isNotBlank() }?.let {
                    xRayInfo.post { xRayInfo.text = it }
                    xRayInfo.visibility = View.VISIBLE
                }
            }
        }

        render(store::patientSeriesModel) {
            timeInfo.visibility = View.GONE

            it.dicomSeriesMetaInfo.let {
                buildString {
                    it.date?.apply { append(this + " ") }
                    it.time?.apply { append(this) }
                }.takeIf { it.isNotBlank() }?.let {
                    timeInfo.post { timeInfo.text = it }
                    timeInfo.visibility = View.VISIBLE
                }
            }
        }

        render(property = store.imageFramesStore::hideMeta, guard = { store.imageFramesStore.hasImage() }) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}