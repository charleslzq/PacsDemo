package com.github.charleslzq.pacsdemo

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-21.
 */
class AnimationImageFramesState(
        val frameUrls: List<URI>,
        private val indexChangeListener: (Int) -> Unit,
        private val finishListener: () -> Unit
) {
    val size = frameUrls.size

    var currentIndex: Int = 0
        set(value) {
            field = value % size
            indexChangeListener.invoke(field)
            if (isFinish()) {
                finishListener.invoke()
            }
        }

    fun isFinish() = currentIndex == size - 1

    fun getAnimation(resources: Resources, duration: Int): IndexListenableAnimationDrawable {
        val animation = IndexListenableAnimationDrawable(currentIndex, this::currentIndex::set)
        animation.isOneShot = true
        frameUrls.subList(currentIndex, size).forEach {
            val bitmapDrawable = BitmapDrawable(resources, BitmapFactory.decodeFile(File(it).absolutePath))
            animation.addFrame(bitmapDrawable, duration)
        }
        animation.selectDrawable(0)
        animation.callback = null
        return animation
    }
}