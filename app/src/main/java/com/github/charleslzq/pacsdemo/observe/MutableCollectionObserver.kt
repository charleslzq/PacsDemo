package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
interface MutableCollectionObserver<in T> {
    fun itemAdded(element: T)
    fun itemsAdded(elements: Collection<T>)
    fun itemRemoved(element: T)
    fun collectionCleared()
}