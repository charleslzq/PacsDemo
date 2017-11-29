package com.github.charleslzq.pacsdemo.binder

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.binder.vo.PacsDemoViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsMainViewBinder(
        mainView: View
) : ViewBinder<View, PacsDemoViewModel>(mainView, { PacsDemoViewModel() }) {
    private val thumbListViewBinder = ThumbListViewBinder(view.findViewById(R.id.thumbList))
    private val viewSelectorBinder = ViewSelectorBinder(view.findViewById(R.id.viewSelector))
    private val imageProgressBarBinder = ImageProgressBarBinder(view.findViewById(R.id.imageSeekBar))
    private val buttonPanelBinder = ButtonPanelBinder(view.findViewById(R.id.buttonPanel), { model.layoutOption = it })

    init {
        onNewModel {
            thumbListViewBinder.model = model
            viewSelectorBinder.model = model
            buttonPanelBinder.model = ButtonPanelBinder.ViewModel()
            onModelChange(model::selected) {
                resetProgress()
            }
            onModelChange(model::layoutOption) {
                when (model.layoutOption) {
                    PacsDemoViewModel.LayoutOption.ONE_ONE -> resetProgress()
                    else -> imageProgressBarBinder.reset()
                }
            }
        }
    }

    private fun resetProgress() {
        val layoutOption = model.layoutOption
        if (layoutOption == PacsDemoViewModel.LayoutOption.ONE_ONE) {
            val binder = viewSelectorBinder.binders[0]
            imageProgressBarBinder.model = binder.model.imageFramesViewModel
            val buttonViewModel = ButtonPanelBinder.ViewModel()
            buttonViewModel.layoutOption = model.layoutOption
            buttonViewModel.imageFramesViewModel = binder.model.imageFramesViewModel
            buttonPanelBinder.model = buttonViewModel
        } else {
            imageProgressBarBinder.reset()
        }
    }
}