package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
class ObservableMutableList<T>(
        internalList: MutableList<T> = emptyList<T>().toMutableList()
): ObservableMutableCollection<T, MutableList<T>, MutableListObserver<T>>(internalList), MutableList<T> {

    override fun add(index: Int, element: T) {
        internal.add(index, element)
        observers.values.forEach { it.itemAddedAt(index, element) }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        if (internal.addAll(index, elements)) {
            observers.values.forEach { it.itemsAddedAt(index, elements) }
            return true
        }
        return false
    }

    override fun removeAt(index: Int): T {
        val removed = internal.removeAt(index)
        observers.values.forEach { it.itemRemoved(removed) }
        return removed
    }

    override fun set(index: Int, element: T): T {
        val oldValue = internal.set(index, element)
        if (oldValue != element) {
            observers.values.forEach { it.itemSetAt(index, element) }
        }
        return oldValue
    }

    override fun get(index: Int): T = internal[index]

    override fun indexOf(element: T): Int = internal.indexOf(element)

    override fun lastIndexOf(element: T): Int = internal.lastIndexOf(element)

    override fun listIterator(): MutableListIterator<T> = internal.listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = internal.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = internal.subList(fromIndex, toIndex)

}