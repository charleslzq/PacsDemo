package com.github.charleslzq.pacsdemo.binder

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.binder.vo.PatientSeriesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCellViewBinder(
        baseView: View
) : ViewBinder<View, PatientSeriesViewModel>(baseView, { PatientSeriesViewModel() }) {
    private val progressTextBinder = ProgressTextViewBinder(baseView.findViewById(R.id.imageProgress))
    private val imageBinder = DicomImageViewBinder(baseView.findViewById(R.id.imagesContainer))
    private val imageScaleBar = ImageScaleBarBinder(baseView.findViewById(R.id.imageScaleBar))

    init {
        onNewModel {
            progressTextBinder.model = model.imageFramesViewModel
            imageBinder.model = model.imageFramesViewModel
            imageScaleBar.model = model.imageFramesViewModel
        }
    }
}