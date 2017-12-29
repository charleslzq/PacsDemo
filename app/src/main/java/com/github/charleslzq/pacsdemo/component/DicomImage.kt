package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.gesture.*
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore
import com.github.charleslzq.pacsdemo.support.IndexAwareAnimationDrawable
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageLayout: View,
        imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(imageLayout, imageFrameStore), RxScheduleSupport {
    private val imageView: ImageView = view.findViewById(R.id.image)
    private var operationMode: OperationMode = PlayMode(view.context, PlayModeGestureListener(store.dispatch))
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }

    init {
        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        view.setOnTouchListener(operationMode)

        render(property = ImageFrameStore::imageDisplayModel, guard = { store.hasImage() }) {
            val background = imageView.background
            if (background != null && background is IndexAwareAnimationDrawable) {
                background.stop()
                imageView.clearAnimation()
                imageView.background = null
            }
            callOnCompute { autoAdjustScale(it.images[0]) }.let {
                imageView.layoutParams.width = it.first
                imageView.layoutParams.height = it.second
            }
            if (it.images.size > 1) {
                imageView.setImageBitmap(null)
                imageView.clearAnimation()
                callOnCompute { getAnimation(view.resources) }.let {
                    imageView.background = it
                    imageView.post(it)
                }
            } else {
                imageView.setImageBitmap(getCurrentImage())
            }
        }

        render(ImageFrameStore::gestureScale) {
            if (store.gestureScale > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(store.dispatch))
            } else if (store.gestureScale == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(store.dispatch))
            }
        }

        render(ImageFrameStore::matrix) {
            imageView.imageMatrix = store.matrix
        }

        render(ImageFrameStore::colorMatrix) {
            imageView.colorFilter = ColorMatrixColorFilter(store.colorMatrix)
        }

        render(property = ImageFrameStore::pseudoColor, guard = { store.hasImage() }) {
            imageView.setImageBitmap(getCurrentImage())
        }

        render(property = ImageFrameStore::measure, guard = { store.hasImage() }) {
            operationMode = when (store.measure != ImageFrameStore.Measure.NONE) {
                true -> {
                    drawOnImage()
                    MeasureMode(view.context, MeasureModeGestureListener(store.measure, store.dispatch))
                }
                false -> {
                    imageView.setImageBitmap(getCurrentImage())
                    if (store.gestureScale > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(store.dispatch))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(store.dispatch))
                    }
                }
            }
        }

        render(property = ImageFrameStore::drawingMap, guard = { store.hasImage() && store.measure != ImageFrameStore.Measure.NONE }) {
            drawOnImage()
        }

        render(property = ImageFrameStore::currentPoints, guard = { store.hasImage() }) {
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

    private fun autoAdjustScale(image: Bitmap): Pair<Int, Int> {
        val viewHeight = view.measuredHeight
        val viewWidth = view.measuredWidth
        val imageWidth = image.width
        val imageHeight = image.height
        val ratio = imageWidth.toFloat() / imageHeight.toFloat()
        val desiredWidth = Math.ceil((viewHeight * ratio).toDouble()).toInt()
        return if (desiredWidth <= viewWidth) {
            desiredWidth to viewHeight
        } else {
            viewWidth to (viewHeight * ratio).toInt()
        }.also {
            store.autoScale = it.first.toFloat() / imageWidth
        }
    }

    private fun getCurrentImage(): Bitmap? {
        return if (store.imageDisplayModel.images.isNotEmpty()) {
            scaleIfNecessary(pseudoIfRequired(store.imageDisplayModel.images[0]))
        } else {
            null
        }
    }

    private fun scaleIfNecessary(rawBitmap: Bitmap): Bitmap {
        return if (store.autoScale > 1.0f) {
            val newWidth = (rawBitmap.width * store.autoScale).toInt()
            val newHeight = (rawBitmap.height * store.autoScale).toInt()
            Bitmap.createScaledBitmap(rawBitmap, newWidth, newHeight, false)
        } else {
            rawBitmap
        }
    }

    private fun pseudoIfRequired(rawBitmap: Bitmap): Bitmap {
        if (store.pseudoColor) {
            val pixels = IntArray(rawBitmap.height * rawBitmap.width)
            rawBitmap.getPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
            (0..(pixels.size - 1)).forEach {
                pixels[it] = calculateColor(pixels[it])
            }
            rawBitmap.setPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
        }
        return rawBitmap
    }

    private fun calculateColor(color: Int): Int {
        return getPseudoColor((Color.red(color) + Color.green(color) + Color.blue(color) + Color.alpha(color)) / 4)
    }

    private fun getPseudoColor(greyValue: Int): Int {
        return when (greyValue) {
            in (0..31) -> Color.rgb(
                    0,
                    (255 * greyValue / 32.0).toInt(),
                    (255 * greyValue / 32.0).toInt())
            in (32..63) -> Color.rgb(
                    0,
                    255,
                    255)
            in (64..95) -> Color.rgb(
                    0,
                    (255 * (96 - greyValue) / 32.0).toInt(),
                    (255 * (96 - greyValue) / 32.0).toInt())
            in (96..127) -> Color.rgb((
                    255 * (greyValue - 96) / 64.0).toInt(),
                    (255 * (greyValue - 96) / 32.0).toInt(),
                    (255 * (greyValue - 96) / 32.0).toInt())
            in (128..191) -> Color.rgb(
                    (255 * (greyValue - 128) / 128.0 + 128).toInt(),
                    0,
                    0)
            in (192..255) -> Color.rgb(
                    255,
                    (255 * (greyValue - 192) / 63.0).toInt(),
                    (255 * (greyValue - 192) / 63.0).toInt())
            else -> throw IllegalArgumentException("$greyValue not in (0..255)")
        }
    }

    private fun getAnimation(resources: Resources): IndexAwareAnimationDrawable {
        val animation = IndexAwareAnimationDrawable(store.dispatch, store.index)
        animation.isOneShot = true
        store.imageDisplayModel.images
                .map { BitmapDrawable(resources, it).apply { colorFilter = ColorMatrixColorFilter(store.colorMatrix) } }
                .forEach { animation.addFrame(it, store.duration) }
        animation.selectDrawable(0)
        animation.callback = null
        return animation
    }

    private fun createCanvas(): Canvas {
        return Canvas(getCurrentImage()!!.also { imageView.setImageBitmap(it) })
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
    }
}