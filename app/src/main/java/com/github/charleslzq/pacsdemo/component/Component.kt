package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.registerObserver
import kotlin.reflect.KProperty0

/**
 * Created by charleslzq on 17-11-27.
 */
abstract class Component<out V, out S>(
        val view: V,
        val state: S
) where V : View {

    fun <T> onStateChange(kProperty0: KProperty0<T>, ignoreUnchanged: Boolean = true, handler: (Triple<T, T, Boolean>) -> Unit) {
        handler(Triple(kProperty0.get(), kProperty0.get(), true))
        registerObserver(kProperty0, { old, new -> if (ignoreUnchanged && old != new) handler(Triple(old, new, false)) }, this.hashCode().toString())
    }

    fun onStatesChange(vararg kProperty0: KProperty0<*>, ignoreUnchanged: Boolean = true, handler: (Boolean) -> Unit) {
        handler(true)
        kProperty0.forEach {
            registerObserver(it, { old, new ->
                if (ignoreUnchanged && old != new) {
                    handler(false)
                }
            })
        }
    }
}