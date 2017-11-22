package com.github.charleslzq.pacsdemo

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import java.net.URI

/**
 * Created by charleslzq on 17-11-22.
 */
class ImageListView(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
) : ImageView(context, attrs, defStyleAttr), ProgressControllable {
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    lateinit var imageFramesState: ImageFramesState
    var duration: Int = 40
    private var animate = false

    fun bindUrls(
            imageUrls: List<URI>,
            animate: Boolean = false,
            indexChangeListener: (Int) -> Unit = {},
            finishListener: (ImageListView) -> Unit = { it.reset() }) {
        imageFramesState = ImageFramesState(imageUrls, indexChangeListener, {
            finishListener.invoke(this)
        })

        val firstImage = imageFramesState.getFrame(0)
        layoutParams.width = Math.ceil(measuredHeight * firstImage.height / firstImage.width.toDouble()).toInt()
        requestLayout()

        if (imageFramesState.size > 1 && animate) {
            this.animate = true
            setImageBitmap(null)
            resetAnimation()
        } else {
            this.animate = false
            clearAnimation()
            background = null
            setImageBitmap(firstImage)
        }
    }

    override fun play() {
        if (animate) {
            post(resetAnimation())
        }
    }

    override fun pause() {
        if (animate) {
            val animation = background as IndexListenableAnimationDrawable
            if (animation.isRunning) {
                animation.stop()
                animation.selectDrawable(animation.currentIndex)
            }
        }
    }

    override fun reset() {
        if (animate) {
            imageFramesState.currentIndex = 0
            resetAnimation()
        }
    }

    override fun isRunning(): Boolean {
        return animate && background != null && (background as IndexListenableAnimationDrawable).isRunning
    }

    override fun changeProgress(progress: Int) {
        val index = (progress + imageFramesState.size - 1) % imageFramesState.size
        when (animate) {
            true -> {
                val animation = background as IndexListenableAnimationDrawable
                if (animation.isRunning) {
                    animation.stop()
                }
                imageFramesState.currentIndex = index
                resetAnimation()
            }
            false -> {
                clearAnimation()
                setImageBitmap(imageFramesState.getFrame(index))
            }
        }
    }

    private fun getListAnimation(): IndexListenableAnimationDrawable {
        return imageFramesState.getAnimation(resources, duration)
    }

    private fun resetAnimation(): IndexListenableAnimationDrawable {
        setImageBitmap(null)
        clearAnimation()
        val animation = getListAnimation()
        background = animation
        return animation
    }
}