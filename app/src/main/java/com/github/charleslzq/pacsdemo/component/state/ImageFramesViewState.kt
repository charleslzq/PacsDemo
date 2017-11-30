package com.github.charleslzq.pacsdemo.component.state

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.IndexListenableAnimationDrawable
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.registerObserver
import java.io.File


/**
 * Created by charleslzq on 17-11-27.
 */
data class ImageFramesViewState(
        var framesModel: ImageFramesModel = ImageFramesModel()
) {
    var duration: Int = 40
    var scaleFactor: Float by ObservablePropertyWithObservers(1.0f)
    var currentIndex: Int by ObservablePropertyWithObservers(0)
    var startOffset: Int = 0
    var matrix by ObservablePropertyWithObservers(Matrix())
    var colorMatrix by ObservablePropertyWithObservers(ColorMatrix())
    var playing by ObservablePropertyWithObservers(false)
    var pseudoColor by ObservablePropertyWithObservers(false)
    var allowPlay = false

    private var rawScale = 1.0f

    init {
        matrix.reset()
        registerObserver(this::scaleFactor, { oldScale, newScale ->
            val newMatrix = Matrix(matrix)
            val scale = newScale / oldScale
            newMatrix.postScale(scale, scale)
            matrix = newMatrix
        })
    }

    fun playable() = framesModel.size > 1 && allowPlay

    fun autoAdjustScale(view: View) {
        if (framesModel.frameUrls.isNotEmpty()) {
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
        val rawBitmap = BitmapFactory.decodeFile(File(framesModel.frameUrls[index]).absolutePath, BitmapFactory.Options().apply { inMutable = pseudoColor })
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
        registerObserver(animation::currentIndex, { _, newIndex ->
            currentIndex = (startOffset + newIndex) % framesModel.size
        })
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
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}