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
    init {
        onNewState {
            onStateChange(state::layoutOption) {
                viewFlipper.displayedChild = state.layoutOption.ordinal
                getImageViewBindersFromPanel().forEachIndexed { index, imageCell ->
                    imageCell.layoutPosition = index
                }
                state.imageCells.clear()
            }
        }
    }

    private fun getImageViewBindersFromPanel(): List<ImageCell> {
        val displayedChild = view.getChildAt(view.displayedChild)
        return when (PacsViewState.LayoutOption.values()[view.displayedChild]) {
            PacsViewState.LayoutOption.ONE_ONE -> {
                listOf(ImageCell(displayedChild, state))
            }
            PacsViewState.LayoutOption.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .map { ImageCell(it, state) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .map { ImageCell(it, state) }
            }
        }
    }
}