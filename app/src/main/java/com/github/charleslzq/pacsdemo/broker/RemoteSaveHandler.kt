package com.github.charleslzq.pacsdemo.broker

import com.github.charleslzq.dicom.store.DicomImageFileSaveHandler
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created by charleslzq on 17-11-15.
 */
class RemoteSaveHandler(
        private val dicomMessageBroker: DicomMessageBroker
) : DicomImageFileSaveHandler {

    override fun save(path: Path, imageMap: Map<String, URI>): Map<String, URI> {
        val imageDir = path.toFile()
        imageDir.mkdirs()
        val imagePath = imageDir.absolutePath
        val fileUris = imageMap.map { it.value.toString() }.toList()
        dicomMessageBroker.requireFiles(imagePath, fileUris)
        return imageMap.map { it.key to newUri(imagePath, it.value) }.toMap()
    }

    private fun newUri(imageDir: String, uri: URI): URI {
        return Paths.get(imageDir, Paths.get(uri).toFile().name).toUri()
    }
}