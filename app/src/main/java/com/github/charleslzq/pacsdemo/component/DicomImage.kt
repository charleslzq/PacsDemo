package com.github.charleslzq.pacsdemo.component

import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.IndexListenableAnimationDrawable
import com.github.charleslzq.pacsdemo.component.gesture.*
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageView: ImageView
) : Component<ImageView, ImageFramesViewState>(imageView, { ImageFramesViewState() }) {
    var operationMode: OperationMode = PlayMode(view.context, NoOpCompositeGestureListener())
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }

    init {
        onNewState {
            if (state.framesModel.size != 0) {
                state.autoAdjustScale(view)
                init()
            } else {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(null)
            }
        }
    }

    private fun init() {
        operationMode = PlayMode(view.context, PlayModeGestureListener(state))

        val firstImage = state.getScaledFrame(0)
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(firstImage)

        onStateChange(state::colorMatrix) {
            if (state.playing) {
                state.playing = false
            }
            view.colorFilter = ColorMatrixColorFilter(state.colorMatrix)
        }

        onStateChange(state::currentIndex) {
            if (!state.playing) {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(state.getScaledFrame(state.currentIndex))
            }
        }
        onStateChange(state::matrix) {
            view.imageMatrix = state.matrix
        }
        onStateChange(state::scaleFactor) {
            if (state.scaleFactor > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, state))
            } else if (state.scaleFactor == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(state))
            }
        }
        onStateChange(state::playing) {
            when (state.playing) {
                true -> view.post(state.resetAnimation(view))
                false -> {
                    if (view.background != null) {
                        val animation = view.background as IndexListenableAnimationDrawable
                        animation.stop()
                        view.clearAnimation()
                        view.background = null
                    }
                    view.setImageBitmap(state.getScaledFrame(state.currentIndex))
                }
            }
        }
        onStateChange(state::pseudoColor) {
            if (state.playing) {
                state.playing = false
            } else {
                view.setImageBitmap(state.getScaledFrame(state.currentIndex))
            }
        }
    }
}