package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Path
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.RequireRedrawCanvas
import com.github.charleslzq.pacsdemo.component.gesture.*
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore
import com.github.charleslzq.pacsdemo.support.IndexAwareAnimationDrawable

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageView: ImageView,
        imageFramesStore: ImageFramesStore
) : Component<ImageView, ImageFramesStore>(imageView, imageFramesStore) {
    var operationMode: OperationMode = PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }
    lateinit var canvas: Canvas

    init {
        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        EventBus.onEvent<RequireRedrawCanvas> { redrawCanvas() }

        refreshByProperty(store::framesModel) {
            view.clearAnimation()
            view.background = null
            if (store.indexValid()) {
                view.setImageBitmap(store.getScaledFrame(store.imagePlayModel.currentIndex))
            }
        }

        refreshByProperty(store::imagePlayModel) {
            if (it.playing && view.background == null && store.playable()) {
                view.setImageBitmap(null)
                view.post(store.resetAnimation(view))
            } else if (!it.playing) {
                val background = view.background
                if (background != null && background is IndexAwareAnimationDrawable) {
                    background.stop()
                    view.clearAnimation()
                    view.background = null
                }
                if (store.indexValid()) {
                    view.setImageBitmap(store.getScaledFrame(it.currentIndex))
                }
            }
        }

        refreshByProperty(store::matrix) {
            view.imageMatrix = store.matrix
        }

        refreshByProperty(store::colorMatrix) {
            view.colorFilter = ColorMatrixColorFilter(store.colorMatrix)
        }

        refreshByProperty(store::pseudoColor) {
            if (store.indexValid()) {
                view.setImageBitmap(store.getScaledFrame(store.imagePlayModel.currentIndex))
            }
        }

        refreshByProperty(store::measure) {
            store.currentPath = Path()
            store.firstPath = true
            operationMode = when (store.measure != ImageFramesStore.Measure.NONE && store.framesModel.frames.isNotEmpty()) {
                true -> {
                    MeasureMode(view.context, MeasureModeGestureListener(store, store.layoutPosition))
                }
                false -> {
                    store.pathList.clear()
                    store.textList.clear()
                    if (store.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, store, store.layoutPosition))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
                    }
                }
            }
            if (store.measure != ImageFramesStore.Measure.NONE && store.indexValid()) {
                redrawCanvas()
            }
        }
    }

    private fun onDragStart(dragCopyCellMessage: DragEventMessage.StartCopyCell) {
        if (dragCopyCellMessage.layoutPosition == store.layoutPosition) {
            val dragBuilder = View.DragShadowBuilder(view)
            val clipDataItem = ClipData.Item(tag, store.layoutPosition.toString())
            val clipData = ClipData(tag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipDataItem)
            @Suppress("DEPRECATION")
            view.startDrag(clipData, dragBuilder, null, 0)
        }
    }

    private fun init() {


//        onStateChange(store::scaleFactor) {
//            if (store.scaleFactor > 1 && operationMode is PlayMode) {
//                operationMode = StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, store))
//            } else if (store.scaleFactor == 1.0f && operationMode is StudyMode) {
//                operationMode = PlayMode(view.context, PlayModeGestureListener(store))
//            }
//        }
    }

    private fun redrawCanvas() {
        val bitmap = store.getScaledFrame(store.imagePlayModel.currentIndex)
        canvas = Canvas(bitmap)
        view.setImageBitmap(bitmap)

        store.pathList.forEach {
            canvas.drawPath(it, store.linePaint)
        }

        canvas.drawPath(store.currentPath, store.linePaint)

        store.textList.forEach {
            canvas.drawText(it.second, it.first.x, it.first.y, store.stringPaint)
        }

        view.invalidate()
    }

    companion object {
        val tag = "imageCell"
    }
}