package com.github.charleslzq.pacsdemo.image

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
) : ImageView(context, attrs, defStyleAttr), ProgressControllable, PageControllable {
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    lateinit var imageFramesState: ImageFramesState
    var duration: Int = 40
    var presentationMode = PresentationMode.SLIDE
    var operationMode: OperationMode = ListMode(context)
        set(value) {
            if (field != value) {
                field = value
                this.setOnTouchListener(field)
            }
        }

    init {
        this.setOnTouchListener(operationMode)
    }

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
                layoutParams.width = Math.ceil(measuredHeight * firstImage.height / firstImage.width.toDouble()).toInt()
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

    private fun getListAnimation(): IndexListenableAnimationDrawable {
        return imageFramesState.getAnimation(resources, duration)
    }
}