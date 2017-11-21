package com.github.charleslzq.pacsdemo

import android.content.res.Resources
import android.graphics.BitmapFactory
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
        private val resources: Resources,
        private val imageView: ImageView,
        private val seekBar: SeekBar,
        imageUriList: List<URI>
) {
    private val animationState = AnimationImageFramesState(imageUriList, this::setSeekBarIndex, this::reset)
    var duration: Int = 40

    init {
        val firstImage = BitmapFactory.decodeFile(File(animationState.frameUrls[0]).absolutePath)
        imageView.layoutParams.width = Math.ceil(imageView.measuredHeight * firstImage.height / firstImage.width.toDouble()).toInt()
        imageView.requestLayout()


        if (animationState.size == 1) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(File(animationState.frameUrls[0]).absolutePath))
        } else {
            imageView.setImageBitmap(null)
            resetAnimation()
            imageView.setOnClickListener {
                val animation = imageView.background as IndexListenableAnimationDrawable
                when (animation.isRunning) {
                    true -> {
                        animation.stop()
                        animation.selectDrawable(animation.currentIndex)
                    }
                    false -> {
                        imageView.post(resetAnimation())
                    }
                }
            }

            seekBar.setOnSeekBarChangeListener(AnimationSeekBarListener(this::progressChanged))
            seekBar.max = animationState.size
            seekBar.visibility = View.VISIBLE
        }
    }

    private fun resetAnimation(): IndexListenableAnimationDrawable {
        imageView.setImageBitmap(null)
        imageView.clearAnimation()
        val animation = animationState.getAnimation(resources, duration)
        imageView.background = animation
        return animation
    }

    private fun progressChanged(index: Int) {
        val animation = imageView.background as IndexListenableAnimationDrawable
        if (animation.isRunning) {
            animation.stop()
        }
        animationState.currentIndex = index
        resetAnimation()
    }

    private fun setSeekBarIndex(index: Int) {
        seekBar.progress = index + 1
    }

    private fun reset() {
        animationState.currentIndex = 0
        resetAnimation()
    }

    class AnimationSeekBarListener(
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