package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.ColorMatrixColorFilter
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.gesture.*
import com.github.charleslzq.pacsdemo.component.state.ImageFramesModel
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.support.IndexListenableAnimationDrawable

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageView: ImageView,
        val position: Int,
        pacsViewState: PacsViewState
) : PacsComponentFragment<ImageView, ImageFramesViewState>(imageView, pacsViewState, { it.imageCells[position] }) {
    val tag = "image$position"
    var dataPosition = -1
    var operationMode: OperationMode = PlayMode(view.context, NoOpCompositeGestureListener())
        set(value) {
            field.unregister()
            field = value
            field.register()
            view.setOnTouchListener(operationMode)
        }

    init {
        onStateChange(state::framesModel) {
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

    private fun onDrag() {
        val dragBuilder = View.DragShadowBuilder(view)
        val clipDataItem = ClipData.Item(tag, dataPosition.toString())
        val clipData = ClipData(tag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipDataItem)
        @Suppress("DEPRECATION")
        view.startDrag(clipData, dragBuilder, null, 0)
        state.framesModel = ImageFramesModel()
    }

    private fun init() {
        operationMode = PlayMode(view.context, PlayModeGestureListener(state, this::onDrag))

        val firstImage = state.getScaledFrame(0)
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(firstImage)

        onStateChange(state::measureLine) {
            operationMode = when (state.measureLine && state.framesModel.frames.isNotEmpty()) {
                true -> {
                    MeasureMode(view.context, MeasureModeGestureListener(view, state))
                }
                false -> {
                    if (state.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, state))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(state, this::onDrag))
                    }
                }
            }
        }

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
                operationMode = PlayMode(view.context, PlayModeGestureListener(state, this::onDrag))
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