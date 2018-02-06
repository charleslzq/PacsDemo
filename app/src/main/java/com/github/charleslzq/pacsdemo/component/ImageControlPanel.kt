package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageDisplayActions
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.*
import com.github.charleslzq.pacsdemo.component.store.ImageMeasureActions
import com.github.charleslzq.pacsdemo.support.TypefaceUtil

/**
 * Created by charleslzq on 17-12-19.
 * 图像单元格右下角的控制面板
 */
class ImageControlPanel(
    baseView: View,
    imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(baseView, imageFrameStore) {
    private val play: Button = view.findViewById<Button>(R.id.imagePlay).apply {
        setOnClickListener { dispatch(ImageDisplayActions.playOrPause()) }
    }
    private val first: Button = view.findViewById<Button>(R.id.firstImage).apply {
        setOnClickListener { dispatch(ImageDisplayActions.showImage(0)) }
    }
    private val previous: Button = view.findViewById<Button>(R.id.previousImage).apply {
        setOnClickListener { dispatch(ImageDisplayActions.showImage(store.index - 1)) }
    }
    private val next: Button = view.findViewById<Button>(R.id.nextImage).apply {
        setOnClickListener { dispatch(ImageDisplayActions.showImage(store.index + 1)) }
    }
    private val last: Button = view.findViewById<Button>(R.id.lastImage).apply {
        setOnClickListener { dispatch(ImageDisplayActions.showImage(store.size - 1)) }
    }

    private val measureAngle: Button = view.findViewById<Button>(R.id.measureAngleButton).apply {
        setOnClickListener { dispatch(MeasureAngleTurned()) }
    }
    private val measureLine: Button = view.findViewById<Button>(R.id.measureLineButton).apply {
        setOnClickListener { dispatch(MeasureLineTurned()) }
    }
    private val pseudo: Button = view.findViewById<Button>(R.id.pseudoColorButton).apply {
        setOnClickListener { dispatch(PseudoColor()) }
    }
    private val reverse: Button = view.findViewById<Button>(R.id.reverseButton).apply {
        setOnClickListener { dispatch(ReverseColor()) }
    }
    private val undo: Button = view.findViewById<Button>(R.id.undoButton).apply {
        setOnClickListener { dispatch(ImageMeasureActions.undoDrawing()) }
    }
    private val redo: Button = view.findViewById<Button>(R.id.redoButton).apply {
        setOnClickListener { dispatch(ImageMeasureActions.redoDrawing()) }
    }
    private val clear: Button = view.findViewById<Button>(R.id.clearButton).apply {
        setOnClickListener { dispatch(ImageMeasureActions.clearDrawing()) }
    }

    private val imageSeekBar: SeekBar = view.findViewById(R.id.imageSeekBar)

    private val displayController: LinearLayout = view.findViewById(R.id.displayController)
    private val indexController: LinearLayout = view.findViewById(R.id.indexController)
    private val progressController: LinearLayout = view.findViewById(R.id.progressController)

    private val dispatch = store.dispatch

    init {
        TypefaceUtil.configureTextView(
            TypefaceUtil.FONT_AWESOME,
            measureLine,
            measureAngle,
            pseudo,
            reverse,
            undo,
            redo,
            clear,
            play,
            previous,
            next,
            first,
            last
        )

        render(store::measure) {
            measureAngle.isSelected = it == ImageFrameStore.Measure.ANGEL
            measureLine.isSelected = it == ImageFrameStore.Measure.LINE
        }

        render(store::pseudoColor) {
            pseudo.isSelected = it
        }

        render(store::reverseColor) {
            reverse.isSelected = it
        }

        renderByAll(store::size, store::index) {
            val visible = if (store.size > 1) {
                imageSeekBar.max = store.size - 1
                imageSeekBar.progress = store.index
                imageSeekBar.visibility = View.VISIBLE
                imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            dispatch(ImageDisplayActions.showImage(progress))
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                })
                View.VISIBLE
            } else {
                View.GONE
            }

            displayController.visibility = visible
            indexController.visibility = visible
            progressController.visibility = visible

            if (store.size == 1) {
                displayController.visibility = View.VISIBLE
            }

            if (!store.playable) {
                play.visibility = View.GONE
            } else {
                play.visibility = View.VISIBLE
            }
        }

        renderByAll(store::displayModel, store::index) {
            play.text =
                    if (store.displayModel.images.size > 1 && store.index != store.size - 1) {
                        view.context.resources.getString(R.string.image_pause)
                    } else {
                        view.context.resources.getString(R.string.image_play)
                    }
        }

        render(property = store::canvasModel, require = { store.hasImage }) {
            undo.visibility = if (it.canUndo) View.VISIBLE else View.GONE
            redo.visibility = if (it.canRedo) View.VISIBLE else View.GONE
            clear.visibility = if (it.canUndo || it.canRedo) View.VISIBLE else View.GONE
        }

        render(store::hideMeta) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}