package com.github.charleslzq.pacsdemo.component.store.action

import java.util.*

/**
 * Created by charleslzq on 17-12-29.
 */
class UndoSupport<T> {
    private val doneStack = Stack<T>()
    private val canceledStack = Stack<T>()

    fun reset() {
        doneStack.clear()
        canceledStack.clear()
    }

    fun initialized() = doneStack.isNotEmpty()

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

    fun generate(generator: (T) -> T): T {
        if (doneStack.isEmpty()) {
            throw IllegalStateException("no data to work on")
        }
        return generator(doneStack.peek()).also { done(it) }
    }

}