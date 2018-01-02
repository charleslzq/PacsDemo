package com.github.charleslzq.pacsdemo.support

import java.util.*

/**
 * Created by charleslzq on 17-12-29.
 */
class UndoSupport<T> {
    private val doneStack = Stack<T>()
    private val canceledStack = Stack<T>()

    fun copyFrom(another: UndoSupport<T>) {
        reset()
        another.doneStack.forEach { doneStack.push(it) }
        another.canceledStack.forEach { canceledStack.push(it) }
    }

    fun reset() {
        doneStack.clear()
        canceledStack.clear()
    }

    fun canUndo() = doneStack.size > 1

    fun canRedo() = canceledStack.isNotEmpty()

    fun done(data: T) {
        doneStack.push(data)
    }

    fun undo(): T {
        if (canUndo()) {
            canceledStack.push(doneStack.pop())
            return doneStack.peek()
        } else {
            throw IllegalStateException("Can not undo any more!")
        }
    }

    fun redo(): T {
        if (canRedo()) {
            return canceledStack.pop().also { done(it) }
        } else {
            throw IllegalStateException("Can not redo any more!")
        }
    }

    fun generate(initialValue: T, generator: (T) -> T): T {
        if (doneStack.isEmpty()) {
            done(initialValue)
        }
        return generator(doneStack.peek()).also { done(it) }
    }
}