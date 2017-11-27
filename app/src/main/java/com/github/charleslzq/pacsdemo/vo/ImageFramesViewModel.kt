package com.github.charleslzq.pacsdemo.vo

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.IndexListenableAnimationDrawable
import com.github.charleslzq.pacsdemo.gesture.PresentationMode
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.registerObserver
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-27.
 */
data class ImageFramesViewModel(
        private val frames: List<URI>
) {
    val size = frames.size
    var duration: Int = 40
    var scaleFactor: Float by ObservablePropertyWithObservers(1.0f)
    var currentIndex: Int by ObservablePropertyWithObservers(0)
    var startOffset: Int = 0
    var presentationMode = PresentationMode.SLIDE
        set(value) {
            if (value != PresentationMode.ANIMATE || frames.size > 1) {
                field = value
            }
        }
    var matrix by ObservablePropertyWithObservers(Matrix())
    var colorMatrix by ObservablePropertyWithObservers(ColorMatrix())
    var playing by ObservablePropertyWithObservers(false)

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

    fun autoAdjustScale(view: View) {
        if (frames.isNotEmpty()) {
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

    fun getFrame(index: Int) = BitmapFactory.decodeFile(File(frames[index]).absolutePath)

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
        frames.subList(startOffset, size).forEachIndexed { index, _ ->
            animation.addFrame(
                    BitmapDrawable(resources, getFrame(currentIndex + index)),
                    duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        registerObserver(animation::currentIndex, { _, newIndex ->
            currentIndex = (startOffset + newIndex) % size
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
}