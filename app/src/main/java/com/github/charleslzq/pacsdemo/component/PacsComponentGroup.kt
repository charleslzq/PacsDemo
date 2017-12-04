package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.component.base.ComponentGroup
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-12-4.
 */
abstract class PacsComponentGroup<V>(
        view: V,
        pacsViewState: PacsViewState,
        config: List<Sub<V, PacsViewState, *, *, *>>
) : ComponentGroup<V, PacsViewState>(view, pacsViewState, config)
        where V : View