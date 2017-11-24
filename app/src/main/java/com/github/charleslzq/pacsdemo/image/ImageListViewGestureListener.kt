package com.github.charleslzq.pacsdemo.image

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Created by charleslzq on 17-11-24.
 */
class ImageListViewGestureListener(
        private val imageListView: ImageListView
) : View.OnTouchListener {

    var listModeGestureListener: GestureDetector.SimpleOnGestureListener = GestureDetector.SimpleOnGestureListener()
        set(value) {
            field = value
            if (operationMode is ListMode) {
                operationMode = ListMode(imageListView.context, listModeGestureListener, listModeScaleGestureListener)
            }
        }
    var listModeScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener = ScaleGestureDetector.SimpleOnScaleGestureListener()
        set(value) {
            field = value
            if (operationMode is ListMode) {
                operationMode = ListMode(imageListView.context, listModeGestureListener, listModeScaleGestureListener)
            }
        }

    var imageModeGestureListener: GestureDetector.SimpleOnGestureListener = GestureDetector.SimpleOnGestureListener()
        set(value) {
            field = value
            if (operationMode is ImageMode) {
                operationMode = ImageMode(imageListView.context, imageModeGestureListener, imageModeScaleGestureListener)
            }
        }
    var imageModeScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener = ScaleGestureDetector.SimpleOnScaleGestureListener()
        set(value) {
            field = value
            if (operationMode is ImageMode) {
                operationMode = ImageMode(imageListView.context, imageModeGestureListener, imageModeScaleGestureListener)
            }
        }

    var editModeGestureListener: GestureDetector.SimpleOnGestureListener = GestureDetector.SimpleOnGestureListener()
        set(value) {
            field = value
            if (operationMode is EditMode) {
                operationMode = EditMode(imageListView.context, editModeGestureListener, editModeScaleGestureListener)
            }
        }
    var editModeScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener = ScaleGestureDetector.SimpleOnScaleGestureListener()
        set(value) {
            field = value
            if (operationMode is EditMode) {
                operationMode = EditMode(imageListView.context, editModeGestureListener, editModeScaleGestureListener)
            }
        }

    var operationMode: OperationMode = ListMode(imageListView.context, listModeGestureListener, listModeScaleGestureListener)

    fun toListMode() {
        operationMode = ListMode(imageListView.context, listModeGestureListener, listModeScaleGestureListener)
    }

    fun toImageMode() {
        operationMode = ImageMode(imageListView.context, imageModeGestureListener, imageModeScaleGestureListener)
    }

    fun toEditMode() {
        operationMode = EditMode(imageListView.context, editModeGestureListener, editModeScaleGestureListener)
    }

    override fun onTouch(p0: View, p1: MotionEvent): Boolean {
        return operationMode.onTouch(p0, p1)
    }
}