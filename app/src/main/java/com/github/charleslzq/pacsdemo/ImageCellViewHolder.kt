package com.github.charleslzq.pacsdemo

import android.view.GestureDetector
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.github.charleslzq.pacsdemo.image.*

/**
 * Created by charleslzq on 17-11-24.
 */
class ImageCellViewHolder(
        private val baseView: View,
        private val progress: TextView = baseView.findViewById(R.id.imageProgress),
        val image: ImageListView = baseView.findViewById(R.id.imagesContainer),
        val scaleBar: SeekBar = baseView.findViewById(R.id.imageScaleBar)
) {
    fun bindData(seriesViewModel: PatientSeriesViewModel, mode: ImageListView.Mode = ImageListView.Mode.SLIDE) {
        val imageUrls = seriesViewModel.imageUrls
        if (imageUrls.isEmpty()) {
            throw IllegalArgumentException("No Image Found!")
        }
        val scaleGestureDetector = ScaleGestureDetector(baseView.context, ImageScaleGestureListener(image))

        image.mode = mode
        image.bindUrls(imageUrls)

        if (imageUrls.size > 1) {
            image.imageFramesState.indexChangeListener = this::setProgress
        }
        setProgress(0)
        progress.visibility = View.VISIBLE

        when(image.mode) {
            ImageListView.Mode.ANIMATE -> {
                image.imageFramesState.finishListener = {
                    if (image.isRunning()) {
                        image.reset()
                    }
                }
                val gestureDetector = GestureDetector(baseView.context, ImageAnimationGestureListener(image))
                image.setOnTouchListener(CompositeTouchEventListener(listOf(
                        { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
                        { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
                )))
                scaleBar.visibility = View.INVISIBLE
            }
            ImageListView.Mode.SLIDE -> {
                val gestureDetector = GestureDetector(baseView.context, ImageSlideGestureListener(image))
                image.setOnTouchListener(CompositeTouchEventListener(listOf(
                        { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
                        { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
                )))
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