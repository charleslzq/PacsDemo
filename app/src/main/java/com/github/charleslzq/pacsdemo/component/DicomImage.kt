package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
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
    private var operationMode: OperationMode =
        PlayMode(imageView.context, PlayModeGestureListener(store.dispatch, ::onDragStart))
        set(value) {
            field = value
            imageView.setOnTouchListener(operationMode)
        }

    init {
        imageView.setOnTouchListener(operationMode)

        render(ImageFrameStore::displayModel) {
            val background = imageView.background
            if (background != null && background is IndexAwareAnimationDrawable) {
                background.stop()
                imageView.clearAnimation()
                imageView.background = null
            }
            if (it.images.isNotEmpty()) {
                runOnCompute { autoAdjustScale(it.images[0]) }
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
                operationMode = StudyMode(
                    imageView.context,
                    StudyModeGestureListener(store.dispatch, ::onDragStart)
                )
            } else if (store.gestureScale == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(
                    imageView.context,
                    PlayModeGestureListener(store.dispatch, ::onDragStart)
                )
            }
        }

        render(ImageFrameStore::matrix) {
            imageView.imageMatrix = store.matrix
        }

        render(ImageFrameStore::colorMatrix) {
            imageView.colorFilter = ColorMatrixColorFilter(store.colorMatrix)
        }

        render(property = ImageFrameStore::pseudoColor, require = { store.hasImage }) {
            imageView.setImageBitmap(getCurrentImage())
        }

        render(property = ImageFrameStore::measure, require = { store.hasImage }) {
            operationMode = when (store.measure != ImageFrameStore.Measure.NONE) {
                true -> {
                    MeasureMode(imageView.context, MeasureModeGestureListener(store.dispatch))
                }
                false -> {
                    if (store.gestureScale > 1.0f) {
                        StudyMode(
                            imageView.context,
                            StudyModeGestureListener(store.dispatch, ::onDragStart)
                        )
                    } else {
                        PlayMode(
                            imageView.context,
                            PlayModeGestureListener(store.dispatch, ::onDragStart)
                        )
                    }
                }
            }
        }

        render(property = ImageFrameStore::canvasModel, require = { store.hasImage }) {
            Canvas(getCurrentImage()!!.also { imageView.setImageBitmap(it) }).apply {
                store.canvasModel.drawing?.let { drawBitmap(it, 0f, 0f, store.linePaint) }
                store.canvasModel.tmp?.let { drawBitmap(it, 0f, 0f, store.linePaint) }
            }
            imageView.invalidate()
        }
    }

    private fun onDragStart() {
        val dragBuilder = View.DragShadowBuilder(imageView)
        val clipData =
            ClipData(tag, arrayOf(ClipDescription.MIMETYPE_TEXT_HTML), ClipData.Item(tag))
        @Suppress("DEPRECATION")
        imageView.startDrag(clipData, dragBuilder, store, 0)
    }

    private fun autoAdjustScale(image: Bitmap) {
        val viewHeight = view.measuredHeight
        val viewWidth = view.measuredWidth
        val imageWidth = image.width
        val imageHeight = image.height
        val ratio = imageWidth.toFloat() / imageHeight.toFloat()
        val desiredWidth = Math.ceil((viewHeight * ratio).toDouble()).toInt()
        store.autoScale = if (desiredWidth <= viewWidth) {
            imageView.layoutParams.width = desiredWidth
            desiredWidth.toFloat() / imageWidth
        } else {
            val desiredHeight = Math.ceil((viewWidth / ratio).toDouble()).toInt()
            imageView.layoutParams.height = desiredHeight
            desiredHeight.toFloat() / imageHeight
        }
        imageView.imageMatrix = Matrix().apply {
            setScale(store.autoScale, store.autoScale)
        }
    }

    private fun getCurrentImage() = if (store.displayModel.images.isNotEmpty()) {
        callOnCompute {
            pseudoIfRequired(store.displayModel.images[0]).let {
                it.copy(
                    it.config,
                    true
                )
            }
        }
    } else {
        null
    }

    private fun pseudoIfRequired(rawBitmap: Bitmap) = if (store.pseudoColor) {
        val pixels = IntArray(rawBitmap.height * rawBitmap.width)
        rawBitmap.getPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
        repeat(pixels.size) {
            pixels[it] = calculateColor(pixels[it])
        }
        Bitmap.createBitmap(rawBitmap.width, rawBitmap.height, rawBitmap.config).apply {
            setPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
        }
    } else {
        rawBitmap
    }

    private fun calculateColor(color: Int) = getPseudoColor(
        (Color.red(color) + Color.green(color) + Color.blue(color) + Color.alpha(color)) / 4
    )

    private fun getPseudoColor(greyValue: Int) = when (greyValue) {
        in (0..31) -> Color.rgb(
            0,
            (255 * greyValue / 32.0).toInt(),
            (255 * greyValue / 32.0).toInt()
        )
        in (32..63) -> Color.rgb(
            0,
            255,
            255
        )
        in (64..95) -> Color.rgb(
            0,
            (255 * (96 - greyValue) / 32.0).toInt(),
            (255 * (96 - greyValue) / 32.0).toInt()
        )
        in (96..127) -> Color.rgb(
            (255 * (greyValue - 96) / 64.0).toInt(),
            (255 * (greyValue - 96) / 32.0).toInt(),
            (255 * (greyValue - 96) / 32.0).toInt()
        )
        in (128..191) -> Color.rgb(
            (255 * (greyValue - 128) / 128.0 + 128).toInt(),
            0,
            0
        )
        in (192..255) -> Color.rgb(
            255,
            (255 * (greyValue - 192) / 63.0).toInt(),
            (255 * (greyValue - 192) / 63.0).toInt()
        )
        else -> throw IllegalArgumentException("$greyValue not in (0..255)")
    }

    private fun getAnimation(resources: Resources) =
        IndexAwareAnimationDrawable(store.dispatch, store.autoJumpIndex).apply {
            isOneShot = true
            store.displayModel.images
                .map {
                    BitmapDrawable(resources, it).apply {
                        colorFilter = ColorMatrixColorFilter(store.colorMatrix)
                    }
                }
                .forEach { addFrame(it, store.duration) }
            selectDrawable(0)
            callback = null
        }

    companion object {
        val tag = "imageCell"
    }
}