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
    private var flingSensitive = 1f

    var imageFramesModel by ObservableStatus(ImageFramesModel())
        private set
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

        reduce(ImageFramesStore::imageFramesModel) {
            on<BindingEvent.ModelSelected>(precondition = { layoutPosition == 0 }) {
                event.patientSeriesModel.imageFramesModel
            }
            on<BindingEvent.ModelDropped>(precondition = { it.layoutPosition == layoutPosition }) {
                event.patientSeriesModel.imageFramesModel
            }
            on<BindingEvent.SeriesListUpdated> { ImageFramesModel() }
            on<ClickEvent.ChangeLayout> { ImageFramesModel() }
        }

        reduce(ImageFramesStore::flingSensitive) {
            on<BindingEvent.ModelSelected>(precondition = { layoutPosition == 0 && it.patientSeriesModel.imageFramesModel.size > 0 }) {
                event.patientSeriesModel.imageFramesModel.size.toFloat() / 500
            }
            on<BindingEvent.ModelDropped>(precondition = { it.layoutPosition == layoutPosition && it.patientSeriesModel.imageFramesModel.size > 0 }) {
                event.patientSeriesModel.imageFramesModel.size.toFloat() / 500
            }
        }

        reduce(ImageFramesStore::imagePlayModel) {
            on<ClickEvent.ChangeLayout> { ImagePlayModel() }
            on<BindingEvent.SeriesListUpdated> { ImagePlayModel() }
            on<ImageDisplayEvent.ChangePlayStatus>(precondition = { targetAtThis(it) && playable() }) {
                state.copy(playing = !state.playing)
            }
            on<ImageDisplayEvent.PlayModeReset>(precondition = { targetAtThis(it) }) {
                state.copy(playing = false, currentIndex = 0)
            }
            on<ImageDisplayEvent.IndexChange>(precondition = { targetAtThis(it) && it.index in (0..(imageFramesModel.size - 1)) }) {
                state.copy(currentIndex = event.index)
            }
            on<ClickEvent.ReverseColor>(precondition = { targetAtThis(it) }) {
                state.copy(playing = false)
            }
            on<ClickEvent.PseudoColor>(precondition = { targetAtThis(it) }) {
                state.copy(playing = false)
            }
            on<ImageDisplayEvent.IndexScroll>(precondition = { targetAtThis(it) }) {
                val offset = (event.scroll * flingSensitive).toInt()
                val currentIndex = Math.min(Math.max(imagePlayModel.currentIndex - offset, 0), imageFramesModel.size - 1)
                state.copy(playing = false, currentIndex = currentIndex)
            }
        }

        reduce(ImageFramesStore::allowPlay) {
            on<ClickEvent.ChangeLayout>(precondition = { layoutPosition == 0 }) {
                event.layoutOrdinal == 0
            }
        }

        reduce(ImageFramesStore::scaleFactor) {
            on<ImageDisplayEvent.ScaleChange>(precondition = { targetAtThis(it) }) {
                getNewScaleFactor(event.scaleFactor)
            }
            on<ImageDisplayEvent.StudyModeReset>(precondition = { targetAtThis(it) }) {
                1.0f
            }
        }

        reduce(ImageFramesStore::matrix) {
            on<ImageDisplayEvent.ScaleChange>(precondition = { targetAtThis(it) }) {
                val matrix = Matrix(state)
                val newScale = getNewScaleFactor(event.scaleFactor)
                matrix.setScale(newScale, newScale)
                matrix
            }
            on<ImageDisplayEvent.StudyModeReset>(precondition = { targetAtThis(it) }) {
                Matrix()
            }
        }

        reduce(ImageFramesStore::colorMatrix) {
            on<ClickEvent.ReverseColor>(precondition = { targetAtThis(it) }) {
                val colorMatrix = ColorMatrix(state)
                colorMatrix.postConcat(reverseMatrix)
                colorMatrix
            }
            on<ImageDisplayEvent.PlayModeReset>(precondition = { targetAtThis(it) }) {
                ColorMatrix()
            }
            on<ImageDisplayEvent.StudyModeReset>(precondition = { targetAtThis(it) }) {
                ColorMatrix()
            }
        }

        reduce(ImageFramesStore::pseudoColor) {
            on<ClickEvent.PseudoColor>(precondition = { targetAtThis(it) }) {
                !state
            }
            on<ImageDisplayEvent.PlayModeReset>(precondition = { targetAtThis(it) }) {
                false
            }
            on<ImageDisplayEvent.StudyModeReset>(precondition = { targetAtThis(it) }) {
                false
            }
        }

        reduce(ImageFramesStore::allowMeasure) {
            on<ClickEvent.ChangeLayout> { event.layoutOrdinal == 0 }
        }

        reduce(ImageFramesStore::measure) {
            on<ClickEvent.TurnToMeasureLine> { Measure.LINE }
            on<ClickEvent.TurnToMeasureAngle> { Measure.ANGEL }
            on<ImageDisplayEvent.MeasureModeReset> { Measure.NONE }
        }

        reduce(ImageFramesStore::imageCanvasModel) {
            on<ImageDisplayEvent.AddPath>(precondition = { targetAtThis(it) }) {
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
            }
            on<ImageDisplayEvent.MeasureModeReset>(precondition = { targetAtThis(it) }) {
                ImageCanvasModel()
            }
        }

        reduce(property = ImageFramesStore::currentPath) {
            on<ImageDisplayEvent.AddPath>(precondition = { targetAtThis(it) }) {
                Path()
            }
            on<ImageDisplayEvent.DrawPath>(precondition = { targetAtThis(it) }) {
                Path().apply {
                    moveTo(event.points[0].x, event.points[0].y)
                    (1..(event.points.size - 1)).forEach {
                        lineTo(event.points[it].x, event.points[it].y)
                    }
                }
            }
            on<ImageDisplayEvent.MeasureModeReset>(precondition = { targetAtThis(it) }) {
                Path()
            }
        }
    }

    fun playable() = imageFramesModel.size > 1 && allowPlay

    fun hasImage() = imageFramesModel.frames.isNotEmpty()

    fun currentIndex() = imagePlayModel.currentIndex

    fun framesSize() = imageFramesModel.size

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

    fun getCurrentFrameMeta(): DicomImageMetaInfo? = if (hasImage()) imageFramesModel.frames[imagePlayModel.currentIndex] else null

    private fun getNewScaleFactor(rawScaleFactor: Float): Float = Math.max(1.0f, Math.min(rawScaleFactor * scaleFactor, 5.0f))

    private fun targetAtThis(event: ImageCellEvent) = event.layoutPosition == layoutPosition

    private fun getFrame(index: Int): Bitmap {
        val rawBitmap = BitmapFactory.decodeFile(File(imageFramesModel.frameUrls[index]).absolutePath, BitmapFactory.Options().apply {
            inMutable = pseudoColor || measure != Measure.NONE
        })
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
            resizedBitmap
        } else {
            rawBitmap
        }
    }

    private fun getAnimation(resources: Resources, duration: Int): IndexAwareAnimationDrawable {
        val startOffset = imagePlayModel.currentIndex
        val animation = IndexAwareAnimationDrawable(layoutPosition, startOffset)
        animation.isOneShot = true
        imageFramesModel.frameUrls.subList(startOffset, imageFramesModel.size).forEachIndexed { index, _ ->
            val bitmap = BitmapDrawable(resources, getFrame(startOffset + index))
            bitmap.colorFilter = ColorMatrixColorFilter(colorMatrix)
            animation.addFrame(bitmap, duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        return animation
    }

    fun getCurrentAnimation(imageView: ImageView): IndexAwareAnimationDrawable {
        return getAnimation(imageView.resources, imagePlayModel.duration)
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