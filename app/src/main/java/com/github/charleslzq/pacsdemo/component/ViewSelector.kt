package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.*
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.support.ViewUtils

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelector(
        viewFlipper: ViewFlipper,
        pacsViewState: PacsViewState
) : PacsComponentGroup<ViewFlipper>(viewFlipper, pacsViewState, listOf(
        Sub(ImageCell::class, this::getImageCellsFromPanel, { parentState, index -> parentState.imageCells[index] })
)) {
    init {
        onStateChange(state::layoutOption) {
            state.resetImageStates()
            rebind()
        }
    }

    companion object {
        private fun getImageCellsFromPanel(view: ViewFlipper): List<View> {
            val displayedChild = view.getChildAt(view.displayedChild)
            return when (PacsViewState.LayoutOption.values()[view.displayedChild]) {
                PacsViewState.LayoutOption.ONE_ONE -> {
                    listOf(displayedChild)
                }
                PacsViewState.LayoutOption.ONE_TWO -> {
                    ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                }
                else -> {
                    ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                            .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                }
            }
        }
    }
}