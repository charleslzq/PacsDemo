package com.github.charleslzq.pacsdemo.component.store

import android.graphics.*
import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.MiddleWare


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
    var imageDisplayModel by ObservableStatus(ImageDisplayModel())
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
    var currentPoints by ObservableStatus(emptyArray<PointF>())
        private set
    var drawingMap: Bitmap? by ObservableStatus(null)
        private set
    var canUndo by ObservableStatus(false)
        private set
    var canRedo by ObservableStatus(false)
        private set

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

        reduce(ImageFrameStore::imageDisplayModel) {
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
            on<PlayModeReset> {
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
            on<PlayModeReset> {
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
            on<PlayModeReset> {
                false
            }
            on<StudyModeReset> {
                false
            }
        }

        reduce(ImageFrameStore::measure) {
            on<MeasureLineTurned> {
                Measure.LINE
            }
            on<MeasureAngleTurned> {
                Measure.ANGEL
            }
            on<IndexChange> {
                Measure.NONE
            }
        }

        reduce(ImageFrameStore::drawingMap) {

        }

        reduce(property = ImageFrameStore::currentPoints) {
            on<AddPath> {
                emptyArray()
            }
            on<DrawLines> {
                event.points.toTypedArray()
            }
            on<IndexChange> {
                emptyArray()
            }
            on<MeasureLineTurned> {
                emptyArray()
            }
            on<MeasureAngleTurned> {
                emptyArray()
            }
        }
    }

    fun hasImage() = imageDisplayModel.images.isNotEmpty()

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

    class ImageClicked
    class MeasureLineTurned
    class MeasureAngleTurned
    class ReverseColor
    class PseudoColor
    class Undo
    class Redo
    class ChangePlayStatus
    class PlayModeReset
    class StudyModeReset
    class Reset

    data class ScaleChange(val scaleFactor: Float)
    data class IndexChange(val index: Int, val fromUser: Boolean)
    data class IndexScroll(val scroll: Float)
    data class LocationTranslate(val distanceX: Float, val distanceY: Float)
    data class AddPath(val points: List<PointF>, val text: Pair<PointF, String>)
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