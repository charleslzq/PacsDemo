package com.github.charleslzq.pacsdemo.component.store

import android.graphics.*
import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.MiddleWare


data class ImageDisplayModel(val images: List<Bitmap> = emptyList())
data class ImageCanvasModel(val drawing: Bitmap? = null, val points: List<PointF> = emptyList(), val canUndo: Boolean = false, val canRedo: Boolean = false)

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageFrameStore(val layoutPosition: Int) : Store<ImageFrameStore>(MiddleWare.debugLog, buildThunk<ImageFrameStore>()) {
    var linePaint = Paint()
    var stringPaint = Paint()
    var pointPaint = Paint()

    var allowPlay = true
        private set
    var hideMeta by ObservableStatus(true)
        private set

    var bindModId = ""
        private set
    var duration = 40
        private set
    var size by ObservableStatus(0)
        private set
    var patientMeta by ObservableStatus(DicomPatientMetaInfo())
        private set
    var studyMeta by ObservableStatus(DicomStudyMetaInfo())
        private set
    var seriesMeta by ObservableStatus(DicomSeriesMetaInfo())
        private set
    var imageMeta by ObservableStatus(DicomImageMetaInfo())
        private set
    var index by ObservableStatus(0)
        private set
    var displayModel by ObservableStatus(ImageDisplayModel())
        private set

    var autoScale by ObservableStatus(1.0f)
    var gestureScale by ObservableStatus(1.0f)
        private set
    var matrix by ObservableStatus(Matrix())
        private set
    var colorMatrix by ObservableStatus(ColorMatrix())
        private set
    var reverseColor by ObservableStatus(false)
        private set
    var pseudoColor by ObservableStatus(false)
        private set

    var measure by ObservableStatus(Measure.NONE)
        private set
    var canvasModel by ObservableStatus(ImageCanvasModel())

    init {
        linePaint.color = Color.RED
        linePaint.strokeWidth = 3f
        linePaint.isAntiAlias = true
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.style = Paint.Style.STROKE
        stringPaint.strokeWidth = 1f
        stringPaint.color = Color.RED
        stringPaint.isLinearText = true
        pointPaint.color = Color.RED
        pointPaint.strokeWidth = 3f
        pointPaint.style = Paint.Style.FILL_AND_STROKE

        reduce(ImageFrameStore::hideMeta) {
            on<ImageClicked> { !state }
            on<BindModel>(precondition = { it.size > 0 }) { false }
            on<ResetDisplay> { false }
        }

        reduce(ImageFrameStore::bindModId) {
            on<BindModel> { event.modeId }
            on<Reset> { "" }
        }

        reduce(ImageFrameStore::size) {
            on<BindModel> { event.size }
            on<Reset> { 0 }
        }

        reduce(ImageFrameStore::patientMeta) {
            on<BindModel> { event.patient }
            on<Reset> { DicomPatientMetaInfo() }
        }

        reduce(ImageFrameStore::studyMeta) {
            on<BindModel> { event.study }
            on<Reset> { DicomStudyMetaInfo() }
        }

        reduce(ImageFrameStore::seriesMeta) {
            on<BindModel> { event.series }
            on<Reset> { DicomSeriesMetaInfo() }
        }

        reduce(ImageFrameStore::imageMeta) {
            on<ShowImage> { event.meta }
            on<PlayIndexChange> { event.meta }
            on<Reset> { DicomImageMetaInfo() }
        }

        reduce(ImageFrameStore::index) {
            on<ShowImage> { event.index }
            on<PlayIndexChange> { event.index }
            on<Reset> { 0 }
        }

        reduce(ImageFrameStore::displayModel) {
            on<ShowImage> {
                ImageDisplayModel(listOf(event.bitmap))
            }
            on<PlayAnimation> {
                ImageDisplayModel(event.images)
            }
        }

        reduce(ImageFrameStore::gestureScale) {
            on<ScaleChange> {
                getNewScaleFactor(event.scaleFactor)
            }
            on<StudyModeReset> {
                1.0f
            }
        }

        reduce(ImageFrameStore::matrix) {
            on<ScaleChange> {
                Matrix(state).apply {
                    val newScale = getNewScaleFactor(event.scaleFactor)
                    setScale(newScale, newScale)
                }
            }
            on<StudyModeReset> {
                Matrix()
            }
        }

        reduce(ImageFrameStore::reverseColor) {
            on<ReverseColor> {
                !state
            }
            on<ResetDisplay> {
                false
            }
            on<StudyModeReset> {
                false
            }
        }

        reduce(ImageFrameStore::colorMatrix) {
            on<ReverseColor> {
                ColorMatrix(state).apply {
                    postConcat(reverseMatrix)
                }
            }
            on<ResetDisplay> {
                ColorMatrix()
            }
            on<StudyModeReset> {
                ColorMatrix()
            }
        }

        reduce(ImageFrameStore::pseudoColor) {
            on<PseudoColor> {
                !state
            }
            on<ResetDisplay> {
                false
            }
            on<StudyModeReset> {
                false
            }
        }

        reduce(ImageFrameStore::measure) {
            on<MeasureLineTurned> {
                when (state) {
                    Measure.LINE -> Measure.NONE
                    else -> Measure.LINE
                }
            }
            on<MeasureAngleTurned> {
                when (state) {
                    Measure.ANGEL -> Measure.NONE
                    else -> Measure.ANGEL
                }
            }
            on<ResetMeasure> { Measure.NONE }
        }

        reduce(ImageFrameStore::canvasModel) {
            on<ImageCanvasModel> { event }
            on<ResetMeasure> { ImageCanvasModel() }
        }
    }

    fun hasImage() = displayModel.images.isNotEmpty()

    fun playable() = allowPlay && size > 1

    private fun getNewScaleFactor(rawScaleFactor: Float): Float = Math.max(1.0f, Math.min(rawScaleFactor * gestureScale, 5.0f))

    data class BindModel(val modeId: String,
                         val patient: DicomPatientMetaInfo,
                         val study: DicomStudyMetaInfo,
                         val series: DicomSeriesMetaInfo,
                         val size: Int)

    data class ShowImage(val bitmap: Bitmap, val index: Int, val meta: DicomImageMetaInfo)
    data class PlayAnimation(val images: List<Bitmap>)
    data class PlayIndexChange(val index: Int, val meta: DicomImageMetaInfo)

    class StudyModeReset
    class Reset
    class ResetDisplay
    class ResetMeasure

    class ImageClicked
    class MeasureLineTurned
    class MeasureAngleTurned
    class ReverseColor
    class PseudoColor

    data class ScaleChange(val scaleFactor: Float)
    data class LocationTranslate(val distanceX: Float, val distanceY: Float)
    data class DrawLines(val points: List<PointF>)

    enum class Measure {
        NONE,
        LINE,
        ANGEL
    }

    companion object {
        private val reverseMatrix = ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
        ))
        private val preloadRange = 5
    }
}