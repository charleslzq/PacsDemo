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
import java.util.*


/**
 * Created by charleslzq on 17-11-27.
 */
class ImageFramesStore(val layoutPosition: Int) : WithReducer<ImageFramesStore> {
    var linePaint = Paint()
    var stringPaint = Paint()
    var pointPaint = Paint()
    private var allowPlay = true
    private var imageWidth = 500

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
    var reverseColor by ObservableStatus(false)
        private set
    var pseudoColor by ObservableStatus(false)
        private set
    var measure by ObservableStatus(Measure.NONE)
        private set
    var currentPoints by ObservableStatus(emptyArray<PointF>())
        private set
    var drawingMap: Bitmap? by ObservableStatus(null)
        private set
    private var bitmapCache = BitmapCache()
    private val drawingStack = Stack<Bitmap>()
    private val redoStack = Stack<Bitmap>()

    init {
        linePaint.color = Color.RED
        linePaint.strokeWidth = 3f
        linePaint.isAntiAlias = true
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.style = Paint.Style.STROKE
        stringPaint.strokeWidth = 1f
        stringPaint.color = Color.RED
        stringPaint.isLinearText = true
        pointPaint.color = Color.RED
        pointPaint.strokeWidth = 3f
        pointPaint.style = Paint.Style.FILL_AND_STROKE

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

        reduce(ImageFramesStore::bitmapCache) {
            on<BindingEvent.ModelSelected>(precondition = { layoutPosition == 0 }) {
                BitmapCache(Math.max(event.patientSeriesModel.imageFramesModel.size, 10), false).apply {
                    preload(*event.patientSeriesModel.imageFramesModel.frameUrls.toTypedArray())
                }
            }
            on<BindingEvent.ModelDropped>(precondition = { it.layoutPosition == layoutPosition }) {
                BitmapCache().apply {
                    preload(*urisInRange(0, preloadRange).toTypedArray())
                }
            }
            on<BindingEvent.SeriesListUpdated> { BitmapCache() }
            on<ClickEvent.ChangeLayout> { BitmapCache() }
        }

        reduce(ImageFramesStore::imagePlayModel) {
            on<ClickEvent.ChangeLayout> { ImagePlayModel() }
            on<BindingEvent.SeriesListUpdated> { ImagePlayModel() }
            on<ImageDisplayEvent.ChangePlayStatus>(precondition = { targetAtThis(it) && playable() }) {
                state.copy(playing = !state.playing, currentIndex = if (state.currentIndex == imageFramesModel.size - 1) 0 else state.currentIndex)
            }
            on<ImageDisplayEvent.PlayModeReset>(precondition = { targetAtThis(it) }) {
                state.copy(playing = false, currentIndex = 0)
            }
            on<ImageDisplayEvent.IndexChange>(precondition = { targetAtThis(it) && it.index in (0..(imageFramesModel.size - 1)) }) {
                if (event.fromUser || event.index == imageFramesModel.size - 1) state.copy(playing = false, currentIndex = event.index) else state.copy(currentIndex = event.index)
            }
            on<ClickEvent.ReverseColor>(precondition = { targetAtThis(it) }) {
                state.copy(playing = false)
            }
            on<ClickEvent.PseudoColor>(precondition = { targetAtThis(it) }) {
                state.copy(playing = false)
            }
            on<ImageDisplayEvent.IndexScroll>(precondition = { targetAtThis(it) }) {
                val offset = (event.scroll * imageFramesModel.size.toFloat() * 2 / imageWidth).toInt()
                val currentIndex = Math.min(Math.max(imagePlayModel.currentIndex - offset, 0), imageFramesModel.size - 1)
                state.copy(playing = false, currentIndex = currentIndex)
            }
        }

        reduce(ImageFramesStore::allowPlay) {
            on<ClickEvent.ChangeLayout> {
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
                Matrix(state).apply {
                    val newScale = getNewScaleFactor(event.scaleFactor)
                    setScale(newScale, newScale)
                }
            }
            on<ImageDisplayEvent.StudyModeReset>(precondition = { targetAtThis(it) }) {
                Matrix()
            }
        }

        reduce(ImageFramesStore::reverseColor) {
            on<ClickEvent.ReverseColor>(precondition = { targetAtThis(it) }) {
                !state
            }
            on<ImageDisplayEvent.PlayModeReset>(precondition = { targetAtThis(it) }) {
                false
            }
            on<ImageDisplayEvent.StudyModeReset>(precondition = { targetAtThis(it) }) {
                false
            }
        }

        reduce(ImageFramesStore::colorMatrix) {
            on<ClickEvent.ReverseColor>(precondition = { targetAtThis(it) }) {
                ColorMatrix(state).apply {
                    postConcat(reverseMatrix)
                }
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

        reduce(ImageFramesStore::measure) {
            on<ClickEvent.MeasureLineTurned>(precondition = { targetAtThis(it) }) {
                if (event.isSelected) {
                    drawingStack.clear()
                    redoStack.clear()
                    Measure.NONE
                } else {
                    Measure.LINE
                }
            }
            on<ClickEvent.MeasureAngleTurned>(precondition = { targetAtThis(it) }) {
                if (event.isSelected) {
                    drawingStack.clear()
                    redoStack.clear()
                    Measure.NONE
                } else {
                    Measure.ANGEL
                }
            }
            on<ImageDisplayEvent.IndexChange>(precondition = { targetAtThis(it) }) {
                drawingStack.clear()
                redoStack.clear()
                Measure.NONE
            }
            on<ClickEvent.ChangeLayout> {
                drawingStack.clear()
                redoStack.clear()
                Measure.NONE
            }
        }

        reduce(ImageFramesStore::drawingMap) {
            on<ImageDisplayEvent.AddPath>(precondition = { targetAtThis(it) }) {
                val oldMap = topOfStack()
                Bitmap.createBitmap(oldMap.width, oldMap.height, oldMap.config).apply {
                    val canvas = Canvas(this)
                    canvas.drawBitmap(topOfStack(), 0f, 0f, linePaint)
                    canvas.drawPath(Path().apply {
                        moveTo(event.points[0].x, event.points[0].y)
                        repeat(event.points.size - 1) {
                            lineTo(event.points[it + 1].x, event.points[it + 1].y)
                        }
                    }, linePaint)
                    canvas.drawText(event.text.second, event.text.first.x, event.text.first.y, stringPaint)
                }.also { drawingStack.push(it) }
            }
            on<ClickEvent.Undo>(precondition = { targetAtThis(it) }) {
                if (!drawingStack.empty()) {
                    redoStack.push(drawingStack.pop())
                }
                topOfStack()
            }
            on<ClickEvent.Redo>(precondition = { targetAtThis(it) }) {
                if (!redoStack.empty()) {
                    drawingStack.push(redoStack.pop())
                }
                topOfStack()
            }
            on<ImageDisplayEvent.IndexChange>(precondition = { targetAtThis(it) }) { null }
            on<ClickEvent.MeasureLineTurned>(precondition = { targetAtThis(it) && it.isSelected }) {
                null
            }
            on<ClickEvent.MeasureAngleTurned>(precondition = { targetAtThis(it) && it.isSelected }) {
                null
            }
        }

        reduce(property = ImageFramesStore::currentPoints) {
            on<ImageDisplayEvent.AddPath>(precondition = { targetAtThis(it) }) {
                emptyArray()
            }
            on<ImageDisplayEvent.DrawLines>(precondition = { targetAtThis(it) }) {
                event.points.toTypedArray()
            }
            on<ImageDisplayEvent.IndexChange>(precondition = { targetAtThis(it) }) {
                emptyArray()
            }
            on<ClickEvent.MeasureLineTurned>(precondition = { targetAtThis(it) }) {
                emptyArray()
            }
            on<ClickEvent.MeasureAngleTurned>(precondition = { targetAtThis(it) }) {
                emptyArray()
            }
        }
    }

    fun playable() = imageFramesModel.size > 1 && allowPlay

    fun canUndo() = drawingStack.size > 1

    fun canRedo() = redoStack.size > 0

    fun hasImage() = imageFramesModel.frames.isNotEmpty()

    fun currentIndex() = imagePlayModel.currentIndex

    fun framesSize() = imageFramesModel.size

    fun autoAdjustScale(view: View) {
        if (hasImage()) {
            val image = getCurrentFrame(false)!!
            val viewHeight = view.measuredHeight
            val viewWidth = view.measuredWidth
            val imageWidth = image.width
            val imageHeight = image.height
            val ratio = imageWidth.toFloat() / imageHeight.toFloat()
            val desiredWidth = Math.ceil((viewHeight * ratio).toDouble()).toInt()
            rawScale = if (desiredWidth <= viewWidth) {
                view.layoutParams.width = desiredWidth
                this.imageWidth = desiredWidth
                desiredWidth.toFloat() / imageWidth
            } else {
                val desiredHeight = Math.ceil((viewWidth / ratio).toDouble()).toInt()
                this.imageWidth = viewWidth
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

    private fun topOfStack(): Bitmap {
        if (drawingStack.empty()) {
            drawingStack.push(getCurrentFrame()!!.let { Bitmap.createBitmap(it.width, it.height, it.config) })
        }
        return drawingStack.peek()
    }

    private fun getNewScaleFactor(rawScaleFactor: Float): Float = Math.max(1.0f, Math.min(rawScaleFactor * scaleFactor, 5.0f))

    private fun targetAtThis(event: ImageCellEvent) = event.layoutPosition == layoutPosition

    private fun urisInRange(start: Int, end: Int) = imageFramesModel.frameUrls.subList(Math.max(0, start), Math.min(end + 1, imageFramesModel.size))

    private fun getFrame(index: Int): Bitmap {
        val rawBitmap = bitmapCache.load(imageFramesModel.frameUrls[index])
        bitmapCache.preload(*urisInRange(index - preloadRange, index + preloadRange).toTypedArray())
        if (rawBitmap != null) {
            return if (pseudoColor) {
                val pixels = IntArray(rawBitmap.height * rawBitmap.width)
                rawBitmap.getPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
                repeat(pixels.size) {
                    pixels[it] = calculateColor(pixels[it])
                }
                Bitmap.createBitmap(rawBitmap.width, rawBitmap.height, rawBitmap.config).apply {
                    setPixels(pixels, 0, rawBitmap.width, 0, 0, rawBitmap.width, rawBitmap.height)
                }
            } else {
                rawBitmap.let { it.copy(it.config, true) }
            }
        } else {
            throw IllegalAccessError("Can't load image file ${imageFramesModel.frameUrls[index]}")
        }
    }

    private fun getScaledFrame(index: Int): Bitmap {
        val rawBitmap = getFrame(index)
        return if (rawScale > 1.0f) {
            val newWidth = (rawBitmap.width * rawScale).toInt()
            val newHeight = (rawBitmap.height * rawScale).toInt()
            Bitmap.createScaledBitmap(rawBitmap, newWidth, newHeight, false)
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
        private val preloadRange = 5
    }

    enum class Measure {
        NONE,
        LINE,
        ANGEL
    }
}