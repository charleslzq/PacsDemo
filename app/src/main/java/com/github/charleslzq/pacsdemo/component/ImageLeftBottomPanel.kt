package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.Component
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
        refreshByProperties(store.imageFramesStore::imagePlayModel) {
            xRayInfo.visibility = View.INVISIBLE
            timeInfo.visibility = View.INVISIBLE

            store.imageFramesStore.getCurrentFrameMeta()?.let {
                xRayInfo.visibility = View.VISIBLE
                timeInfo.visibility = View.VISIBLE

                val kvp = it.kvp ?: "unknown"
                val xRay = it.xRayTubCurrent ?: "unknown"
                xRayInfo.post { xRayInfo.text = "${xRay}mA ${kvp}KV" }
            }
        }

        refreshByProperty(store::patientSeriesModel) {
            val seriesDate = it.dicomSeriesMetaInfo.date ?: "unknown"
            val seriesTime = it.dicomSeriesMetaInfo.time ?: "unknown"
            timeInfo.post { timeInfo.text = "$seriesDate $seriesTime" }
        }
    }
}