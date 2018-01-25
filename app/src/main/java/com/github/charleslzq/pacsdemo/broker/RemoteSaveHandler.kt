package com.github.charleslzq.pacsdemo.broker

import com.github.charleslzq.dicom.store.DicomImageFileSaveHandler
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-11-15.
 * 在接受到图像层级的meta.json文件时,需要将其中指定的图片从服务器上下载到本地
 * @param dicomMessageBroker 消息代理, 发送文件请求
 */
class RemoteSaveHandler(
    private val dicomMessageBroker: DicomMessageBroker
) : DicomImageFileSaveHandler {

    /**
     * @param path 本地图像文件根目录
     * @param imageMap 服务器端图像元文件中包含的图像名-位置映射表
     * @return 地的图像名-位置映射表
     */
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