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
        renderByAll(store.imageFramesStore::imagePlayModel, store.imageFramesStore::imageFramesModel) {
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

        render(store::patientSeriesModel) {
            val seriesDate = it.dicomSeriesMetaInfo.date ?: "unknown"
            val seriesTime = it.dicomSeriesMetaInfo.time ?: "unknown"
            timeInfo.post { timeInfo.text = "$seriesDate $seriesTime" }
        }

        render(store::hideMeta) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}