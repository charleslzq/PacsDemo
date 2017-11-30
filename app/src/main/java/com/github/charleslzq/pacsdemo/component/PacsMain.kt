package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsMain(
        mainView: View,
        pacsViewState: PacsViewState
) : PacsComponent<View>(mainView, pacsViewState) {
    private val thumbList = ThumbList(view.findViewById(R.id.thumbList), pacsViewState)
    private val viewSelector = ViewSelector(view.findViewById(R.id.viewSelector), pacsViewState)
    private val imageProgressBar = ImageProgressBar(view.findViewById(R.id.imageSeekBar), pacsViewState)
    private val buttonPanel = ButtonPanel(view.findViewById(R.id.buttonPanel), pacsViewState)
}