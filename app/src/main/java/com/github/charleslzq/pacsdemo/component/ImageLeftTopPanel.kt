package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesViewState

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftTopPanel(
        panelView: LinearLayout,
        patientSeriesViewState: PatientSeriesViewState
) : Component<LinearLayout, PatientSeriesViewState>(panelView, patientSeriesViewState) {
    private val windowInfo: TextView = view.findViewById(R.id.windowInfo)
    private val sliceInfo: TextView = view.findViewById(R.id.sliceInfo)
    private val scaleInfo: TextView = view.findViewById(R.id.scaleInfo)
    private val imageProgress: TextView = view.findViewById(R.id.imageProgress)

    init {
        onStateChange(state::patientSeriesModel) {
            val visible = when (state.patientSeriesModel.imageFramesModel.size == 0) {
                true -> View.INVISIBLE
                false -> View.VISIBLE
            }
            windowInfo.visibility = visible
            sliceInfo.visibility = visible
            scaleInfo.visibility = visible
            imageProgress.visibility = visible
        }

        onStateChange(state.imageFramesViewState::currentIndex, false) {
            setTexts()
        }

    }

    private fun setTexts() {
        if (state.patientSeriesModel.imageFramesModel.size > 0) {
            val imageInfo = state.patientSeriesModel.imageFramesModel.frames[state.imageFramesViewState.currentIndex]

            val windowCenter = imageInfo.windowCenter ?: "不明"
            val windowWidth = imageInfo.windowWidth ?: "不明"
            windowInfo.post { windowInfo.text = "窗宽: $windowWidth 窗位: $windowCenter" }

            val sliceThickness = imageInfo.sliceThickness ?: "不明"
            val sliceLocation = imageInfo.sliceLocation ?: "不明"
            sliceInfo.post { sliceInfo.text = "T:${sliceThickness}mm L:${sliceLocation}mm" }

            scaleInfo.post { scaleInfo.text = "缩放: ${state.imageFramesViewState.scaleFactor * state.imageFramesViewState.rawScale}倍" }

            imageProgress.post { imageProgress.text = "IMAGE: ${state.imageFramesViewState.currentIndex + 1}/${state.imageFramesViewState.framesModel.size}" }
        }
    }
}