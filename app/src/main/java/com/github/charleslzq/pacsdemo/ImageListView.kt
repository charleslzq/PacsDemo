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
) : ImageView(context, attrs, defStyleAttr), ProgressControllable, PageControllable {
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    lateinit var imageFramesState: ImageFramesState
    var duration: Int = 40
    var mode = Mode.SLIDE

    fun bindUrls(
            imageUrls: List<URI> = emptyList(),
            indexChangeListener: (Int) -> Unit = {},
            finishListener: (ImageListView) -> Unit = { it.reset() }) {
        imageFramesState = ImageFramesState(imageUrls, indexChangeListener, {
            finishListener.invoke(this)
        })
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
                mode.init(this)
            }
        }
    }

    override fun play() {
        mode.play(this)
    }

    override fun pause() {
        mode.pause(this)
    }

    override fun reset() {
        mode.reset(this)
    }

    override fun isRunning(): Boolean {
        return mode.isRunning(this)
    }

    override fun changeProgress(progress: Int) {
        mode.changeProgress(this, progress)
    }

    override fun nextPage() {
        mode.nextPage(this)
    }

    override fun previousPage() {
        mode.previousPage(this)
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

    enum class Mode {
        ANIMATE {
            override fun play(imageListView: ImageListView) {
                imageListView.post(imageListView.resetAnimation())
            }

            override fun pause(imageListView: ImageListView) {
                val animation = imageListView.background as IndexListenableAnimationDrawable
                if (animation.isRunning) {
                    animation.stop()
                    animation.selectDrawable(animation.currentIndex)
                }
            }

            override fun isRunning(imageListView: ImageListView): Boolean {
                return imageListView.background != null
                        && (imageListView.background as IndexListenableAnimationDrawable).isRunning
            }

            override fun reset(imageListView: ImageListView) {
                imageListView.imageFramesState.currentIndex = 0
                imageListView.resetAnimation()
            }

            override fun changeProgress(imageListView: ImageListView, progress: Int) {
                val index = newIndex(imageListView, progress)
                val animation = imageListView.background as IndexListenableAnimationDrawable
                if (animation.isRunning) {
                    animation.stop()
                }
                imageListView.imageFramesState.currentIndex = index
                imageListView.resetAnimation()
            }

            override fun init(imageListView: ImageListView) {
                if (imageListView.imageFramesState.size > 1) {
                    imageListView.setImageBitmap(null)
                    imageListView.resetAnimation()
                } else {
                    imageListView.mode = SLIDE
                    imageListView.mode.init(imageListView)
                }
            }
        },
        SLIDE {
            override fun nextPage(imageListView: ImageListView) {
                if (!imageListView.imageFramesState.isFinish()) {
                    changeProgress(imageListView, imageListView.imageFramesState.currentIndex + 2)
                }
            }

            override fun previousPage(imageListView: ImageListView) {
                if (imageListView.imageFramesState.currentIndex != 0) {
                    changeProgress(imageListView, imageListView.imageFramesState.currentIndex)
                }
            }

            override fun changeProgress(imageListView: ImageListView, progress: Int) {
                val index = newIndex(imageListView, progress)
                imageListView.clearAnimation()
                imageListView.imageFramesState.currentIndex = index
                imageListView.setImageBitmap(imageListView.imageFramesState.getFrame(index))
            }

            override fun init(imageListView: ImageListView) {
                val firstImage = imageListView.imageFramesState.getFrame(0)
                imageListView.clearAnimation()
                imageListView.background = null
                imageListView.setImageBitmap(firstImage)
            }
        };

        abstract fun init(imageListView: ImageListView)
        open fun play(imageListView: ImageListView) {}
        open fun pause(imageListView: ImageListView) {}
        open fun isRunning(imageListView: ImageListView) = false
        open fun reset(imageListView: ImageListView) {}
        open fun nextPage(imageListView: ImageListView) {}
        open fun previousPage(imageListView: ImageListView) {}
        abstract fun changeProgress(imageListView: ImageListView, progress: Int)
        protected fun newIndex(imageListView: ImageListView, progress: Int): Int {
            return (progress + imageListView.imageFramesState.size - 1) % imageListView.imageFramesState.size
        }
    }
}