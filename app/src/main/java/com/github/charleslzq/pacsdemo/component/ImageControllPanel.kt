package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore
import com.github.charleslzq.pacsdemo.support.TypefaceUtil

/**
 * Created by charleslzq on 17-12-19.
 */
class ImageControllPanel(
        baseView: View,
        patientStore: PatientSeriesStore
) : Component<View, PatientSeriesStore>(baseView, patientStore) {
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

    private val imageDispatch = store.imageFramesStore.dispatch

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
            imageDispatch(ImageFramesStore.MeasureAngleTurned())
        }

        measureLine.setOnClickListener {
            imageDispatch(ImageFramesStore.MeasureLineTurned())
        }

        reverse.setOnClickListener {
            imageDispatch(ImageFramesStore.ReverseColor())
        }

        pseudo.setOnClickListener {
            imageDispatch(ImageFramesStore.PseudoColor())
        }

        undo.setOnClickListener {
            imageDispatch(ImageFramesStore.Undo())
        }

        redo.setOnClickListener {
            imageDispatch(ImageFramesStore.Redo())
        }

        first.setOnClickListener {
            imageDispatch(ImageFramesStore.IndexChange(0, true))
        }

        play.setOnClickListener {
            imageDispatch(ImageFramesStore.ChangePlayStatus())
        }

        previous.setOnClickListener {
            val currentIndex = store.imageFramesStore.imagePlayModel.currentIndex
            if (currentIndex > 0) {
                imageDispatch(ImageFramesStore.IndexChange(currentIndex - 1, true))
            }
        }

        next.setOnClickListener {
            val currentIndex = store.imageFramesStore.imagePlayModel.currentIndex
            if (currentIndex < store.imageFramesStore.imageFramesModel.size - 1) {
                imageDispatch(ImageFramesStore.IndexChange(currentIndex + 1, true))
            }
        }

        last.setOnClickListener {
            imageDispatch(ImageFramesStore.IndexChange(store.imageFramesStore.imageFramesModel.size - 1, true))
        }

        render(store.imageFramesStore::measure) {
            measureAngle.isSelected = it == ImageFramesStore.Measure.ANGEL
            measureLine.isSelected = it == ImageFramesStore.Measure.LINE
        }

        render(store.imageFramesStore::pseudoColor) {
            pseudo.isSelected = it
        }

        render(store.imageFramesStore::reverseColor) {
            reverse.isSelected = it
        }

        render(store.imageFramesStore::imageFramesModel) {
            val visible = if (it.size > 1) {
                imageSeekBar.max = it.size - 1
                imageSeekBar.progress = 0
                imageSeekBar.visibility = View.VISIBLE
                imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            imageDispatch(ImageFramesStore.IndexChange(progress, true))
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

            if (store.imageFramesStore.imageFramesModel.size == 1) {
                displayController.visibility = View.VISIBLE
            }

            if (!store.imageFramesStore.playable()) {
                play.visibility = View.GONE
            } else {
                play.visibility = View.VISIBLE
            }
        }

        render(store.imageFramesStore::imagePlayModel) {
            imageSeekBar.progress = it.currentIndex
            play.post {
                play.text = if (it.playing) view.context.resources.getString(R.string.image_pause) else view.context.resources.getString(R.string.image_play)
            }
        }

        render(property = store.imageFramesStore::hideMeta, guard = { store.imageFramesStore.hasImage() }) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }

        render(property = store.imageFramesStore::drawingMap, guard = { store.imageFramesStore.hasImage() && store.imageFramesStore.measure != ImageFramesStore.Measure.NONE }) {
            undo.visibility = if (store.imageFramesStore.canUndo()) View.VISIBLE else View.GONE
            redo.visibility = if (store.imageFramesStore.canRedo()) View.VISIBLE else View.GONE
        }
    }
}