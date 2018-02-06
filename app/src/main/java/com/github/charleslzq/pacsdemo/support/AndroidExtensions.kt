package com.github.charleslzq.pacsdemo.support

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Created by charleslzq on 18-2-6.
 */
/**
 * 编辑SharedPrefernces
 */
fun SharedPreferences.edit(edit: SharedPreferences.Editor.() -> Unit) = edit().apply(edit).apply()

/**
 * 在bitmap上作画
 */
fun Bitmap.applyCanvas(draw: Canvas.() -> Unit) = also { Canvas(this).apply(draw) }

/**
 * 获取矩阵值
 */
fun Matrix.values() = FloatArray(9).apply { getValues(this) }

/**
 * 拷贝并转换
 */
fun Matrix.copy(transform: Matrix.() -> Unit = {}) = Matrix(this).apply(transform)

/**
 * 拷贝并转换
 */
fun ColorMatrix.copy(transform: ColorMatrix.() -> Unit = {}) = ColorMatrix(this).apply(transform)

/**
 * 获取一个ViewGroup的所有子View
 */
fun ViewGroup.getAllChildren() = (0 until childCount).map { getChildAt(it) }

/**
 * 获取一个ViewGroup的类型为klass的所有子View
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : View> ViewGroup.getTypedChildren() =
    getAllChildren().filter { it is T }.map { it as T }

inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.getViewHolder(position: Int) =
    findViewHolderForAdapterPosition(position) as? VH