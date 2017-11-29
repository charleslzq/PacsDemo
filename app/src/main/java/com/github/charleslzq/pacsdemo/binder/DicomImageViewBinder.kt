package com.github.charleslzq.pacsdemo.binder

import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.IndexListenableAnimationDrawable
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel
import com.github.charleslzq.pacsdemo.gesture.*

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImageViewBinder(
        imageView: ImageView
) : ViewBinder<ImageView, ImageFramesViewModel>(imageView, { ImageFramesViewModel() }) {
    var operationMode: OperationMode = PlayMode(view.context, NoOpCompositeGestureListener())
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }

    init {
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(null)

        onNewModel {
            if (model.size != 0) {
                model.autoAdjustScale(view)
                init()
            } else {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(null)
            }
        }
    }

    private fun init() {
        operationMode = PlayMode(view.context, PlayModeGestureListener(model))

        val firstImage = model.getScaledFrame(0)
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(firstImage)

        onModelChange(model::colorMatrix) {
            if (model.playing) {
                model.playing = false
            }
            view.colorFilter = ColorMatrixColorFilter(model.colorMatrix)
        }

        onModelChange(model::currentIndex) {
            if (!model.playing) {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(model.getScaledFrame(model.currentIndex))
            }
        }
        onModelChange(model::matrix) {
            view.imageMatrix = model.matrix
        }
        onModelChange(model::scaleFactor) {
            if (model.scaleFactor > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, model))
            } else if (model.scaleFactor == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(model))
            }
        }
        onModelChange(model::playing) {
            when (model.playing) {
                true -> view.post(model.resetAnimation(view))
                false -> {
                    if (view.background != null) {
                        val animation = view.background as IndexListenableAnimationDrawable
                        animation.stop()
                        view.clearAnimation()
                        view.background = null
                    }
                    view.setImageBitmap(model.getScaledFrame(model.currentIndex))
                }
            }
        }
        onModelChange(model::pseudoColor) {
            if (model.playing) {
                model.playing = false
            } else {
                view.setImageBitmap(model.getScaledFrame(model.currentIndex))
            }
        }
    }
}