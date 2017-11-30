package com.github.charleslzq.pacsdemo.component

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCell(
        baseView: View,
        val layoutPosition: Int,
        pacsViewState: PacsViewState
) : PacsComponentFragment<View, PatientSeriesModel>(baseView, pacsViewState, { PatientSeriesModel() }) {
    private val progressText = ProgressText(baseView.findViewById(R.id.imageProgress), layoutPosition, pacsViewState)
    private val dicomImage = DicomImage(baseView.findViewById(R.id.imagesContainer), layoutPosition, pacsViewState)

    init {
        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val dataPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                    if (dataPosition >= 0 && dataPosition < pacsViewState.seriesList.size) {
                        val newState = pacsViewState.seriesList[dataPosition]
                        state.patientMetaInfo = newState.patientMetaInfo
                        state.studyMetaInfo = newState.studyMetaInfo
                        state.dicomSeriesMetaInfo = newState.dicomSeriesMetaInfo
                        state.imageFramesModel = newState.imageFramesModel

                        dicomImage.dataPosition = dataPosition
                        dicomImage.state.framesModel = newState.imageFramesModel
                        globalState.imageCells[layoutPosition] = dicomImage.state
                    }
                }
            }
            true
        }
    }
}