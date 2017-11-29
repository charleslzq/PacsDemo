package com.github.charleslzq.pacsdemo.binder

import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.IndexListenableAnimationDrawable
import com.github.charleslzq.pacsdemo.gesture.*
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImageViewBinder(
        imageView: ImageView
) : ViewBinder<ImageView, ImageFramesViewModel>(imageView) {
    var operationMode: OperationMode = PlayMode(view.context, NoOpAllGestureListener())
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }

    init {
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(null)

        onNewModel { newModel ->
            if (newModel != null && newModel.size != 0) {
                newModel.autoAdjustScale(view)
                init(newModel, view)
            } else {
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(null)
            }
        }
    }

    private fun init(newModel: ImageFramesViewModel, imageView: ImageView) {
        operationMode = PlayMode(view.context, PlayModeGestureListener(view.layoutParams.width, view.layoutParams.height, newModel))

        val firstImage = newModel.getScaledFrame(0)
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(firstImage)

        onModelChange(newModel::colorMatrix) { _, newMatrix ->
            if (newModel.playing) {
                newModel.playing = false
            }
            imageView.colorFilter = ColorMatrixColorFilter(newMatrix)
        }

        onModelChange(newModel::currentIndex) { _, newIndex ->
            if (!newModel.playing) {
                imageView.clearAnimation()
                imageView.background = null
                imageView.setImageBitmap(newModel.getScaledFrame(newIndex))
            }
        }
        onModelChange(newModel::matrix) { _, newMatrix ->
            view.imageMatrix = newMatrix
        }
        onModelChange(newModel::scaleFactor) { _, newScale ->
            if (newScale > 1 && operationMode is PlayMode) {
                operationMode = StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, newModel))
            } else if (newScale == 1.0f && operationMode is StudyMode) {
                operationMode = PlayMode(view.context, PlayModeGestureListener(view.layoutParams.width, view.layoutParams.height, newModel))
            }
        }
        onModelChange(newModel::playing) { _, newStatus ->
            when (newStatus) {
                true -> imageView.post(newModel.resetAnimation(imageView))
                false -> {
                    val animation = imageView.background as IndexListenableAnimationDrawable
                    animation.stop()
                    imageView.clearAnimation()
                    imageView.background = null
                    imageView.setImageBitmap(newModel.getScaledFrame(newModel.currentIndex))
                }
            }
        }
        onModelChange(newModel::pseudoColor) { _, _ ->
            if (newModel.playing) {
                newModel.playing = false
            } else {
                imageView.setImageBitmap(newModel.getScaledFrame(newModel.currentIndex))
            }
        }
    }
}