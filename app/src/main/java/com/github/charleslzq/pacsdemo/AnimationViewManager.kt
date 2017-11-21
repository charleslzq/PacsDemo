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
        private val resources: Resources
) {
    private var size = 0
    private var lastStartIndex = 0
    private lateinit var imageUriList: List<URI>
    private lateinit var imageView: ImageView
    private var duration: Int = 40

    fun bind(view: ImageView, uriList: List<URI>) {
        imageView = view
        imageUriList = uriList
        size = imageUriList.size
        lastStartIndex = 0
        val firstImage = BitmapFactory.decodeFile(File(imageUriList[0]).absolutePath)
        imageView.layoutParams.width = Math.ceil(imageView.measuredHeight * firstImage.height / firstImage.width.toDouble()).toInt()
        imageView.requestLayout()
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
                            when (background.finish) {
                                true -> {
                                    val animation = getAnimation()
                                    imageView.clearAnimation()
                                    imageView.background = animation
                                    imageView.post(animation)
                                }
                                false -> {
                                    lastStartIndex = (lastStartIndex + background.currentIndex) % size
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
        }
    }


    private fun getAnimation(startIndex: Int = 0): ControllableAnimationDrawable {
        val start = startIndex % size
        return getAnimation(imageUriList.subList(start, size), start)
    }

    private fun getAnimation(imageUrls: List<URI>, offset: Int = 0): ControllableAnimationDrawable {
        val animation = ControllableAnimationDrawable(size, offset)
        animation.isOneShot = true
        imageUrls.forEach {
            val bitmapDrawable = BitmapDrawable(resources, BitmapFactory.decodeFile(File(it).absolutePath))
            animation.addFrame(bitmapDrawable, duration)
        }
        animation.callback = null
        return animation
    }
}