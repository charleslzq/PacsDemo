package com.github.charleslzq.pacsdemo

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.github.charleslzq.pacsdemo.image.*
import com.github.charleslzq.pacsdemo.image.gesture.ImageAnimationGestureListener
import com.github.charleslzq.pacsdemo.image.gesture.ImageScaleGestureListener
import com.github.charleslzq.pacsdemo.image.gesture.ImageSlideGestureListener

/**
 * Created by charleslzq on 17-11-24.
 */
class ImageCellViewHolder(
        private val baseView: View,
        private val progress: TextView = baseView.findViewById(R.id.imageProgress),
        val image: ImageListView = baseView.findViewById(R.id.imagesContainer),
        val scaleBar: SeekBar = baseView.findViewById(R.id.imageScaleBar),
        val imageGestureListener: ImageListViewGestureListener = ImageListViewGestureListener(image)
) {
    fun bindData(seriesViewModel: PatientSeriesViewModel, mode: PresentationMode = PresentationMode.SLIDE) {
        val imageUrls = seriesViewModel.imageUrls
        if (imageUrls.isEmpty()) {
            throw IllegalArgumentException("No Image Found!")
        }

        image.presentationMode = mode
        image.bindUrls(imageUrls)

        if (imageUrls.size > 1) {
            image.imageFramesState.indexChangeListener = this::setProgress
        }
        setProgress(0)
        progress.visibility = View.VISIBLE

        when(image.presentationMode) {
            PresentationMode.ANIMATE -> {
                image.imageFramesState.finishListener = {
                    if (image.isRunning()) {
                        image.reset()
                    }
                }
                imageGestureListener.listModeGestureListener = ImageAnimationGestureListener(image)
                imageGestureListener.listModeScaleGestureListener = ImageScaleGestureListener(image)
                imageGestureListener.toListMode()
                image.setOnTouchListener(imageGestureListener)
                scaleBar.visibility = View.INVISIBLE
            }
            PresentationMode.SLIDE -> {
                imageGestureListener.listModeGestureListener = ImageSlideGestureListener(image)
                imageGestureListener.listModeScaleGestureListener = ImageScaleGestureListener(image)
                imageGestureListener.toListMode()
                image.setOnTouchListener(imageGestureListener)
                scaleBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setProgress(newIndex: Int) {
        val newProgress = newIndex + 1
        progress.post({
            progress.text = "$newProgress / ${image.imageFramesState.size}"
        })
    }
}