package com.github.charleslzq.pacsdemo.component

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.ComponentGroup
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCell(
        baseView: View,
        patientSeriesViewState: PatientSeriesViewState
) : ComponentGroup<View, PatientSeriesViewState>(baseView, patientSeriesViewState, listOf(
        Sub(ImageLeftTopPanel::class, byId(R.id.leftTopPanel), sameAsParent()),
        Sub(ImageRightTopPanel::class, byId(R.id.rightTopPanel), sameAsParent()),
        Sub(DicomImage::class, byId(R.id.imagesContainer), { patientState, _ -> patientState.imageFramesViewState })
)) {

    init {
        onStateChange(state::patientSeriesModel) {
            state.imageFramesViewState.framesModel = state.patientSeriesModel.imageFramesModel
        }

        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val tag = dragEvent.clipData.getItemAt(0).text.toString()
                    if (tag == ThumbList.tag) {
                        val dataPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                        val layoutPosition = state.imageFramesViewState.layoutPosition
                        EventBus.send(DragEventMessage.DropAtCellWithData(layoutPosition, dataPosition))
                    } else if (tag == DicomImage.tag) {
                        val layoutPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                        EventBus.send(DragEventMessage.DropToCopyCell(layoutPosition, state.imageFramesViewState.layoutPosition))
                    }
                }
            }
            true
        }
    }
}