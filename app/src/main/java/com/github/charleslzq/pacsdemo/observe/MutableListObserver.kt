package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
interface MutableListObserver<T> : MutableCollectionObserver<MutableList<T>, T> {
    fun itemAddedAt(index: Int, element: T)
    fun itemsAddedAt(index: Int, elements: Collection<T>)
    fun itemSetAt(index: Int, element: T)
}