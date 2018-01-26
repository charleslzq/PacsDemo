package com.github.charleslzq.pacsdemo.component.store

import android.graphics.*
import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.MiddleWare
import com.github.charleslzq.pacsdemo.support.UndoSupport
import java.net.URI
import java.util.*

/**
 * 图像显示模型.
 * @param images 所需显示的图像. 大小为1时显示图像, 大于1时作为动画播放
 */
data class ImageDisplayModel(val images: List<Bitmap> = emptyList())

/**
 * 图像测量模型
 * @param drawing 当前已绘制完成的测量线/角
 * @param tmp 当前绘制未完成的测量线/角(例如只选择了一个点的测量线)
 * @param canUndo 是否能撤销
 * @param canRedo 是否能重做
 */
data class ImageCanvasModel(
    val drawing: Bitmap? = null,
    val tmp: Bitmap? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

/**
 * dicom图像文件数据模型
 * @param meta 图像元信息
 * @param frame 图像URI
 */
data class ImageFrameModel(
    val meta: DicomImageMetaInfo,
    val frame: URI = meta.let { it.files[DEFAULT] }!!
) {
    companion object {
        const val THUMB = "thumb"
        const val DEFAULT = "default"
        const val RAW = "raw"
    }
}

/**
 * Created by charleslzq on 17-11-27.
 * 图像单元格store
 * @param layoutPosition 位置参数 0-8
 */
class ImageFrameStore(val layoutPosition: Int) : Store<ImageFrameStore>(
    MiddleWare.debugLog,
    buildThunk<ImageFrameStore>(UndoSupport<Bitmap>(), Stack<PointF>())
) {
    /**
     * 测量模式中线的paint
     */
    val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 3f
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }
    /**
     * 测量模式中字的paint
     */
    val stringPaint = Paint().apply {
        strokeWidth = 1f
        color = Color.RED
        isLinearText = true
        textSize = 20f
    }
    /**
     * 测量模式中未完成端点的paint
     */
    val pointPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
    }
    /**
     * 放大镜显示的区域范围
     */
    var range = 50

    /**
     * 是否允许播放动画
     */
    var allowPlay = true
        private set
    /**
     * 是否隐藏单元格四个角上的元信息面板和控制面板
     */
    var hideMeta by ObservableStatus(true)
        private set

    /**
     * 绑定的dicom数据模型id
     */
    var bindModId = ""
        private set
    /**
     * 播放动画每帧的时间
     */
    var duration = 40
        private set
    /**
     * 当前series中图像的张数
     */
    var size by ObservableStatus(0)
        private set
    /**
     * 病人元信息
     */
    var patientMeta by ObservableStatus(DicomPatientMetaInfo())
        private set
    /**
     * study元信息
     */
    var studyMeta by ObservableStatus(DicomStudyMetaInfo())
        private set
    /**
     * series元信息
     */
    var seriesMeta by ObservableStatus(DicomSeriesMetaInfo())
        private set
    /**
     * 图像元信息
     */
    var imageMeta by ObservableStatus(DicomImageMetaInfo())
        private set
    /**
     * 当前图片在series中的位置
     */
    var index by ObservableStatus(0)
        private set
    var displayModel by ObservableStatus(ImageDisplayModel())
        private set

    /**
     * 总的缩放比
     */
    val scale
        get() = autoScale * gestureScale
    /**
     * 图像和ImageView适配所需的缩放比.因为动画只能放在背景中播放,为了让其不变形,只能将ImageView的长宽调整为适应图像
     */
    var autoScale by ObservableStatus(1.0f)
    val viewWidth
        get() = if (displayModel.images.isNotEmpty()) displayModel.images[0].width * autoScale else -1f
    val viewHeight
        get() = if (displayModel.images.isNotEmpty()) displayModel.images[0].height * autoScale else -1f
    /**
     * 通过触摸缩放图形的缩放比
     */
    var gestureScale by ObservableStatus(1.0f)
        private set
    /**
     * 复合了自动扩充和触摸调整因素的位置矩阵
     */
    val compositeMatrix
        get() = Matrix(matrix).apply { postScale(autoScale, autoScale) }
    private val matrixValues
        get() = FloatArray(9).apply { compositeMatrix.getValues(this) }
    private val imageX
        get() = matrixValues[Matrix.MTRANS_X]
    private val imageY
        get() = matrixValues[Matrix.MTRANS_Y]
    private val imageWidth
        get() = if (displayModel.images.isNotEmpty()) matrixValues[Matrix.MSCALE_X] * displayModel.images[0].width else -1f
    private val imageHeight
        get() = if (displayModel.images.isNotEmpty()) matrixValues[Matrix.MSCALE_Y] * displayModel.images[0].height else -1f
    /**
     * 通过触摸调整后的位置矩阵
     */
    var matrix by ObservableStatus(Matrix())
        private set
    /**
     * 颜色显示矩阵
     */
    var colorMatrix by ObservableStatus(ColorMatrix())
        private set
    /**
     * 是否反色
     */
    var reverseColor by ObservableStatus(false)
        private set
    /**
     * 是否伪彩
     */
    var pseudoColor by ObservableStatus(false)
        private set

    /**
     * 所处的测量模式
     */
    var measure by ObservableStatus(Measure.NONE)
        private set
    var canvasModel by ObservableStatus(ImageCanvasModel())

    /**
     * 是否可播放
     */
    val playable
        get() = allowPlay && size > 1
    /**
     * 当前series模型是否有图片
     */
    val hasImage
        get() = size > 0
    /**
     * 给播放用的index. 实现当前图像为最后一张时点播放将从头播放的功能
     */
    val autoJumpIndex
        get() = if (index == size - 1) 0 else index

    init {
        reduce(ImageFrameStore::allowPlay) {
            on<SetAllowPlay> { event.value }
            on<Reset> { true }
        }

        reduce(ImageFrameStore::hideMeta) {
            on<ImageClicked> { !state }
            on<BindModel> { event.size == 0 }
            on<MoveModel> { event.size == 0 }
            on<ResetDisplay> { false }
            on<Reset> { true }
        }

        reduce(ImageFrameStore::bindModId) {
            on<BindModel> { event.modeId }
            on<MoveModel> { event.modeId }
            on<Reset> { "" }
        }

        reduce(ImageFrameStore::size) {
            on<BindModel> { event.size }
            on<MoveModel> { event.size }
            on<Reset> { 0 }
        }

        reduce(ImageFrameStore::patientMeta) {
            on<BindModel> { event.patient }
            on<MoveModel> { event.patient }
            on<Reset> { DicomPatientMetaInfo() }
        }

        reduce(ImageFrameStore::studyMeta) {
            on<BindModel> { event.study }
            on<MoveModel> { event.study }
            on<Reset> { DicomStudyMetaInfo() }
        }

        reduce(ImageFrameStore::seriesMeta) {
            on<BindModel> { event.series }
            on<MoveModel> { event.series }
            on<Reset> { DicomSeriesMetaInfo() }
        }

        reduce(ImageFrameStore::imageMeta) {
            on<ShowImage> { event.meta }
            on<MoveModel> { event.meta }
            on<PlayIndexChange> { event.meta }
            on<Reset> { DicomImageMetaInfo() }
        }

        reduce(ImageFrameStore::index) {
            on<ShowImage> { event.index }
            on<MoveModel> { event.index }
            on<PlayIndexChange> { event.index }
            on<Reset> { 0 }
        }

        reduce(ImageFrameStore::displayModel) {
            on<ShowImage> { ImageDisplayModel(listOf(event.bitmap)) }
            on<MoveModel> { ImageDisplayModel(listOf(event.bitmap)) }
            on<PlayAnimation> { ImageDisplayModel(event.images) }
            on<Reset> { ImageDisplayModel() }
        }

        reduce(ImageFrameStore::gestureScale) {
            on<ScaleChange> {
                state * event.scaleFactor
            }
            on<StudyModeReset> {
                1.0f
            }
            on<BindModel> {
                1.0f
            }
            on<MoveModel> { 1.0f }
        }

        reduce(ImageFrameStore::matrix) {
            on<ScaleChange> {
                Matrix(state).apply {
                    postScale(event.scaleFactor, event.scaleFactor, event.focus.x, event.focus.y)
                }
            }
            on<LocationTranslate> {
                Matrix(state).apply {
                    val offsetX = Math.min(
                        -imageX,
                        Math.max(-event.distanceX, viewWidth - imageX - imageWidth)
                    )
                    val offsetY = Math.min(
                        -imageY,
                        Math.max(-event.distanceY, viewHeight - imageY - imageHeight)
                    )
                    postTranslate(offsetX, offsetY)
                }
            }
            on<StudyModeReset> {
                Matrix()
            }
            on<Reset> {
                Matrix()
            }
            on<BindModel> {
                Matrix()
            }
            on<MoveModel> { Matrix() }
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
            on<Reset> {
                false
            }
            on<BindModel> {
                false
            }
            on<MoveModel> { event.reverseColor }
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
            on<Reset> {
                ColorMatrix()
            }
            on<BindModel> {
                ColorMatrix()
            }
            on<MoveModel> {
                ColorMatrix().apply {
                    if (event.reverseColor) {
                        postConcat(reverseMatrix)
                    }
                }
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
            on<Reset> {
                false
            }
            on<BindModel> {
                false
            }
            on<MoveModel> { event.pseudoColor }
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
            on<Reset> { Measure.NONE }
            on<BindModel> {
                Measure.NONE
            }
            on<MoveModel> { Measure.NONE }
        }

        reduce(ImageFrameStore::canvasModel) {
            on<ImageCanvasModel> { event }
            on<DrawLines> { state.copy(tmp = event.tmp, canUndo = event.canUndo) }
            on<ResetMeasure> { ImageCanvasModel() }
            on<Reset> { ImageCanvasModel() }
            on<BindModel> { ImageCanvasModel() }
            on<MoveModel> { event.canvasModel }
            on<ClearMeasure> { ImageCanvasModel() }
        }
    }

    /**
     * 获取当前位置矩阵的反矩阵, 给测量模式计算点所赢处的原始坐标
     */
    fun getInvertMatrix() = Matrix().apply {
        compositeMatrix.invert(this)
    }

    private fun getNewScaleFactor(rawScaleFactor: Float): Float =
        Math.max(1.0f, Math.min(rawScaleFactor * gestureScale, 5.0f))

    /**
     * 修改是否允许播放
     */
    class SetAllowPlay(val value: Boolean)

    /**
     * 绑定模型事件(点击/拖放缩略图列表中的元素)
     */
    data class BindModel(
        val modeId: String,
        val patient: DicomPatientMetaInfo,
        val study: DicomStudyMetaInfo,
        val series: DicomSeriesMetaInfo,
        val size: Int
    )

    /**
     * 移动模型事件(拖放图像单元格中的图像)
     */
    data class MoveModel(
        val modeId: String,
        val patient: DicomPatientMetaInfo,
        val study: DicomStudyMetaInfo,
        val series: DicomSeriesMetaInfo,
        val size: Int,
        val bitmap: Bitmap,
        val index: Int,
        val meta: DicomImageMetaInfo,
        val reverseColor: Boolean,
        val pseudoColor: Boolean,
        val canvasModel: ImageCanvasModel
    )

    data class ShowImage(val bitmap: Bitmap, val index: Int, val meta: DicomImageMetaInfo)
    data class PlayAnimation(val images: List<Bitmap>)
    /**
     * 播放过程中更新图像的序号和元信息
     */
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

    data class ScaleChange(val scaleFactor: Float, val focus: PointF)
    data class LocationTranslate(val distanceX: Float, val distanceY: Float)
    data class DrawLines(val tmp: Bitmap?, val canUndo: Boolean)
    class ClearMeasure

    enum class Measure {
        NONE,
        LINE,
        ANGEL
    }

    companion object {
        private val reverseMatrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}