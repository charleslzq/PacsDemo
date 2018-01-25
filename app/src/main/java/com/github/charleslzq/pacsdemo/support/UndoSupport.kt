package com.github.charleslzq.pacsdemo.support

import java.util.*

/**
 * Created by charleslzq on 17-12-29.
 * 撤销/重做支持类
 */
class UndoSupport<T> {
    private val doneStack = Stack<T>()
    private val canceledStack = Stack<T>()

    /**
     * 从另一个UndoSupport拷贝数据.在拷贝前会清空本地数据
     */
    fun copyFrom(another: UndoSupport<T>) {
        reset()
        another.doneStack.forEach { doneStack.push(it) }
        another.canceledStack.forEach { canceledStack.push(it) }
    }

    /**
     * 清空数据
     */
    fun reset() {
        doneStack.clear()
        canceledStack.clear()
    }

    /**
     * 是否能撤销
     */
    fun canUndo() = doneStack.isNotEmpty()

    /**
     * 是否能重做
     */
    fun canRedo() = canceledStack.isNotEmpty()

    /**
     * 将值加入到栈中,之后撤销将返回该值
     */
    fun done(data: T) = doneStack.push(data)

    /**
     * 撤销,返回上一个值
     */
    fun undo() = if (canUndo()) {
        canceledStack.push(doneStack.pop())
        if (doneStack.isNotEmpty()) {
            doneStack.peek()
        } else {
            null
        }
    } else {
        null
    }

    /**
     * 重做,返回上一个被撤销的值
     */
    fun redo() = if (canRedo()) {
        canceledStack.pop().also { done(it) }
    } else {
        null
    }

    /**
     * 根据当前值计算新值
     * @param initialValue 没有值(不可撤销)时作为种子, 由generator计算出新值作为当前值
     * @param generator 计算函数, 结果会作为新的当前值
     * @return generator产生的新值
     */
    fun generate(initialValue: () -> T, generator: (T) -> T) = generator(
        if (doneStack.isEmpty()) {
            initialValue()
        } else {
            doneStack.peek()
        }
    ).also { done(it) }
}