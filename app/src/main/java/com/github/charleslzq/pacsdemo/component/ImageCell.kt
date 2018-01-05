package com.github.charleslzq.pacsdemo.component

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.kotlin.react.ComponentGroup
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageActions
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCell(
        baseView: View,
        imageFrameStore: ImageFrameStore
) : ComponentGroup<View, ImageFrameStore>(baseView, imageFrameStore, listOf(
        Sub(ImageLeftTopPanel::class, byId(R.id.leftTopPanel), sameAsParent()),
        Sub(ImageRightTopPanel::class, byId(R.id.rightTopPanel), sameAsParent()),
        Sub(ImageLeftBottomPanel::class, byId(R.id.leftBottomPanel), sameAsParent()),
        Sub(ImageControllPanel::class, byId(R.id.imageController), sameAsParent()),
        Sub(DicomImage::class, byId(R.id.imageContainer), sameAsParent())
)) {
    init {
        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val tag = dragEvent.clipData.getItemAt(0).text.toString()
                    if (tag == DicomImage.tag) {
                        (dragEvent.localState as? ImageFrameStore)?.let {
                            if (it.layoutPosition != imageFrameStore.layoutPosition) {
                                store.dispatch(ImageActions.moveFrame(it))
                            }
                        }
                    } else if (tag == ThumbList.tag) {
                        (dragEvent.localState as? String)?.let {
                            store.dispatch(ImageActions.bindModel(it))
                        }
                    }
                }
            }
            true
        }
    }
}