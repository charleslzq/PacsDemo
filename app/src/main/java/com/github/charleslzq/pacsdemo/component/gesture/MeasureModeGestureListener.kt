package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-30.
 */
class MeasureModeGestureListener(
        val imageView: ImageView,
        framesViewState: ImageFramesViewState
) : ScaleCompositeGestureListener(framesViewState) {
    private lateinit var canvas: Canvas
    private lateinit var startPoint: PointF
    private var init = false
    private val historyLines: MutableList<Pair<PointF, PointF>> = mutableListOf()
    private val historyMeasure: MutableList<Pair<PointF, String>> = mutableListOf()

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint = getPoint(motionEvent)
                if (!init) {
                    resetCanvas()
                    init = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                resetCanvas()
                val currentPoint = getPoint(motionEvent)
                canvas.drawLine(startPoint.x, startPoint.y, currentPoint.x, currentPoint.y, framesViewState.linePaint)
                imageView.invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val currentPoint = getPoint(motionEvent)
                val length = length(startPoint, currentPoint)
                if (length > 5.0f) {
                    historyLines.add(startPoint to currentPoint)
                    val text = length.toString()
                    historyMeasure.add(currentPoint to text)
                    canvas.drawText(text, currentPoint.x, currentPoint.y, framesViewState.stringPaint)
                    imageView.invalidate()
                }
            }
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        historyMeasure.clear()
        historyLines.clear()
        resetCanvas()
        return true
    }

    private fun getPoint(motionEvent: MotionEvent): PointF {
        return PointF(motionEvent.x, motionEvent.y)
    }

    private fun length(point1: PointF, point2: PointF): Float {
        return Math.sqrt(((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y)).toDouble()).toFloat()
    }

    private fun resetCanvas() {
        val bitmap = framesViewState.getScaledFrame(framesViewState.currentIndex)
        canvas = Canvas(bitmap)
        imageView.setImageBitmap(bitmap)

        historyLines.forEach {
            canvas.drawLine(it.first.x, it.first.y, it.second.x, it.second.y, framesViewState.linePaint)
        }
        historyMeasure.forEach {
            canvas.drawText(it.second, it.first.x, it.first.x, framesViewState.stringPaint)
        }
    }
}