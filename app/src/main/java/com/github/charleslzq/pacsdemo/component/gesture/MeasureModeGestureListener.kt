package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.Canvas
import android.graphics.Path
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
    private var path = Path()
    private var init = false
    private val pathList = mutableListOf<Path>()
    private val textList = mutableListOf<Pair<PointF, String>>()

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint = getPoint(motionEvent)
                if (!init) {
                    resetCanvas()
                    init = true
                }
                path.moveTo(startPoint.x, startPoint.y)
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPoint = getPoint(motionEvent)
                path.lineTo(currentPoint.x, currentPoint.y)
                canvas.drawPath(path, framesViewState.linePaint)
                imageView.invalidate()
            }
            MotionEvent.ACTION_UP -> {
                resetCanvas()
                val currentPoint = getPoint(motionEvent)
                val length = length(startPoint, currentPoint)
                if (length > 5.0f) {
                    val text = length.toString()
                    path.reset()
                    path.moveTo(startPoint.x, startPoint.y)
                    path.lineTo(currentPoint.x, currentPoint.y)
                    canvas.drawPath(path, framesViewState.linePaint)
                    canvas.drawText(text, currentPoint.x, currentPoint.y, framesViewState.stringPaint)
                    imageView.invalidate()

                    textList.add(currentPoint to text)
                    pathList.add(path)
                    path = Path()
                }
            }
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        pathList.clear()
        textList.clear()
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

        pathList.forEach {
            canvas.drawPath(it, framesViewState.linePaint)
        }

        textList.forEach {
            canvas.drawText(it.second, it.first.x, it.first.y, framesViewState.stringPaint)
        }
    }
}