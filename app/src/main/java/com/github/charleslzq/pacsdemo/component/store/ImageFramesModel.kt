package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-11-29.
 */
data class ImageFramesModel(
        val frames: List<DicomImageMetaInfo> = emptyList(),
        val size: Int = frames.size,
        val frameUrls: List<URI> = frames.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[DEFAULT] },
        var thumbUrl: URI? = when (frames.isNotEmpty()) {
            true -> frames.sortedBy { it.instanceNumber?.toInt() }[0].files[THUMB]
            false -> null
        }
) {
    companion object {
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}
