package com.github.charleslzq.pacsdemo.component.store

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.base.WithReducer
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus.Companion.getDelegate
import com.github.charleslzq.pacsdemo.support.IndexListenableAnimationDrawable
import java.io.File


/**
 * Created by charleslzq on 17-11-27.
 */
class ImageFramesStore(val layoutPosition: Int) : WithReducer {
    var framesModel by ObservableStatus(ImageFramesModel())
    var duration: Int = 40
    var scaleFactor: Float by ObservableStatus(1.0f)
    var currentIndex: Int by ObservableStatus(0)
    var startOffset: Int = 0
    var matrix by ObservableStatus(Matrix())
    var colorMatrix by ObservableStatus(ColorMatrix())
    var playing by ObservableStatus(false)
    var pseudoColor by ObservableStatus(false)
    var allowPlay = false
    var measure by ObservableStatus(Measure.NONE)
    var linePaint = Paint()
    var stringPaint = Paint()

    var currentPath = Path()
    val pathList = mutableListOf<Path>()
    val textList = mutableListOf<Pair<PointF, String>>()
    var firstPath = true


    var rawScale = 1.0f

    init {
        reset()
        getDelegate(this::scaleFactor)?.onChange {
            val newMatrix = Matrix(matrix)
            val scale = it.second / it.first
            newMatrix.postScale(scale, scale)
            matrix = newMatrix
        }
    }

    fun reset() {
        scaleFactor = 1.0f
        currentIndex = 0
        startOffset = 0
        matrix = Matrix()
        colorMatrix = ColorMatrix()
        playing = false
        pseudoColor = false
        allowPlay = false
        measure = Measure.NONE
        linePaint = Paint()
        stringPaint = Paint()

        linePaint.color = Color.RED
        linePaint.strokeWidth = 3f
        linePaint.isAntiAlias = true
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.style = Paint.Style.STROKE
        stringPaint.strokeWidth = 1f
        stringPaint.color = Color.RED
        stringPaint.isLinearText = true

        pathList.clear()
        textList.clear()
        currentPath = Path()
    }

    fun copyFrom(imageFramesStore: ImageFramesStore) {
        scaleFactor = imageFramesStore.scaleFactor
        startOffset = imageFramesStore.startOffset
        matrix = imageFramesStore.matrix
        colorMatrix = imageFramesStore.colorMatrix
        pseudoColor = imageFramesStore.pseudoColor
        measure = imageFramesStore.measure
        linePaint = imageFramesStore.linePaint
        stringPaint = imageFramesStore.stringPaint

        pathList.clear()
        pathList.addAll(imageFramesStore.pathList)
        textList.clear()
        textList.addAll(imageFramesStore.textList)
        currentPath = imageFramesStore.currentPath

        currentIndex = imageFramesStore.currentIndex
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

    fun reverseColor() {
        val newColorMatrix = ColorMatrix(colorMatrix)
        newColorMatrix.postConcat(reverseMatrix)
        colorMatrix = newColorMatrix
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

    fun getAnimation(resources: Resources, duration: Int): IndexListenableAnimationDrawable {
        startOffset = currentIndex
        val animation = IndexListenableAnimationDrawable()
        animation.isOneShot = true
        framesModel.frames.subList(startOffset, framesModel.size).forEachIndexed { index, _ ->
            val bitmap = BitmapDrawable(resources, getFrame(currentIndex + index))
            bitmap.colorFilter = ColorMatrixColorFilter(colorMatrix)
            animation.addFrame(bitmap, duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        getDelegate(animation::currentIndex)?.onChange {
            currentIndex = (startOffset + it.second) % framesModel.size
        }
        return animation
    }

    fun resetAnimation(imageView: ImageView): IndexListenableAnimationDrawable {
        val animation = getAnimation(imageView.resources, duration)

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