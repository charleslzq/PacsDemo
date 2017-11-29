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
    private val buttonPanelBinder = ButtonPanelBinder(view.findViewById(R.id.buttonPanel))

    init {
        onNewModel {
            thumbListViewBinder.model = model
            viewSelectorBinder.model = model
            buttonPanelBinder.model = model
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
        val selected = model.selected
        val layoutOption = model.layoutOption
        val seriesList = model.seriesList
        if (layoutOption == PacsDemoViewModel.LayoutOption.ONE_ONE && selected >= 0 && selected < seriesList.size) {
            imageProgressBarBinder.model = seriesList[selected].imageFramesViewModel
        } else {
            imageProgressBarBinder.reset()
        }
    }
}