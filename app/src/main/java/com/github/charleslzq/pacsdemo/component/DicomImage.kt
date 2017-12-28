package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.PointF
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.gesture.*
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore
import com.github.charleslzq.pacsdemo.component.store.action.ImageAction
import com.github.charleslzq.pacsdemo.support.IndexAwareAnimationDrawable
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageView: ImageView,
        imageFramesStore: ImageFramesStore
) : Component<ImageView, ImageFramesStore>(imageView, imageFramesStore), RxScheduleSupport {
    private var operationMode: OperationMode = PlayMode(view.context, PlayModeGestureListener(store.dispatch))
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }

    init {
        store.initialImageHeight = view.measuredHeight
        store.initialImageWidth = view.measuredWidth

        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        view.setOnTouchListener(operationMode)

        render(ImageFramesStore::imageFramesModel) {
            view.setImageBitmap(store.getCurrentFrame())
        }

        render(ImageFramesStore::imageDisplayModel) {
            it.image?.apply {
                val background = view.background
                if (background != null && background is IndexAwareAnimationDrawable) {
                    background.stop()
                    view.clearAnimation()
                    view.background = null
                }
                callOnCompute { autoAdjustScale(this) }.let {
                    view.layoutParams.width = it.second
                    view.layoutParams.height = it.third
                    view.setImageBitmap(it.first)
                }
            } ?: it.animation?.let {
                view.setImageBitmap(null)
                view.clearAnimation()
                view.background = it
                view.post(it)
            }
        }

        render(ImageFramesStore::scaleFactor) {
            if (store.scaleFactor > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(store.dispatch))
            } else if (store.scaleFactor == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(store.dispatch))
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
            operationMode = when (store.measure != ImageFramesStore.Measure.NONE) {
                true -> {
                    drawOnImage()
                    MeasureMode(view.context, MeasureModeGestureListener(store.measure, store.dispatch))
                }
                false -> {
                    view.setImageBitmap(store.getCurrentFrame())
                    if (store.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(store.dispatch))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store.dispatch))
                    }
                }
            }
        }

        render(property = ImageFramesStore::drawingMap, guard = { store.hasImage() && store.measure != ImageFramesStore.Measure.NONE }) {
            drawOnImage()
        }

        render(property = ImageFramesStore::currentPoints, guard = { store.hasImage() }) {
            drawOnImage()
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

    private fun autoAdjustScale(image: Bitmap): Triple<Bitmap, Int, Int> {
        val viewHeight = view.measuredHeight
        val viewWidth = view.measuredWidth
        val imageWidth = image.width
        val imageHeight = image.height
        val ratio = imageWidth.toFloat() / imageHeight.toFloat()
        val desiredWidth = Math.ceil((viewHeight * ratio).toDouble()).toInt()
        val newSize = if (desiredWidth <= viewWidth) {
            desiredWidth to viewHeight
        } else {
            viewWidth to (viewHeight * ratio).toInt()
        }
        store.rawScale = newSize.first.toFloat() / imageWidth
        return Triple(if (store.rawScale > 1.0f) {
            Bitmap.createScaledBitmap(image, view.layoutParams.width, view.layoutParams.height, false)
        } else {
            image
        }, newSize.first, newSize.second)
    }

    private fun createCanvas(): Canvas {
        return Canvas(store.getCurrentFrame()!!.also { view.setImageBitmap(it) })
    }

    private fun drawOnImage() {
        createCanvas().apply {
            store.drawingMap?.let {
                drawBitmap(it, 0f, 0f, store.linePaint)
            }
            toLines(*store.currentPoints).let {
                if (it.size > 1) {
                    drawCircle(it[it.size - 2], it[it.size - 1], 5f, store.pointPaint)
                    if (it.size > 3) {
                        drawLines(it, store.linePaint)
                    }
                }
            }
        }
        view.invalidate()
    }

    private fun toLines(vararg points: PointF): FloatArray {
        return when (points.size) {
            0 -> FloatArray(0)
            1 -> FloatArray(2).apply {
                val point = points.first()
                this[0] = point.x
                this[1] = point.y
            }
            else -> FloatArray((points.size - 1) * 4).apply {
                repeat(points.size - 1) {
                    val start = it * 4
                    this[start] = points[it].x
                    this[start + 1] = points[it].y
                    this[start + 2] = points[it + 1].x
                    this[start + 3] = points[it + 1].y
                }
            }
        }
    }

    companion object {
        val tag = "imageCell"
        val action = ImageAction()
    }
}