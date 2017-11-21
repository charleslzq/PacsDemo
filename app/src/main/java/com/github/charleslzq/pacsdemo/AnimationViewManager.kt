package com.github.charleslzq.pacsdemo

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-20.
 */
class AnimationViewManager(
        private val imageView: ImageView,
        imageUriList: List<URI>,
        var indexChangeListener: (Int) -> Unit = {},
        var finishedListener: (AnimationViewManager) -> Unit = { it.reset() }
) {
    private val animationState = AnimationImageFramesState(imageUriList.map {
        BitmapDrawable(imageView.resources, BitmapFactory.decodeFile(File(it).absolutePath))
    }, indexChangeListener, {
        finishedListener.invoke(this)
    })
    var duration: Int = 40
    val numOfFrames = animationState.size

    init {
        val firstImage = animationState.frames[0].bitmap
        imageView.layoutParams.width = Math.ceil(imageView.measuredHeight * firstImage.height / firstImage.width.toDouble()).toInt()
        imageView.requestLayout()

        if (animationState.size == 1) {
            imageView.clearAnimation()
            imageView.background = null
            imageView.setImageBitmap(firstImage)
        } else {
            imageView.setImageBitmap(null)
            resetAnimation()
        }
    }

    fun isRunning() = (imageView.background as IndexListenableAnimationDrawable).isRunning

    fun pause() {
        val animation = imageView.background as IndexListenableAnimationDrawable
        if (animation.isRunning) {
            animation.stop()
            animation.selectDrawable(animation.currentIndex)
        }
    }

    fun resume() {
        imageView.post(resetAnimation())
    }

    fun stop() {
        val animation = imageView.background as IndexListenableAnimationDrawable
        if (animation.isRunning) {
            animation.stop()
        }
        reset()
    }

    private fun resetAnimation(): IndexListenableAnimationDrawable {
        imageView.setImageBitmap(null)
        imageView.clearAnimation()
        val animation = animationState.getAnimation(duration)
        imageView.background = animation
        return animation
    }

    fun changePosition(index: Int) {
        val animation = imageView.background as IndexListenableAnimationDrawable
        if (animation.isRunning) {
            animation.stop()
        }
        animationState.currentIndex = index
        resetAnimation()
    }

    private fun reset() {
        animationState.currentIndex = 0
        resetAnimation()
    }
}