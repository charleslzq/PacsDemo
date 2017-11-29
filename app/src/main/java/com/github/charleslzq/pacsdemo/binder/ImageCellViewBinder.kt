package com.github.charleslzq.pacsdemo.binder

import android.view.DragEvent
import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel
import com.github.charleslzq.pacsdemo.binder.vo.PatientSeriesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCellViewBinder(
        baseView: View,
        requireBinding: (Int, Int) -> Unit
) : ViewBinder<View, PatientSeriesViewModel>(baseView, { PatientSeriesViewModel() }) {
    private val progressTextBinder = ProgressTextViewBinder(baseView.findViewById(R.id.imageProgress))
    val imageBinder = DicomImageViewBinder(baseView.findViewById(R.id.imagesContainer))
    var layoutPosition = -1

    init {
        view.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    requireBinding(layoutPosition, dragEvent.clipData.getItemAt(0).htmlText.toInt())
                }
            }
            true
        }

        onNewModel {
            imageBinder.model = ImageFramesViewModel(model.imageFramesModel)
            progressTextBinder.model = imageBinder.model
        }
    }
}