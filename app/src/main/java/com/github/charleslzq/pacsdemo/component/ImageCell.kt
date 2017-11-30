package com.github.charleslzq.pacsdemo.component

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCell(
        baseView: View,
        val layoutPosition: Int,
        private val pacsViewState: PacsViewState
) : Component<View, PatientSeriesModel>(baseView, { PatientSeriesModel() }) {
    private val progressText = ProgressText(baseView.findViewById(R.id.imageProgress))
    private val dicomImage = DicomImage(baseView.findViewById(R.id.imagesContainer))

    init {
        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val dataPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                    if (dataPosition >= 0 && dataPosition < pacsViewState.seriesList.size) {
                        state = pacsViewState.seriesList[dataPosition].clone()
                    }
                }
            }
            true
        }

        onNewState {
            val imageState = ImageFramesViewState(state.imageFramesModel)
            if (pacsViewState.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                imageState.allowPlay = true
            }
            dicomImage.state = imageState
            pacsViewState.imageCells[layoutPosition] = dicomImage.state
            progressText.state = dicomImage.state
        }
    }
}