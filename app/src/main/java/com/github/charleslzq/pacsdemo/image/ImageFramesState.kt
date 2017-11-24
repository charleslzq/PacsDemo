package com.github.charleslzq.pacsdemo.image

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-21.
 */
class ImageFramesState(
        val frames: List<URI>
) {
    val size = frames.size
    var indexChangeListener: (Int) -> Unit = {}
    var finishListener: () -> Unit = {}

    var currentIndex: Int = 0
        set(value) {
            field = value % size
            indexChangeListener.invoke(field)
            if (isFinish()) {
                finishListener.invoke()
            }
        }

    fun isFinish() = currentIndex == size - 1

    fun getFrame(index: Int) = BitmapFactory.decodeFile(File(frames[index % size]).absolutePath)

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