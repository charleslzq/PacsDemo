package com.github.charleslzq.pacsdemo.binder

import android.graphics.ColorMatrixColorFilter
import android.util.Log
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.IndexListenableAnimationDrawable
import com.github.charleslzq.pacsdemo.gesture.*
import com.github.charleslzq.pacsdemo.gesture.PresentationMode.ANIMATE
import com.github.charleslzq.pacsdemo.gesture.PresentationMode.SLIDE
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
        operationMode = PlayMode(view.context, PlayModeGestureListener(view.width, view.height, newModel))

        onModelChange(newModel::colorMatrix) { _, newMatrix ->
            imageView.colorFilter = ColorMatrixColorFilter(newMatrix)
        }

        when (newModel.presentationMode) {
            ANIMATE -> {
                view.clearAnimation()
                view.setImageBitmap(null)
                newModel.resetAnimation(imageView)

                onModelChange(newModel::currentIndex) { _, _ ->
                    if (!newModel.playing) {
                        newModel.resetAnimation(imageView)
                    }
                }
                onModelChange(newModel::playing) { _, newStatus ->
                    Log.i("image play", "$newStatus")
                    when (newStatus) {
                        true -> imageView.post(newModel.resetAnimation(imageView))
                        false -> {
                            val animation = imageView.background as IndexListenableAnimationDrawable
                            animation.stop()
                            animation.selectDrawable(animation.currentIndex)
                        }
                    }
                }
            }
            SLIDE -> {
                val firstImage = newModel.getScaledFrame(0)
                view.clearAnimation()
                view.background = null
                view.setImageBitmap(firstImage)

                onModelChange(newModel::currentIndex) { _, newIndex ->
                    imageView.clearAnimation()
                    imageView.setImageBitmap(newModel.getScaledFrame(newIndex))
                }
                onModelChange(newModel::matrix) { _, newMatrix ->
                    view.imageMatrix = newMatrix
                }
                onModelChange(newModel::scaleFactor) { _, newScale ->
                    if (newScale > 1 && operationMode is PlayMode) {
                        operationMode = StudyMode(view.context, StudyModeGestureListenerPlayModeGestureListener(newModel))
                    }
                }
            }
        }
    }
}