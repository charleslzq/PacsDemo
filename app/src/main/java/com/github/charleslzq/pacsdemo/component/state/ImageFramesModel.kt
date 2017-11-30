package com.github.charleslzq.pacsdemo.component.state

import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-11-29.
 */
data class ImageFramesModel(
        val frames: List<DicomImageMetaInfo> = emptyList(),
        val size: Int = frames.size,
        val frameUrls: List<URI> = frames.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[ImageFramesViewState.DEFAULT] },
        var thumbUrl: URI? = when (frames.isNotEmpty()) {
            true -> frames.sortedBy { it.instanceNumber?.toInt() }[0].files[ImageFramesViewState.THUMB]
            false -> null
        }
)
