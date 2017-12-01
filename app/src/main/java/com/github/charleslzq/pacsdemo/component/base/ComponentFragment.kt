package com.github.charleslzq.pacsdemo.component.base

import android.view.View

/**
 * Created by charleslzq on 17-11-30.
 */
abstract class ComponentFragment<out V, out S, G>(
        view: V,
        val globalState: G,
        val mapGlobalToLocal: (G) -> S
) : Component<V, S>(view, mapGlobalToLocal(globalState))
        where V : View