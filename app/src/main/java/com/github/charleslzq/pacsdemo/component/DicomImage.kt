package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
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
    private var operationMode: OperationMode = PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }

    init {
        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        view.setOnTouchListener(operationMode)

        render(ImageFramesStore::imageFramesModel) {
            store.autoAdjustScale(view)
            view.setImageBitmap(store.getCurrentFrame())
        }

        render(ImageFramesStore::imagePlayModel) {
            if (it.playing && view.background == null && store.playable()) {
                if (store.hasImage()) {
                    val animation = store.getCurrentAnimation(view)
                    view.setImageBitmap(null)
                    view.clearAnimation()
                    view.background = animation
                    view.post(animation)
                }
            } else if (!it.playing) {
                val background = view.background
                if (background != null && background is IndexAwareAnimationDrawable) {
                    background.stop()
                    view.clearAnimation()
                    view.background = null
                }
                view.setImageBitmap(store.getCurrentFrame())
            }
        }

        render(ImageFramesStore::scaleFactor) {
            if (store.scaleFactor > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(store.layoutPosition))
            } else if (store.scaleFactor == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
            }
        }

        render(ImageFramesStore::matrix) {
            view.imageMatrix = store.matrix
        }

        render(ImageFramesStore::colorMatrix) {
            view.colorFilter = ColorMatrixColorFilter(store.colorMatrix)
        }

        render(property = ImageFramesStore::pseudoColor, guard = { store.hasImage() }) {
            view.setImageBitmap(store.getCurrentFrame())
        }

        render(property = ImageFramesStore::measure, guard = { store.hasImage() }) {
            operationMode = when (store.measure != ImageFramesStore.Measure.NONE && store.hasImage()) {
                true -> {
                    drawOnImage()
                    MeasureMode(view.context, MeasureModeGestureListener(store.measure, store.layoutPosition))
                }
                false -> {
                    view.setImageBitmap(store.getCurrentFrame())
                    if (store.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(store.layoutPosition))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
                    }
                }
            }
        }

        render(property = ImageFramesStore::drawingMap, guard = { store.hasImage() }) {
            drawOnImage()
        }

        render(property = ImageFramesStore::currentLines, guard = { store.hasImage() && store.measure != ImageFramesStore.Measure.NONE }) {
            if (it.size >= 4) {
                drawOnImage(it)
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

    private fun createCanvas(): Canvas {
        return Canvas(store.getCurrentFrame()!!.also { view.setImageBitmap(it) })
    }

    private fun drawOnImage(lines: FloatArray? = null) {
        createCanvas().apply {
            store.drawingMap?.let { drawBitmap(it, 0f, 0f, store.linePaint) }
            lines?.let { drawLines(it, store.linePaint) }
        }
        view.invalidate()
    }

    companion object {
        val tag = "imageCell"
    }
}