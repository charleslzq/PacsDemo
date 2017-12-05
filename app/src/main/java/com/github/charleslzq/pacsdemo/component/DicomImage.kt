package com.github.charleslzq.pacsdemo.component

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Path
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.RequireRedrawCanvas
import com.github.charleslzq.pacsdemo.component.gesture.*
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.support.IndexListenableAnimationDrawable

/**
 * Created by charleslzq on 17-11-27.
 */
class DicomImage(
        imageView: ImageView,
        imageFramesViewState: ImageFramesViewState
) : Component<ImageView, ImageFramesViewState>(imageView, imageFramesViewState) {
    var operationMode: OperationMode = PlayMode(view.context, NoOpCompositeGestureListener())
        set(value) {
            field = value
            view.setOnTouchListener(operationMode)
        }
    lateinit var canvas: Canvas

    init {
        EventBus.onEvent<DragEventMessage.StartCopyCell> { onDragStart(it) }
        EventBus.onEvent<RequireRedrawCanvas> { redrawCanvas() }

        onStateChange(state::framesModel) {
            state.reset()
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

    private fun onDragStart(dragCopyCellMessage: DragEventMessage.StartCopyCell) {
        if (dragCopyCellMessage.layoutPosition == state.layoutPosition) {
            val dragBuilder = View.DragShadowBuilder(view)
            val clipDataItem = ClipData.Item(tag, state.layoutPosition.toString())
            val clipData = ClipData(tag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipDataItem)
            @Suppress("DEPRECATION")
            view.startDrag(clipData, dragBuilder, null, 0)
        }
    }

    private fun init() {
        operationMode = PlayMode(view.context, PlayModeGestureListener(state))

        val firstImage = state.getScaledFrame(0)
        view.clearAnimation()
        view.background = null
        view.setImageBitmap(firstImage)

        onStateChange(state::measure) {
            state.currentPath = Path()
            state.firstPath = true
            operationMode = when (state.measure != ImageFramesViewState.Measure.NONE && state.framesModel.frames.isNotEmpty()) {
                true -> {
                    MeasureMode(view.context, MeasureModeGestureListener(state))
                }
                false -> {
                    state.pathList.clear()
                    state.textList.clear()
                    if (state.scaleFactor > 1.0f) {
                        StudyMode(view.context, StudyModeGestureListener(view.layoutParams.width, view.layoutParams.height, state))
                    } else {
                        PlayMode(view.context, PlayModeGestureListener(state))
                    }
                }
            }
            redrawCanvas()
        }

        onStateChange(state::colorMatrix) {
            if (state.playing) {
                state.playing = false
            }
            view.colorFilter = ColorMatrixColorFilter(state.colorMatrix)
        }

        onStateChange(state::currentIndex) {
            if (!state.playing && state.framesModel.size > 0) {
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

    private fun redrawCanvas() {
        val bitmap = state.getScaledFrame(state.currentIndex)
        canvas = Canvas(bitmap)
        view.setImageBitmap(bitmap)

        state.pathList.forEach {
            canvas.drawPath(it, state.linePaint)
        }

        canvas.drawPath(state.currentPath, state.linePaint)

        state.textList.forEach {
            canvas.drawText(it.second, it.first.x, it.first.y, state.stringPaint)
        }

        view.invalidate()
    }

    companion object {
        val tag = "imageCell"
    }
}