package com.github.charleslzq.pacsdemo.binder

import android.view.View
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.register
import kotlin.reflect.KProperty0

/**
 * Created by charleslzq on 17-11-27.
 */
abstract class ViewBinder<out V, D>(
        val view: V
) where V : View {
    var model: D? by ObservablePropertyWithObservers(null)

    fun onNewModel(handler: (D?) -> Unit) {
        register(this::model, { _, newModel ->
            handler(newModel)
        })
    }

    fun <T> onModelChange(kProperty0: KProperty0<T>, handler: (T, T) -> Unit) {
        register(kProperty0, handler)
    }
}