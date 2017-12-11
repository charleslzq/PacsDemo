package com.github.charleslzq.pacsdemo.component.store

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.WithReducer
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageCellEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.support.IndexAwareAnimationDrawable
import java.io.File


/**
 * Created by charleslzq on 17-11-27.
 */
class ImageFramesStore(val layoutPosition: Int) : WithReducer<ImageFramesStore> {
    var linePaint = Paint()
    var stringPaint = Paint()
    private var allowPlay = true
    private var allowMeasure = true

    var imagePlayModel by ObservableStatus(ImagePlayModel())
        private set
    var rawScale = 1.0f
        private set
    var scaleFactor by ObservableStatus(1.0f)
        private set
    var matrix by ObservableStatus(Matrix())
        private set
    var colorMatrix by ObservableStatus(ColorMatrix())
        private set
    var pseudoColor by ObservableStatus(false)
        private set
    var measure by ObservableStatus(Measure.NONE)
        private set
    var imageCanvasModel by ObservableStatus(ImageCanvasModel())
        private set
    var currentPath by ObservableStatus(Path())
        private set

    init {
        linePaint.color = Color.RED
        linePaint.strokeWidth = 3f
        linePaint.isAntiAlias = true
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.style = Paint.Style.STROKE
        stringPaint.strokeWidth = 1f
        stringPaint.color = Color.RED
        stringPaint.isLinearText = true

        reduce(ImageFramesStore::imagePlayModel) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> ImagePlayModel()
                is BindingEvent.ModelSelected -> {
                    if (layoutPosition == 0) {
                        ImagePlayModel(frameMetas = event.patientSeriesModel.imageFramesModel.frames)
                    } else {
                        state
                    }
                }
                is BindingEvent.ModelDropped -> {
                    if (layoutPosition == event.layoutPosition) {
                        ImagePlayModel(frameMetas = event.patientSeriesModel.imageFramesModel.frames)
                    } else {
                        state
                    }
                }
                is BindingEvent.SeriesListUpdated -> ImagePlayModel()
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
                    if (event.layoutPosition == layoutPosition && event.index >= 0 && event.index < imagePlayModel.frameUrls.size) {
                        state.copy(currentIndex = event.index)
                    } else {
                        state
                    }
                }
                is ClickEvent.ReverseColor, is ClickEvent.PseudoColor -> {
                    if ((event as ImageCellEvent).layoutPosition == layoutPosition) {
                        state.copy(playing = false)
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.IndexScroll -> {
                    if (event.layoutPosition == layoutPosition) {
                        val currentIndex = Math.min(Math.max(imagePlayModel.currentIndex - event.scroll, 0), imagePlayModel.frameUrls.size - 1)
                        state.copy(playing = false, currentIndex = currentIndex)
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::allowPlay,
                guard = { layoutPosition == 0 && hasImage() }
        ) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> event.layoutOrdinal == 0
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::scaleFactor,
                type = ImageCellEvent::class.java
        ) { state, event ->
            when (event) {
                is ImageDisplayEvent.ScaleChange -> {
                    if (event.layoutPosition == layoutPosition) {
                        getNewScaleFactor(event.scaleFactor)
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.StudyModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        1.0f
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::matrix,
                type = ImageCellEvent::class.java,
                guard = { hasImage() }
        ) { state, event ->
            when (event) {
                is ImageDisplayEvent.ScaleChange -> {
                    if (event.layoutPosition == layoutPosition) {
                        val matrix = Matrix(state)
                        val newScale = getNewScaleFactor(event.scaleFactor)
                        matrix.setScale(newScale, newScale)
                        matrix
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.StudyModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        Matrix()
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::colorMatrix,
                type = ImageCellEvent::class.java,
                guard = { hasImage() }
        ) { state, event ->
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
                is ImageDisplayEvent.PlayModeReset, is ImageDisplayEvent.StudyModeReset -> {
                    if ((event as ImageCellEvent).layoutPosition == layoutPosition) {
                        ColorMatrix()
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::pseudoColor,
                type = ImageCellEvent::class.java,
                guard = { hasImage() }
        ) { state, event ->
            when (event) {
                is ClickEvent.PseudoColor -> {
                    if (event.layoutPosition == layoutPosition) {
                        !state
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.PlayModeReset, is ImageDisplayEvent.StudyModeReset -> {
                    if ((event as ImageCellEvent).layoutPosition == layoutPosition) {
                        false
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::allowMeasure,
                type = ClickEvent.ChangeLayout::class.java,
                guard = { hasImage() }
        ) { state, event ->
            when (event) {
                is ClickEvent.ChangeLayout -> event.layoutOrdinal == 0
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::measure,
                guard = { layoutPosition == 0 && allowMeasure }
        ) { state, event ->
            when (event) {
                is ClickEvent.TurnToMeasureLine -> Measure.LINE
                is ClickEvent.TurnToMeasureAngle -> Measure.ANGEL
                is ImageDisplayEvent.MeasureModeReset -> Measure.NONE
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::imageCanvasModel,
                type = ImageCellEvent::class.java,
                guard = { hasImage() }
        ) { state, event ->
            when (event) {
                is ImageDisplayEvent.AddPath -> {
                    if (event.layoutPosition == layoutPosition) {
                        ImageCanvasModel(
                                state.paths.toMutableList().apply {
                                    val path = Path()
                                    path.moveTo(event.points[0].x, event.points[0].y)
                                    (1..(event.points.size - 1)).forEach {
                                        path.lineTo(event.points[it].x, event.points[it].y)
                                    }
                                    add(path)
                                },
                                state.texts.toMutableMap().apply { put(event.text.first, event.text.second) }
                        )
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.MeasureModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        ImageCanvasModel()
                    } else {
                        state
                    }
                }
                else -> state
            }
        }

        reduce(
                property = ImageFramesStore::currentPath,
                type = ImageCellEvent::class.java,
                guard = { hasImage() })
        { state, event ->
            when (event) {
                is ImageDisplayEvent.AddPath -> {
                    if (event.layoutPosition == layoutPosition) {
                        Path()
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.DrawPath -> {
                    if (event.layoutPosition == layoutPosition) {
                        Path().apply {
                            moveTo(event.points[0].x, event.points[0].y)
                            (1..(event.points.size - 1)).forEach {
                                lineTo(event.points[it].x, event.points[it].y)
                            }
                        }
                    } else {
                        state
                    }
                }
                is ImageDisplayEvent.MeasureModeReset -> {
                    if (event.layoutPosition == layoutPosition) {
                        Path()
                    } else {
                        state
                    }
                }
                else -> state
            }
        }
    }

    fun playable() = imagePlayModel.frameUrls.size > 1 && allowPlay

    fun hasImage() = imagePlayModel.frameUrls.isNotEmpty()

    fun currentIndex() = imagePlayModel.currentIndex

    fun framesSize() = imagePlayModel.frameUrls.size

    fun autoAdjustScale(view: View) {
        if (hasImage()) {
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

    fun getCurrentFrame(scaled: Boolean = true): Bitmap? {
        return when (hasImage()) {
            true -> when (scaled) {
                true -> getScaledFrame(imagePlayModel.currentIndex)
                false -> getFrame(imagePlayModel.currentIndex)
            }
            false -> null
        }
    }

    fun getCurrentFrameMeta(): DicomImageMetaInfo? = if (hasImage()) imagePlayModel.frameMetas[imagePlayModel.currentIndex] else null

    private fun getNewScaleFactor(rawScaleFactor: Float): Float = Math.max(1.0f, Math.min(rawScaleFactor * scaleFactor, 5.0f))

    private fun getFrame(index: Int): Bitmap {
        val rawBitmap = BitmapFactory.decodeFile(File(imagePlayModel.frameUrls[index]).absolutePath,
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

    private fun getScaledFrame(index: Int): Bitmap {
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

    private fun getAnimation(resources: Resources, duration: Int): IndexAwareAnimationDrawable {
        val startOffset = imagePlayModel.currentIndex
        val animation = IndexAwareAnimationDrawable(layoutPosition, startOffset)
        animation.isOneShot = true
        imagePlayModel.frameUrls.subList(startOffset, imagePlayModel.frameUrls.size).forEachIndexed { index, _ ->
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