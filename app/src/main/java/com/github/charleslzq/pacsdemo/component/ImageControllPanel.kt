package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.Button
import android.widget.SeekBar
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
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
            EventBus.post(ClickEvent.TurnToMeasureAngle(store.imageFramesStore.layoutPosition))
        }

        measureLine.setOnClickListener {
            EventBus.post(ClickEvent.TurnToMeasureLine(store.imageFramesStore.layoutPosition))
        }

        reverse.setOnClickListener {
            EventBus.post(ClickEvent.ReverseColor(store.imageFramesStore.layoutPosition))
        }

        pseudo.setOnClickListener {
            EventBus.post(ClickEvent.PseudoColor(store.imageFramesStore.layoutPosition))
        }

        first.setOnClickListener {
            EventBus.post(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, 0, true))
        }

        play.setOnClickListener {
            EventBus.post(ImageDisplayEvent.ChangePlayStatus(store.imageFramesStore.layoutPosition))
        }

        previous.setOnClickListener {
            val currentIndex = store.imageFramesStore.imagePlayModel.currentIndex
            if (currentIndex > 0) {
                EventBus.post(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, currentIndex - 1, true))
            }
        }

        next.setOnClickListener {
            val currentIndex = store.imageFramesStore.imagePlayModel.currentIndex
            if (currentIndex < store.imageFramesStore.imageFramesModel.size - 1) {
                EventBus.post(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, currentIndex + 1, true))
            }
        }

        last.setOnClickListener {
            EventBus.post(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, store.imageFramesStore.imageFramesModel.size - 1, true))
        }

        render(store.imageFramesStore::imageFramesModel) {
            if (it.size > 0) {
                imageSeekBar.max = it.size - 1
                imageSeekBar.progress = 0
                imageSeekBar.visibility = View.VISIBLE
                imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        EventBus.post(ImageDisplayEvent.IndexChange(store.imageFramesStore.layoutPosition, progress, fromUser))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                })
            } else {
                imageSeekBar.visibility = View.INVISIBLE
            }
        }

        render(store.imageFramesStore::imagePlayModel) {
            imageSeekBar.progress = it.currentIndex
            play.post {
                play.text = if (it.playing) view.context.resources.getString(R.string.image_pause) else view.context.resources.getString(R.string.image_play)
            }
        }
    }
}