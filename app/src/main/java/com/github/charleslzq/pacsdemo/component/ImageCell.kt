package com.github.charleslzq.pacsdemo.component

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.kotlin.react.ComponentGroup
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCell(
        baseView: View,
        patientSeriesStore: PatientSeriesStore
) : ComponentGroup<View, PatientSeriesStore>(baseView, patientSeriesStore, listOf(
        Sub(ImageLeftTopPanel::class, byId(R.id.leftTopPanel), sameAsParent()),
//        Sub(ImageRightTopPanel::class, byId(R.id.rightTopPanel), sameAsParent()),
//        Sub(ImageLeftBottomPanel::class, byId(R.id.leftBottomPanel), sameAsParent()),
//        Sub(ImageControllPanel::class, byId(R.id.imageController), sameAsParent()),
        Sub(DicomImage::class, byId(R.id.imagesContainer), { patientState, _ -> patientState.imageFramesStore })
)) {

    init {
        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val tag = dragEvent.clipData.getItemAt(0).text.toString()
                    if (tag == ThumbList.tag) {
                        val dataPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                        val layoutPosition = store.imageFramesStore.layoutPosition
                        EventBus.post(DragEventMessage.DropAtCellWithData(layoutPosition, dataPosition))
                    } else if (tag == DicomImage.tag) {
                        val layoutPosition = dragEvent.clipData.getItemAt(0).htmlText.toInt()
                        EventBus.post(DragEventMessage.DropToCopyCell(layoutPosition, store.imageFramesStore.layoutPosition))
                    }
                }
            }
            true
        }
    }
}