package com.github.charleslzq.pacsdemo.binder

import android.widget.*
import com.github.charleslzq.pacsdemo.ViewUtils
import com.github.charleslzq.pacsdemo.binder.vo.PacsDemoViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelectorBinder(
        viewFlipper: ViewFlipper
) : ViewBinder<ViewFlipper, PacsDemoViewModel>(viewFlipper, { PacsDemoViewModel() }) {
    private lateinit var binders: List<ImageCellViewBinder>

    init {
        onNewModel {
            onModelChange(model::layoutOption) {
                viewFlipper.displayedChild = model.layoutOption.ordinal
                binders = getImageViewBindersFromPanel()
                binders.forEachIndexed { index, imageCellViewBinder ->
                    imageCellViewBinder.model.layoutPosition = index
                }
            }
        }
    }

    private fun getImageViewBindersFromPanel(): List<ImageCellViewBinder> {
        val displayedChild = view.getChildAt(view.displayedChild)
        return when (PacsDemoViewModel.LayoutOption.values()[view.displayedChild]) {
            PacsDemoViewModel.LayoutOption.ONE_ONE -> {
                listOf(ImageCellViewBinder(displayedChild, this::bind))
            }
            PacsDemoViewModel.LayoutOption.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .map { ImageCellViewBinder(it, this::bind) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .map { ImageCellViewBinder(it, this::bind) }
            }
        }
    }

    private fun bind(layoutPosition: Int, dataPosition: Int) {
        if (dataPosition >= 0 && dataPosition < model.seriesList.size && layoutPosition >= 0 && layoutPosition < binders.size) {
            val dataModel = model.seriesList[dataPosition]
            dataModel.imageFramesViewModel.allowPlay = binders.size == 1
            binders[layoutPosition].model = dataModel.copy()
        }
    }
}