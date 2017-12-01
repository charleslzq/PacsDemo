package com.github.charleslzq.pacsdemo.component

import android.widget.*
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.support.ViewUtils

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelector(
        viewFlipper: ViewFlipper,
        pacsViewState: PacsViewState
) : PacsComponent<ViewFlipper>(viewFlipper, pacsViewState) {
    lateinit var imageCells: List<ImageCell>

    init {
        onStateChange(state::layoutOption) {
            state.resetImageStates()
            changeLayout()
        }
    }

    private fun changeLayout() {
        view.displayedChild = state.layoutOption.ordinal
        imageCells = getImageCellsFromPanel()
    }

    private fun getImageCellsFromPanel(): List<ImageCell> {
        val displayedChild = view.getChildAt(view.displayedChild)
        return when (PacsViewState.LayoutOption.values()[view.displayedChild]) {
            PacsViewState.LayoutOption.ONE_ONE -> {
                listOf(ImageCell(displayedChild, 0, state))
            }
            PacsViewState.LayoutOption.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .mapIndexed { index, relativeLayout -> ImageCell(relativeLayout, index, state) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .mapIndexed { index, relativeLayout -> ImageCell(relativeLayout, index, state) }
            }
        }
    }
}