package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-30.
 */
abstract class PacsComponent<out V>(
        view: V,
        pacsViewState: PacsViewState
) : Component<V, PacsViewState>(view, pacsViewState)
        where V : View