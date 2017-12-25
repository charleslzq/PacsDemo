package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.Button
import android.widget.SeekBar
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore
import com.github.charleslzq.pacsdemo.support.GlobalDispatch
import com.github.charleslzq.pacsdemo.support.TypefaceUtil

/**
 * Created by charleslzq on 17-12-19.
 */
class ImageControllPanel(
        baseView: View,
        patientStore: PatientSeriesStore
) : Component<View, PatientSeriesStore>(baseView, patientStore) {
    private val dispatch = GlobalDispatch.DEBUG_DISPATCH

    private val play: Button = view.findViewById(R.id.imagePlay)
    private val first: Button = view.findViewById(R.id.firstImage)
    private val previous: Button = view.findViewById(R.id.previousImage)
    private val next: Button = view.findViewById(R.id.nextImage)
    private val last: Button = view.findViewById(R.id.lastImage)

    private val measureAngle: Button = view.findViewById(R.id.measureAngleButton)
    private val measureLine: Button = view.findViewById(R.id.measureLineButton)
    private val pseudo: Button = view.findViewById(R.id.pseudoColorButton)
    private val reverse: Button = view.findViewById(R.id.reverseButton)

    private val imageSeekBar: SeekBar = view.findViewById(R.id.imageSeekBar)

    init {
        val fontAwesomeTypeface = TypefaceUtil.getTypeFace(view.context, TypefaceUtil.fontAwesome)
        measureLine.typeface = fontAwesomeTypeface
        measureAngle.typeface = fontAwesomeTypeface
        pseudo.typeface = fontAwesomeTypeface
        reverse.typeface = fontAwesomeTypeface
        play.typeface = fontAwesomeTypeface
        previous.typeface = fontAwesomeTypeface
        next.typeface = fontAwesomeTypeface
        first.typeface = fontAwesomeTypeface
        last.typeface = fontAwesomeTypeface

        measureAngle.setOnClickListener {
            dispatch(ClickEvent.TurnToMeasureAngle(store.imageFramesStore.layoutPosition))
        }

        measureLine.setOnClickListener {
            dispatch(ClickEvent.TurnToMeasureLine(store.imageFramesStore.layoutPosition))
        }

        reverse.setOnClickListener {
            dispatch(ClickEvent.ReverseColor(store.imageFramesStore.layoutPosition))
        }

        pseudo.setOnClickListener {
            dispatch(ClickEvent.PseudoColor(store.imageFramesStore.layoutPosition))
        }

        first.setOnClickListener {
            dispatch(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, 0, true))
        }

        play.setOnClickListener {
            dispatch(ImageDisplayEvent.ChangePlayStatus(store.imageFramesStore.layoutPosition))
        }

        previous.setOnClickListener {
            val currentIndex = store.imageFramesStore.imagePlayModel.currentIndex
            if (currentIndex > 0) {
                dispatch(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, currentIndex - 1, true))
            }
        }

        next.setOnClickListener {
            val currentIndex = store.imageFramesStore.imagePlayModel.currentIndex
            if (currentIndex < store.imageFramesStore.imageFramesModel.size - 1) {
                dispatch(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, currentIndex + 1, true))
            }
        }

        last.setOnClickListener {
            dispatch(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, store.imageFramesStore.imageFramesModel.size - 1, true))
        }

        render(store.imageFramesStore::imageFramesModel) {
            val visible = if (it.size > 1) {
                imageSeekBar.max = it.size - 1
                imageSeekBar.progress = 0
                imageSeekBar.visibility = View.VISIBLE
                imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        dispatch(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, progress, fromUser))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                })
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            imageSeekBar.visibility = visible
            measureAngle.visibility = visible
            measureLine.visibility = visible
            pseudo.visibility = visible
            reverse.visibility = visible
            first.visibility = visible
            previous.visibility = visible
            play.visibility = visible
            next.visibility = visible
            last.visibility = visible

            if (store.imageFramesStore.imageFramesModel.size == 1) {
                measureAngle.visibility = View.VISIBLE
                measureLine.visibility = View.VISIBLE
                pseudo.visibility = View.VISIBLE
                reverse.visibility = View.VISIBLE
            }
        }

        render(store.imageFramesStore::imagePlayModel) {
            imageSeekBar.progress = it.currentIndex
            play.post {
                play.text = if (it.playing) view.context.resources.getString(R.string.image_pause) else view.context.resources.getString(R.string.image_play)
            }
        }

        render(store::hideMeta) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}