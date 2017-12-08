package com.github.charleslzq.pacsdemo.component.store

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.base.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus
import com.github.charleslzq.pacsdemo.support.IndexAwareAnimationDrawable
import java.io.File


/**
 * Created by charleslzq on 17-11-27.
 */
class ImageFramesStore(val layoutPosition: Int) : WithReducer {
    var measure by ObservableStatus(Measure.NONE)
    var linePaint = Paint()
    var stringPaint = Paint()

    var currentPath = Path()
    val pathList = mutableListOf<Path>()
    val textList = mutableListOf<Pair<PointF, String>>()
    var firstPath = true


    var rawScale = 1.0f
        private set
    var scaleFactor = 1.0f
        private set

    var framesModel by ObservableStatus(ImageFramesModel())
        private set
    var imagePlayModel by ObservableStatus(ImagePlayModel())
        private set
    var allowPlay = false
        private set
    var matrix by ObservableStatus(Matrix())
        private set
    var colorMatrix by ObservableStatus(ColorMatrix())
        private set
    var pseudoColor by ObservableStatus(false)
        private set

    init {
        reduce(this::framesModel) { state, event ->
            when (event) {
                is BindingEvent.ModelSelected -> {
                    if (layoutPosition == 0) {
                        event.patientSeriesModel.imageFramesModel
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(this::imagePlayModel, { framesModel.size > 0 }) { state, event ->
            when (event) {
                is BindingEvent.ModelSelected -> {
                    if (layoutPosition == 0 && event.patientSeriesModel.imageFramesModel.size > 0) {
                        ImagePlayModel().copy(currentIndex = 0)
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.ChangePlayStatus -> {
                    if (event.layoutPosition == layoutPosition && playable()) {
                        state.copy(playing = !state.playing)
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.PlayModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        ImagePlayModel()
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.IndexChange -> {
                    if (event.layoutPosition == layoutPosition && event.index >= 0 && event.index < framesModel.size) {
                        state.copy(currentIndex = event.index)
                    } else {
                        state
                    }
                }
                is ClickEvent.ReverseColor -> {
                    if (event.layoutPosition == layoutPosition) {
                        state.copy(playing = false)
                    } else {
                        state
                    }
                }
                is ClickEvent.PseudoColor -> {
                    if (event.layoutPosition == layoutPosition) {
                        state.copy(playing = false)
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.IndexScroll -> {
                    if (event.layoutPosition == layoutPosition) {
                        val currentIndex = Math.min(Math.max(imagePlayModel.currentIndex - event.scroll, 0), framesModel.size - 1)
                        state.copy(playing = false, currentIndex = currentIndex)
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(this::allowPlay, { layoutPosition == 0 && framesModel.size > 0 }) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> event.layoutOrdinal == 0
                else -> state
            }
        }

        reduce(this::matrix, { framesModel.size > 0 }) { state, event ->
            when (event) {
                is ImageDisplayEvent.ScaleChange -> {
                    if (event.layoutPosition == layoutPosition) {
                        val matrix = Matrix(state)
                        matrix.setScale(event.scaleFactor, event.scaleFactor)
                        matrix
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(this::colorMatrix, { framesModel.size > 0 }) { state, event ->
            when (event) {
                is ClickEvent.ReverseColor -> {
                    if (event.layoutPosition == layoutPosition) {
                        val colorMatrix = ColorMatrix(state)
                        colorMatrix.postConcat(reverseMatrix)
                        colorMatrix
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.PlayModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        ColorMatrix()
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(this::pseudoColor, { framesModel.size > 0 }) { state, event ->
            when (event) {
                is ClickEvent.PseudoColor -> {
                    if (event.layoutPosition == layoutPosition) {
                        !state
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.PlayModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        false
                    } else {
                        state
                    }
                }
                else -> state
            }
        }
    }

    fun playable() = framesModel.size > 1 && allowPlay

    fun autoAdjustScale(view: View) {
        if (framesModel.size > 0) {
            val viewHeight = view.measuredHeight
            val viewWidth = view.measuredWidth
            val firstImage = getFrame(0)
            val imageWidth = firstImage.width
            val imageHeight = firstImage.height
            val ratio = imageWidth.toFloat() / imageHeight.toFloat()
            val desiredWidth = Math.ceil((viewHeight * ratio).toDouble()).toInt()
            rawScale = if (desiredWidth <= viewWidth) {
                view.layoutParams.width = desiredWidth
                desiredWidth.toFloat() / imageWidth
            } else {
                val desiredHeight = Math.ceil((viewWidth / ratio).toDouble()).toInt()
                view.layoutParams.height = desiredHeight
                desiredHeight.toFloat() / imageHeight
            }
        }
    }

    fun getFrame(index: Int): Bitmap {
        val rawBitmap = BitmapFactory.decodeFile(File(framesModel.frameUrls[index]).absolutePath,
                BitmapFactory.Options().apply { inMutable = pseudoColor || measure != Measure.NONE })
        if (pseudoColor) {
            val pixels = IntArray(rawBitmap.height * rawBitmap.width)
            rawBitmap.getPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
            (0..(pixels.size - 1)).forEach {
                pixels[it] = calculateColor(pixels[it])
            }
            rawBitmap.setPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
        }
        return rawBitmap
    }

    fun getScaledFrame(index: Int): Bitmap {
        val rawBitmap = getFrame(index)
        return if (rawScale != 1.0f) {
            val newWidth = (rawBitmap.width * rawScale).toInt()
            val newHeight = (rawBitmap.height * rawScale).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, newWidth, newHeight, false)
            rawBitmap.recycle()
            resizedBitmap
        } else {
            rawBitmap
        }
    }

    fun getAnimation(resources: Resources, duration: Int): IndexAwareAnimationDrawable {
        val startOffset = imagePlayModel.currentIndex
        val animation = IndexAwareAnimationDrawable(layoutPosition, startOffset)
        animation.isOneShot = true
        framesModel.frames.subList(startOffset, framesModel.size).forEachIndexed { index, _ ->
            val bitmap = BitmapDrawable(resources, getFrame(startOffset + index))
            bitmap.colorFilter = ColorMatrixColorFilter(colorMatrix)
            animation.addFrame(bitmap, duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        return animation
    }

    fun resetAnimation(imageView: ImageView): IndexAwareAnimationDrawable {
        val animation = getAnimation(imageView.resources, imagePlayModel.duration)

        imageView.clearAnimation()
        imageView.setImageBitmap(null)
        imageView.background = animation

        return animation
    }

    fun calculateColor(color: Int): Int {
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

    companion object {
        private val reverseMatrix = ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
        ))
    }

    enum class Measure {
        NONE,
        LINE,
        ANGEL
    }
}