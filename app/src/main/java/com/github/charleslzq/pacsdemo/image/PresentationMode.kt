package com.github.charleslzq.pacsdemo.image

/**
 * Created by charleslzq on 17-11-24.
 */
enum class PresentationMode {
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
                imageListView.presentationMode = SLIDE
                imageListView.presentationMode.init(imageListView)
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
            imageListView.setImageBitmap(imageListView.imageFramesState.getScaledFrame(index))
        }

        override fun init(imageListView: ImageListView) {
            val firstImage = imageListView.imageFramesState.getScaledFrame(0)
            imageListView.clearAnimation()
            imageListView.background = null
            imageListView.setImageBitmap(firstImage)
        }
    };

    abstract fun init(imageListView: ImageListView)
    open fun play(imageListView: ImageListView) {
        throw UnsupportedOperationException()
    }

    open fun pause(imageListView: ImageListView) {
        throw UnsupportedOperationException()
    }

    open fun isRunning(imageListView: ImageListView): Boolean {
        throw UnsupportedOperationException()
    }

    open fun reset(imageListView: ImageListView) {
        throw UnsupportedOperationException()
    }

    open fun nextPage(imageListView: ImageListView) {
        throw UnsupportedOperationException()
    }

    open fun previousPage(imageListView: ImageListView) {
        throw UnsupportedOperationException()
    }

    abstract fun changeProgress(imageListView: ImageListView, progress: Int)
    protected fun newIndex(imageListView: ImageListView, progress: Int): Int {
        return (progress + imageListView.imageFramesState.size - 1) % imageListView.imageFramesState.size
    }
}