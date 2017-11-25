package com.github.charleslzq.pacsdemo

import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.github.charleslzq.pacsdemo.image.ImageListView
import com.github.charleslzq.pacsdemo.image.ImageListViewGestureListener
import com.github.charleslzq.pacsdemo.image.PresentationMode
import com.github.charleslzq.pacsdemo.image.gesture.ImageAnimationGestureListener
import com.github.charleslzq.pacsdemo.image.gesture.ImageModeGestureListener
import com.github.charleslzq.pacsdemo.image.gesture.ImageScaleGestureListener
import com.github.charleslzq.pacsdemo.image.gesture.ImageSlideGestureListener
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import com.github.charleslzq.pacsdemo.observe.ObserverUtil

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

        imageGestureListener.imageModeGestureListener = ImageModeGestureListener(image)
        imageGestureListener.listModeScaleGestureListener = ImageScaleGestureListener(image)
        ObserverUtil.register(image.imageFramesState::currentIndex, { newValue ->
            Log.i("index", "$newValue received")
        })

        when (image.presentationMode) {
            PresentationMode.ANIMATE -> {
                image.imageFramesState.finishListener = {
                    if (image.isRunning()) {
                        image.reset()
                    }
                }
                imageGestureListener.listModeGestureListener = ImageAnimationGestureListener(image)
                imageGestureListener.toListMode()
                image.setOnTouchListener(imageGestureListener)
                scaleBar.visibility = View.INVISIBLE
            }
            PresentationMode.SLIDE -> {
                imageGestureListener.listModeGestureListener = ImageSlideGestureListener(image)
                imageGestureListener.toListMode()
                scaleBar.max = 40
                scaleBar.progress = 0
                scaleBar.visibility = View.VISIBLE
                scaleBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val newScale = (1 + progress.toFloat() / 10)
                            val scaleRatio = newScale / image.imageFramesState.scaleFactor
                            image.imageFramesState.scaleFactor = newScale
                            Log.i("scale", "$newScale, ${image.savedMatrix}")
                            image.savedMatrix.postScale(scaleRatio, scaleRatio)
                            image.imageMatrix = image.savedMatrix
                            image.invalidate()
                            if (newScale > 1) {
                                imageGestureListener.toImageMode()
                            } else if (newScale == 1.0f) {
                                imageGestureListener.toListMode()
                            }
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }

                })

                image.imageFramesState.scaleChangeListener = {
                    image.changeProgress(image.imageFramesState.currentIndex + 1)
                    val newProgress = ((image.imageFramesState.scaleFactor - 1) * 10).toInt()
                    scaleBar.progress = newProgress
                }
            }
        }
        image.setOnTouchListener(imageGestureListener)
    }

    private fun setProgress(newIndex: Int) {
        val newProgress = newIndex + 1
        progress.post({
            progress.text = "$newProgress / ${image.imageFramesState.size}"
        })
    }
}