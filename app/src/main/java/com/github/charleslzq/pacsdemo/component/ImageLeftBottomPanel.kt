package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesViewState

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftBottomPanel(
        panelView: View,
        patientSeriesViewState: PatientSeriesViewState
) : Component<View, PatientSeriesViewState>(panelView, patientSeriesViewState) {
    private val xRayInfo: TextView = view.findViewById(R.id.xRayInfo)
    private val timeInfo: TextView = view.findViewById(R.id.time)

    init {
        onStateChange(state.imageFramesViewState::currentIndex, false) {
            if (state.imageFramesViewState.framesModel.size > 1) {
                xRayInfo.visibility = View.VISIBLE
                timeInfo.visibility = View.VISIBLE

                val imageMeta = state.imageFramesViewState.framesModel.frames[state.imageFramesViewState.currentIndex]
                val kvp = imageMeta.kvp ?: "unknown"
                val xRay = imageMeta.xRayTubCurrent ?: "unknown"
                xRayInfo.post { xRayInfo.text = "${xRay}mA ${kvp}KV" }

                val date = state.patientSeriesModel.dicomSeriesMetaInfo.date
                val time = state.patientSeriesModel.dicomSeriesMetaInfo.time
                timeInfo.post { timeInfo.text = "$date $time" }
            } else {
                xRayInfo.visibility = View.INVISIBLE
                timeInfo.visibility = View.INVISIBLE
            }
        }
    }
}