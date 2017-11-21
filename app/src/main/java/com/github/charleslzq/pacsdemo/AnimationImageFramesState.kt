package com.github.charleslzq.pacsdemo

import android.graphics.drawable.BitmapDrawable

/**
 * Created by charleslzq on 17-11-21.
 */
class AnimationImageFramesState(
        val frames: List<BitmapDrawable>,
        private val indexChangeListener: (Int) -> Unit,
        private val finishListener: () -> Unit
) {
    val size = frames.size

    var currentIndex: Int = 0
        set(value) {
            field = value % size
            indexChangeListener.invoke(field)
            if (isFinish()) {
                finishListener.invoke()
            }
        }

    fun isFinish() = currentIndex == size - 1

    fun getAnimation(duration: Int): IndexListenableAnimationDrawable {
        val animation = IndexListenableAnimationDrawable(currentIndex, this::currentIndex::set)
        animation.isOneShot = true
        frames.subList(currentIndex, size).forEach {
            animation.addFrame(it, duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        return animation
    }
}