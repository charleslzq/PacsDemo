package com.github.charleslzq.pacsdemo

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-20.
 */
class AnimationViewManager(
        private val resources: Resources,
        private val imageUriList: List<URI>,
        private val imageView: ImageView,
        var duration: Int
) {
    private val size = imageUriList.size
    private var lastStartIndex = 0

    init {
        if (imageUriList.size == 1) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(File(imageUriList[0]).absolutePath))
        } else {
            val originalAnimation = getAnimation(0)
            originalAnimation.selectDrawable(0)
            imageView.setImageBitmap(null)
            imageView.background = originalAnimation
            imageView.setOnClickListener {
                val background = imageView.background
                when(background) {
                    is ControllableAnimationDrawable -> when(background.isRunning) {
                        true -> {
                            background.stop()
                            background.selectDrawable(background.currentIndex)
                        }
                        false -> {
                            lastStartIndex += background.currentIndex
                            Log.i("test", "Start at $lastStartIndex")
                            val newAnimation = getAnimation(lastStartIndex)
                            imageView.clearAnimation()
                            imageView.background = newAnimation
                            imageView.post(newAnimation)
                        }
                    }
                }
            }
        }
    }


    private fun getAnimation(startIndex: Int): ControllableAnimationDrawable {
        when (startIndex) {
            in 0..(size-2) -> return getAnimation(imageUriList.subList(startIndex, size-1))
            else -> throw IllegalArgumentException("Wrong Index")
        }
    }

    private fun getAnimation(imageUrls: List<URI>): ControllableAnimationDrawable {
        val animation = ControllableAnimationDrawable()
        animation.isOneShot = true
        imageUrls.forEach {
            val bitmapDrawable = BitmapDrawable(resources, BitmapFactory.decodeFile(File(it).absolutePath))
            animation.addFrame(bitmapDrawable, duration)
        }
        animation.callback = null
        return animation
    }
}