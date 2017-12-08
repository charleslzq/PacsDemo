package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.EventBus
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
    lateinit var canvas: Canvas

    init {
        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        view.setOnTouchListener(operationMode)

        refreshByProperty(store::imagePlayModel) {
            if (it.playing && view.background == null && store.playable()) {
                view.setImageBitmap(null)
                if (store.hasImage()) {
                    view.post(store.resetAnimation(view))
                }
            } else if (!it.playing) {
                val background = view.background
                view.setImageBitmap(null)
                if (background != null && background is IndexAwareAnimationDrawable) {
                    background.stop()
                    view.clearAnimation()
                    view.background = null
                }
                if (store.hasImage()) {
                    store.autoAdjustScale(view)
                    view.setImageBitmap(store.getCurrentFrame())
                }
            }
        }

        refreshByProperty(store::scaleFactor) {
            if (store.scaleFactor > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(store.layoutPosition))
            } else if (store.scaleFactor == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
            }
        }

        refreshByProperty(store::matrix) {
            view.imageMatrix = store.matrix
        }

        refreshByProperty(store::colorMatrix) {
            view.colorFilter = ColorMatrixColorFilter(store.colorMatrix)
        }

        refreshByProperty(store::pseudoColor, { store.hasImage() }) {
            view.setImageBitmap(store.getCurrentFrame())
        }

        refreshByProperty(store::measure, { store.hasImage() }) {
            operationMode = when (store.measure != ImageFramesStore.Measure.NONE && store.hasImage()) {
                true -> {
                    initCanvas()
                    MeasureMode(view.context, MeasureModeGestureListener(store, store.layoutPosition))
                }
                false -> {
                    if (store.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(store.layoutPosition))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
                    }
                }
            }
        }

        refreshByProperty(store::imageCanvasModel, { store.hasImage() }) {
            initCanvas()
            it.paths.forEach {
                canvas.drawPath(it, store.linePaint)
            }
            it.texts.forEach {
                canvas.drawText(it.value, it.key.x, it.key.y, store.stringPaint)
            }
            view.invalidate()
        }

        refreshByProperty(store::currentPath, { store.hasImage() }) {
            initCanvas()
            canvas.drawPath(it, store.linePaint)
            view.invalidate()
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

    private fun initCanvas() {
        val bitmap = store.getCurrentFrame()
        canvas = Canvas(bitmap)
        view.setImageBitmap(bitmap)
    }

    companion object {
        val tag = "imageCell"
    }
}