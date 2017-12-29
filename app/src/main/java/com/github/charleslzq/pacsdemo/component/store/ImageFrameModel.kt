package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-11-29.
 */
data class ImageFrameModel(
        val meta: DicomImageMetaInfo,
        val frame: URI = meta.let { it.files[DEFAULT] }!!
) {
    companion object {
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}
