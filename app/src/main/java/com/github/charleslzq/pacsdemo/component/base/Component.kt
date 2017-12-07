package com.github.charleslzq.pacsdemo.component.base

import android.view.View
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus.Companion.getDelegate
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 17-11-27.
 */
open class Component<out V, S>(
        val view: V,
        val store: S
) where V : View, S : Store<S> {
    fun <P> bind(property: KProperty1<S, P>, handler: (P) -> Unit) {
        val delegate = getDelegate(property, store)
        if (delegate != null) {
            handler(property.get(store))
            delegate.onChange { handler(property.get(store)) }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't bind")
        }
    }

    fun <P> bind(property: KProperty0<P>, handler: (P) -> Unit) {
        val delegate = getDelegate(property)
        if (delegate != null) {
            handler(property.get())
            delegate.onChange { handler(property.get()) }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't bind")
        }
    }
}