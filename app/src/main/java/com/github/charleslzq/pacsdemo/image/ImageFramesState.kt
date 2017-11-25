package com.github.charleslzq.pacsdemo.image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import java.io.File
import java.net.URI
import kotlin.properties.Delegates

/**
 * Created by charleslzq on 17-11-21.
 */
class ImageFramesState(
        val frames: List<URI>
) {
    val size = frames.size
    var rawScale = 1.0f
    var scaleFactor = 1.0f
        set(value) {
            if (field != value) {
                field = value
                scaleChangeListener.invoke(field)
            }
        }

    var currentIndex: Int by ObservablePropertyWithObservers(0)

    var indexChangeListener: (Int) -> Unit = {}
    var finishListener: () -> Unit = {}
    var scaleChangeListener: (Float) -> Unit = {}

    fun isFinish() = currentIndex == size - 1

    fun getFrame(index: Int) = BitmapFactory.decodeFile(File(frames[index % size]).absolutePath)

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
        val animation = IndexListenableAnimationDrawable(currentIndex, this::currentIndex::set)
        animation.isOneShot = true
        frames.subList(currentIndex, size).forEachIndexed { index, _ ->
            animation.addFrame(
                    BitmapDrawable(resources, getFrame(currentIndex + index)),
                    duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        return animation
    }
}