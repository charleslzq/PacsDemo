package com.github.charleslzq.pacsdemo.binder

import android.graphics.ColorMatrix
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel
import com.github.charleslzq.pacsdemo.binder.vo.PacsDemoViewModel
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers

/**
 * Created by charleslzq on 17-11-27.
 */
class ButtonPanelBinder(
        buttonPanel: View,
        val resetLayout: (PacsDemoViewModel.LayoutOption) -> Unit
) : ViewBinder<View, PacsDemoViewModel>(buttonPanel, { PacsDemoViewModel() }) {
    private val pseudoButton: Button = view.findViewById(R.id.pseudoColorButton)
    private val reverseButton: Button = view.findViewById(R.id.reverseButton)
    private val splitButton: Button = view.findViewById(R.id.spliteButton)
    private val layoutSelector: PopupMenu = PopupMenu(buttonPanel.context, splitButton)
    private val reverseMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
    ))

    init {
        layoutSelector.menu.add(Menu.NONE, R.id.one_one, Menu.NONE, "1 X 1")
        layoutSelector.menu.add(Menu.NONE, R.id.one_two, Menu.NONE, "1 X 2")
        layoutSelector.menu.add(Menu.NONE, R.id.two_two, Menu.NONE, "2 X 2")
        layoutSelector.menu.add(Menu.NONE, R.id.three_three, Menu.NONE, "3 X 3")
        layoutSelector.setOnMenuItemClickListener(this::onLayoutSelected)
        splitButton.setOnClickListener {
            layoutSelector.show()
        }

        onNewModel {
            reverseButton.setOnClickListener {
                if (model.layoutOption == PacsDemoViewModel.LayoutOption.ONE_ONE && model.selected >= 0 && model.selected < model.seriesList.size) {
                    val imageModel = model.seriesList[model.selected].imageFramesViewModel
                    val newColorMatrix = ColorMatrix(imageModel.colorMatrix)
                    newColorMatrix.postConcat(reverseMatrix)
                    imageModel.colorMatrix = newColorMatrix
                }
            }

            pseudoButton.setOnClickListener {
                if (model.layoutOption == PacsDemoViewModel.LayoutOption.ONE_ONE && model.selected >= 0 && model.selected < model.seriesList.size) {
                    val imageModel = model.seriesList[model.selected].imageFramesViewModel
                    imageModel.pseudoColor = !imageModel.pseudoColor
                }
            }
            onModelChange(model::layoutOption) {
                val visible = when (model.layoutOption == PacsDemoViewModel.LayoutOption.ONE_ONE) {
                    true -> View.VISIBLE
                    false -> View.INVISIBLE
                }
                reverseButton.visibility = visible
                pseudoButton.visibility = visible
                resetLayout(model.layoutOption)
            }
        }

    }

    private fun onLayoutSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.one_one -> {
                model.layoutOption = PacsDemoViewModel.LayoutOption.ONE_ONE
            }
            R.id.one_two -> {
                model.layoutOption = PacsDemoViewModel.LayoutOption.ONE_TWO
            }
            R.id.two_two -> {
                model.layoutOption = PacsDemoViewModel.LayoutOption.TWO_TWO
            }
            R.id.three_three -> {
                model.layoutOption = PacsDemoViewModel.LayoutOption.THREE_THREE
            }
        }
        return true
    }
}