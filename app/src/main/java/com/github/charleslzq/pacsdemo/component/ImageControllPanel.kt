package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.*
import com.github.charleslzq.pacsdemo.component.store.action.ImageActions
import com.github.charleslzq.pacsdemo.support.TypefaceUtil

/**
 * Created by charleslzq on 17-12-19.
 */
class ImageControllPanel(
        baseView: View,
        imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(baseView, imageFrameStore) {
    private val play: Button = view.findViewById(R.id.imagePlay)
    private val first: Button = view.findViewById(R.id.firstImage)
    private val previous: Button = view.findViewById(R.id.previousImage)
    private val next: Button = view.findViewById(R.id.nextImage)
    private val last: Button = view.findViewById(R.id.lastImage)

    private val measureAngle: Button = view.findViewById(R.id.measureAngleButton)
    private val measureLine: Button = view.findViewById(R.id.measureLineButton)
    private val pseudo: Button = view.findViewById(R.id.pseudoColorButton)
    private val reverse: Button = view.findViewById(R.id.reverseButton)
    private val undo: Button = view.findViewById(R.id.undoButton)
    private val redo: Button = view.findViewById(R.id.redoButton)

    private val imageSeekBar: SeekBar = view.findViewById(R.id.imageSeekBar)

    private val displayController: LinearLayout = view.findViewById(R.id.displayController)
    private val indexController: LinearLayout = view.findViewById(R.id.indexController)
    private val progressController: LinearLayout = view.findViewById(R.id.progressController)

    private val dispatch = store.dispatch

    init {
        val fontAwesomeTypeface = TypefaceUtil.getTypeFace(view.context, TypefaceUtil.fontAwesome)
        measureLine.typeface = fontAwesomeTypeface
        measureAngle.typeface = fontAwesomeTypeface
        pseudo.typeface = fontAwesomeTypeface
        reverse.typeface = fontAwesomeTypeface
        undo.typeface = fontAwesomeTypeface
        redo.typeface = fontAwesomeTypeface
        play.typeface = fontAwesomeTypeface
        previous.typeface = fontAwesomeTypeface
        next.typeface = fontAwesomeTypeface
        first.typeface = fontAwesomeTypeface
        last.typeface = fontAwesomeTypeface

        measureAngle.setOnClickListener {
            dispatch(MeasureAngleTurned())
        }

        measureLine.setOnClickListener {
            dispatch(MeasureLineTurned())
        }

        reverse.setOnClickListener {
            dispatch(ReverseColor())
        }

        pseudo.setOnClickListener {
            dispatch(PseudoColor())
        }

        undo.setOnClickListener {
            dispatch(Undo())
        }

        redo.setOnClickListener {
            dispatch(Redo())
        }

        first.setOnClickListener {
            dispatch(ImageActions.showImage(0))
        }

        previous.setOnClickListener {
            dispatch(ImageActions.showImage(store.index - 1))
        }

        play.setOnClickListener {
            dispatch(ImageActions.playOrPause())
        }

        next.setOnClickListener {
            dispatch(ImageActions.showImage(store.index + 1))
        }

        last.setOnClickListener {
            dispatch(ImageActions.showImage(store.size - 1))
        }

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

        render(store::size) {
            val visible = if (it > 1) {
                imageSeekBar.max = it - 1
                imageSeekBar.progress = 0
                imageSeekBar.visibility = View.VISIBLE
                imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            dispatch(ImageActions.showImage(progress))
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

            if (!store.playable()) {
                play.visibility = View.GONE
            } else {
                play.visibility = View.VISIBLE
            }
        }

        render(store::index) {
            imageSeekBar.progress = it
        }

        render(store::imageDisplayModel) {
            play.post {
                play.text = if (it.images.size > 1) view.context.resources.getString(R.string.image_pause) else view.context.resources.getString(R.string.image_play)
            }
        }

        render(property = store::hideMeta, guard = { store.hasImage() }) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }

        render(property = store::drawingMap, guard = { store.hasImage() && store.measure != ImageFrameStore.Measure.NONE }) {
            undo.visibility = if (store.canUndo) View.VISIBLE else View.GONE
            redo.visibility = if (store.canRedo) View.VISIBLE else View.GONE
        }
    }
}