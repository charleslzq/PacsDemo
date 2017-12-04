package com.github.charleslzq.pacsdemo.component

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class ButtonPanel(
        buttonPanel: View,
        pacsViewState: PacsViewState
) : PacsComponent<View>(buttonPanel, pacsViewState) {
    private val measureAngleButton: Button = view.findViewById(R.id.measureAngleButton)
    private val measureLineButton: Button = view.findViewById(R.id.measureLineButton)
    private val pseudoButton: Button = view.findViewById(R.id.pseudoColorButton)
    private val reverseButton: Button = view.findViewById(R.id.reverseButton)
    private val splitButton: Button = view.findViewById(R.id.spliteButton)
    private val layoutSelector: PopupMenu = PopupMenu(buttonPanel.context, splitButton)

    init {
        layoutSelector.menu.add(Menu.NONE, R.id.one_one, Menu.NONE, "1 X 1")
        layoutSelector.menu.add(Menu.NONE, R.id.one_two, Menu.NONE, "1 X 2")
        layoutSelector.menu.add(Menu.NONE, R.id.two_two, Menu.NONE, "2 X 2")
        layoutSelector.menu.add(Menu.NONE, R.id.three_three, Menu.NONE, "3 X 3")
        layoutSelector.setOnMenuItemClickListener(this::onLayoutSelected)
        splitButton.setOnClickListener {
            layoutSelector.show()
        }

        measureAngleButton.setOnClickListener {
            if (state.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                val imageModel = state.imageCells[0]
                imageModel.imageFramesViewState.measure = ImageFramesViewState.Measure.ANGEL
            }
        }

        measureLineButton.setOnClickListener {
            if (state.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                val imageModel = state.imageCells[0]
                imageModel.imageFramesViewState.measure = ImageFramesViewState.Measure.LINE
            }
        }

        reverseButton.setOnClickListener {
            if (state.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                state.imageCells[0].imageFramesViewState.reverseColor()
            }
        }

        pseudoButton.setOnClickListener {
            if (state.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                val imageModel = state.imageCells[0]
                imageModel.imageFramesViewState.pseudoColor = !imageModel.imageFramesViewState.pseudoColor
            }
        }

        onStateChange(state::layoutOption) {
            val visible = when (state.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
            measureLineButton.visibility = visible
            reverseButton.visibility = visible
            pseudoButton.visibility = visible
        }

    }

    private fun onLayoutSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.one_one -> {
                state.layoutOption = PacsViewState.LayoutOption.ONE_ONE
            }
            R.id.one_two -> {
                state.layoutOption = PacsViewState.LayoutOption.ONE_TWO
            }
            R.id.two_two -> {
                state.layoutOption = PacsViewState.LayoutOption.TWO_TWO
            }
            R.id.three_three -> {
                state.layoutOption = PacsViewState.LayoutOption.THREE_THREE
            }
        }
        return true
    }
}