package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import java.net.URI

/**
 * Created by charleslzq on 17-12-8.
 */
data class ImagePlayModel(
        val frameMetas: List<DicomImageMetaInfo> = emptyList(),
        val frameUrls: List<URI> = frameMetas.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[ImageFramesModel.DEFAULT] },
        val duration: Int = 40,
        val currentIndex: Int = 0,
        val playing: Boolean = false,
        val framesChanged: Boolean = false
)