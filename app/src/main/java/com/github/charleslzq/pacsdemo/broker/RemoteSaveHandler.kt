package com.github.charleslzq.pacsdemo.broker

import com.github.charleslzq.dicom.store.DicomImageFileSaveHandler
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-15.
 */
class RemoteSaveHandler(
    private val dicomMessageBroker: DicomMessageBroker
) : DicomImageFileSaveHandler {

    override fun save(path: String, imageMap: Map<String, URI>): Map<String, URI> {
        val imageDir = File(path)
        imageDir.mkdirs()
        val fileUris = imageMap.map { it.value.toString() }.toList()
        dicomMessageBroker.requireFiles(path, fileUris)
        return imageMap.map { it.key to newUri(path, it.value) }.toMap()
    }

    private fun newUri(imageDir: String, uri: URI) =
        File(imageDir + File.separator + File(uri).name).toURI()
}