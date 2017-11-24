package com.github.charleslzq.pacsdemo.image

import android.content.Context
import android.graphics.Matrix
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
) : ImageView(context, attrs, defStyleAttr), ProgressControllable, PageControllable {
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    lateinit var imageFramesState: ImageFramesState
    var duration: Int = 40
    var presentationMode = PresentationMode.SLIDE
    var savedMatrix = Matrix()

    fun bindUrls(imageUrls: List<URI> = emptyList()) {
        imageFramesState = ImageFramesState(imageUrls)
        when (imageUrls.isEmpty()) {
            true -> {
                clearAnimation()
                background = null
                setImageBitmap(null)
            }
            false -> {
                val firstImage = imageFramesState.getFrame(0)
                savedMatrix.reset()
                imageFramesState.rawScale = getRawScaleFactor(firstImage.width, firstImage.height)
                requestLayout()
                presentationMode.init(this)
            }
        }
    }

    fun resetAnimation(): IndexListenableAnimationDrawable {
        setImageBitmap(null)
        clearAnimation()
        val animation = getListAnimation()
        background = animation
        return animation
    }

    override fun play() {
        presentationMode.play(this)
    }

    override fun pause() {
        presentationMode.pause(this)
    }

    override fun reset() {
        presentationMode.reset(this)
    }

    override fun isRunning(): Boolean {
        return presentationMode.isRunning(this)
    }

    override fun changeProgress(progress: Int) {
        presentationMode.changeProgress(this, progress)
    }

    override fun nextPage() {
        presentationMode.nextPage(this)
    }

    override fun previousPage() {
        presentationMode.previousPage(this)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun getRawScaleFactor(imageWidth: Int, imageHeight: Int): Float {
        val ratio = imageWidth.toFloat() / imageHeight.toFloat()
        val desiredWidth = Math.ceil((measuredHeight * ratio).toDouble()).toInt()
        return if (desiredWidth <= measuredWidth) {
            layoutParams.width = desiredWidth
            desiredWidth.toFloat() / imageWidth
        } else {
            val desiredHeight = Math.ceil((measuredWidth / ratio).toDouble()).toInt()
            layoutParams.height = desiredHeight
            desiredHeight.toFloat() / imageHeight
        }
    }

    private fun getListAnimation(): IndexListenableAnimationDrawable {
        return imageFramesState.getAnimation(resources, duration)
    }
}