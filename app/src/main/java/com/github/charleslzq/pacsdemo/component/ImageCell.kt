package com.github.charleslzq.pacsdemo.component

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageActions
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCell(
        baseView: View,
        imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(baseView, imageFrameStore) {
    init {
        bind {
            child { ImageLeftTopPanel(byId(R.id.leftTopPanel), store) }
            child { ImageRightTopPanel(byId(R.id.rightTopPanel), store) }
            child { ImageLeftBottomPanel(byId(R.id.leftBottomPanel), store) }
            child { ImageControllPanel(byId(R.id.imageController), store) }
            child { DicomImage(byId(R.id.imageContainer), store) }
        }

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