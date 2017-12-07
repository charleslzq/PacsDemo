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
import com.github.charleslzq.pacsdemo.support.IndexListenableAnimationDrawable

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageView: ImageView,
        imageFramesStore: ImageFramesStore
) : Component<ImageView, ImageFramesStore>(imageView, imageFramesStore) {
    var operationMode: OperationMode = PlayMode(view.context, NoOpCompositeGestureListener())
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }
    lateinit var canvas: Canvas

    init {
        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        EventBus.onEvent<RequireRedrawCanvas> { redrawCanvas() }

        onStateChange(store::framesModel) {
            store.reset()
            if (store.framesModel.size != 0) {
                store.autoAdjustScale(view)
                init()
            } else {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(null)
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
        operationMode = PlayMode(view.context, PlayModeGestureListener(store))

        val firstImage = store.getScaledFrame(0)
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(firstImage)

        onStateChange(store::measure) {
            store.currentPath = Path()
            store.firstPath = true
            operationMode = when (store.measure != ImageFramesStore.Measure.NONE && store.framesModel.frames.isNotEmpty()) {
                true -> {
                    MeasureMode(view.context, MeasureModeGestureListener(store))
                }
                false -> {
                    store.pathList.clear()
                    store.textList.clear()
                    if (store.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, store))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store))
                    }
                }
            }
            redrawCanvas()
        }

        onStateChange(store::colorMatrix) {
            if (store.playing) {
                store.playing = false
            }
            view.colorFilter = ColorMatrixColorFilter(store.colorMatrix)
        }

        onStateChange(store::currentIndex) {
            if (!store.playing && store.framesModel.size > 0) {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(store.getScaledFrame(store.currentIndex))
            }
        }
        onStateChange(store::matrix) {
            view.imageMatrix = store.matrix
        }
        onStateChange(store::scaleFactor) {
            if (store.scaleFactor > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, store))
            } else if (store.scaleFactor == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(store))
            }
        }
        onStateChange(store::playing) {
            when (store.playing) {
                true -> view.post(store.resetAnimation(view))
                false -> {
                    if (view.background != null) {
                        val animation = view.background as IndexListenableAnimationDrawable
                        animation.stop()
                        view.clearAnimation()
                        view.background = null
                    }
                    view.setImageBitmap(store.getScaledFrame(store.currentIndex))
                }
            }
        }
        onStateChange(store::pseudoColor) {
            if (store.playing) {
                store.playing = false
            } else {
                view.setImageBitmap(store.getScaledFrame(store.currentIndex))
            }
        }

        store.currentIndex = 0
    }

    private fun redrawCanvas() {
        val bitmap = store.getScaledFrame(store.currentIndex)
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