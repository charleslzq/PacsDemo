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
        refreshByProperties(store.imageFramesStore::imagePlayModel, store.imageFramesStore::framesModel) {
            if (store.imageFramesStore.hasImage()) {
                xRayInfo.visibility = View.VISIBLE
                timeInfo.visibility = View.VISIBLE

                val imageMeta = store.imageFramesStore.framesModel.frames[store.imageFramesStore.imagePlayModel.currentIndex]
                val kvp = imageMeta.kvp ?: "unknown"
                val xRay = imageMeta.xRayTubCurrent ?: "unknown"
                xRayInfo.post { xRayInfo.text = "${xRay}mA ${kvp}KV" }

                val date = store.patientSeriesModel.dicomSeriesMetaInfo.date
                val time = store.patientSeriesModel.dicomSeriesMetaInfo.time
                timeInfo.post { timeInfo.text = "$date $time" }
            } else {
                xRayInfo.visibility = View.INVISIBLE
                timeInfo.visibility = View.INVISIBLE
            }
        }
    }
}