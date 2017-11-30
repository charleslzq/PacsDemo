package com.github.charleslzq.pacsdemo.component

import android.widget.*
import com.github.charleslzq.pacsdemo.ViewUtils
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelector(
        viewFlipper: ViewFlipper
) : Component<ViewFlipper, PacsViewState>(viewFlipper, { PacsViewState() }) {
    lateinit var imageCells: List<ImageCell>

    init {
        onNewState {
            onStateChange(state::layoutOption) {
                viewFlipper.displayedChild = state.layoutOption.ordinal
                imageCells = getImageCellsFromPanel()
            }
        }
    }

    private fun getImageCellsFromPanel(): List<ImageCell> {
        val displayedChild = view.getChildAt(view.displayedChild)
        return when (PacsViewState.LayoutOption.values()[view.displayedChild]) {
            PacsViewState.LayoutOption.ONE_ONE -> {
                listOf(ImageCell(displayedChild, 0, state))
            }
            PacsViewState.LayoutOption.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .mapIndexed { index, relativeLayout ->  ImageCell(relativeLayout, index, state) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .mapIndexed { index, relativeLayout -> ImageCell(relativeLayout, index, state) }
            }
        }
    }
}