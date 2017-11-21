package com.github.charleslzq.pacsdemo

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
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
    private lateinit var seekBar: SeekBar
    private var duration: Int = 40

    fun bind(view: ImageView, uriList: List<URI>, seekBar: SeekBar) {
        imageView = view
        imageUriList = uriList
        this.seekBar = seekBar
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
            seekBar.setOnSeekBarChangeListener(SeekBarListener(this::progressChanged))
            seekBar.max = size
            seekBar.visibility = View.VISIBLE
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
        val animation = ControllableAnimationDrawable(size, offset, this::setSeekBar)
        animation.isOneShot = true
        imageUrls.forEach {
            val bitmapDrawable = BitmapDrawable(resources, BitmapFactory.decodeFile(File(it).absolutePath))
            animation.addFrame(bitmapDrawable, duration)
        }
        animation.callback = null
        return animation
    }

    private fun progressChanged(index: Int) {
        val background = imageView.background
        if (background is ControllableAnimationDrawable && background.isRunning) {
            background.stop()
        } else if (background is ControllableAnimationDrawable) {
            background.currentIndex = 0
        }
        lastStartIndex = index
        val newAnimation = getAnimation(lastStartIndex)
        newAnimation.selectDrawable(0)
        imageView.clearAnimation()
        imageView.background = newAnimation
    }

    private fun setSeekBar(index: Int) {
        seekBar.progress = index + 1
    }

    class SeekBarListener(
            private val onUserChangeProgress: (Int) -> Unit
    ) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
            Log.i("Seek", "$fromUser: $progress")
            if (fromUser) {
                onUserChangeProgress.invoke(progress - 1)
            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
            Log.i("Seek", "start")
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            Log.i("Seek", "end")
        }

    }
}