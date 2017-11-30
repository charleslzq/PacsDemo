package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsMain(
        mainView: View
) : Component<View, PacsViewState>(mainView, { PacsViewState() }) {
    private val thumbList = ThumbList(view.findViewById(R.id.thumbList))
    private val viewSelector = ViewSelector(view.findViewById(R.id.viewSelector))
    private val imageProgressBar = ImageProgressBar(view.findViewById(R.id.imageSeekBar))
    private val buttonPanel = ButtonPanel(view.findViewById(R.id.buttonPanel))

    init {
        onNewState {
            thumbList.state = state
            viewSelector.state = state
            buttonPanel.state = state

            onStateChange(state::singleBinding) {
                if (state.singleBinding && state.imageCells[0] != null) {
                    imageProgressBar.state = state.imageCells[0]!!
                }
            }
            onStateChange(state::layoutOption) {
                state.imageCells = arrayOfNulls<ImageFramesViewState>(9).toMutableList()
            }
        }
    }
}