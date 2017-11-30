package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-30.
 */
abstract class PacsComponentFragment<V, S>(
        view: V,
        globalState: PacsViewState,
        mapGlobalToLocal: (PacsViewState) -> S
) : ComponentFragment<V, S, PacsViewState>(view, globalState, mapGlobalToLocal)
        where V : View