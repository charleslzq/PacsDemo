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
        Sub(ProgressText::class, byId(R.id.imageProgress), { patientState, _ -> patientState.imageFramesViewState }),
        Sub(DicomImage::class, byId(R.id.imagesContainer), { patientState, _ -> patientState.imageFramesViewState })
)) {

    init {
        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val dataPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                    val layoutPosition = state.imageFramesViewState.layoutPosition
                    EventBus.send(DragEventMessage.DropAtCellWithData(layoutPosition, dataPosition))
                }
            }
            true
        }
    }
}