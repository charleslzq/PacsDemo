package com.github.charleslzq.pacsdemo.binder

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.register
import com.github.charleslzq.pacsdemo.vo.PatientSeriesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageCellViewBinder(
        baseView: View
) : ViewBinder<View, PatientSeriesViewModel>(baseView) {
    private val progressTextBinder = ProgressTextViewBinder(baseView.findViewById(R.id.imageProgress))
    private val imageBinder = DicomImageViewBinder(baseView.findViewById(R.id.imagesContainer))

    init {
        register(this::model, { _, newModel ->
            if (newModel != null) {
                progressTextBinder.model = newModel.imageFramesViewModel
                imageBinder.model = newModel.imageFramesViewModel
            }
        })
    }
}