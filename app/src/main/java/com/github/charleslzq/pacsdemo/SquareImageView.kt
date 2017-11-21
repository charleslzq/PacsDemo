package com.github.charleslzq.pacsdemo

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Created by charleslzq on 17-11-21.
 */
class SquareImageView(
        context: Context,
        attributeSet: AttributeSet?,
        defStyle: Int
): ImageView(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet): this(context, attributeSet, 0)

    constructor(context: Context): this(context, null, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val length = minOf(measuredHeight, measuredWidth)
        setMeasuredDimension(length, length)
    }
}