package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Bitmap
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
    lateinit var canvas: Canvas
    var drawingCache: Bitmap? = null
    var drawingCanvas: Canvas? = null

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
                    createDrawingCache()
                    initCanvas()
                    store.imageCanvasModel.paths.forEach {
                        drawingCanvas?.drawPath(it, store.linePaint)
                    }
                    store.imageCanvasModel.texts.forEach {
                        drawingCanvas?.drawText(it.value, it.key.x, it.key.y, store.stringPaint)
                    }
                    canvas.drawBitmap(drawingCache, 0f, 0f, store.linePaint)
                    view.invalidate()
                    MeasureMode(view.context, MeasureModeGestureListener(store))
                }
                false -> {
                    view.setImageBitmap(store.getCurrentFrame())
                    drawingCache = null
                    if (store.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(store.layoutPosition))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store.layoutPosition))
                    }
                }
            }
        }

        render(property = ImageFramesStore::imageCanvasModel, guard = { store.hasImage() }) {
            createDrawingCache()
            initCanvas()
            store.imageCanvasModel.paths.forEach {
                drawingCanvas?.drawPath(it, store.linePaint)
            }
            store.imageCanvasModel.texts.forEach {
                drawingCanvas?.drawText(it.value, it.key.x, it.key.y, store.stringPaint)
            }
            canvas.drawBitmap(drawingCache, 0f, 0f, store.linePaint)
            view.invalidate()
        }

        render(property = ImageFramesStore::currentLines, guard = { store.hasImage() && store.measure != ImageFramesStore.Measure.NONE }) {
            if (it.size >= 4) {
                initCanvas()
                canvas.drawBitmap(drawingCache, 0f, 0f, store.linePaint)
                canvas.drawLines(it, store.linePaint)
                view.invalidate()
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

    private fun initCanvas() {
        val bitmap = store.getCurrentFrame()
        canvas = Canvas(bitmap)
        view.setImageBitmap(bitmap)
    }

    private fun createDrawingCache() {
        store.getCurrentFrame()?.let {
            drawingCache = Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
            drawingCanvas = Canvas(drawingCache)
        }
    }

    companion object {
        val tag = "imageCell"
    }
}