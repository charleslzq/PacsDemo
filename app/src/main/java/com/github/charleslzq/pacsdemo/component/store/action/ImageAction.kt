package com.github.charleslzq.pacsdemo.component.store.action

import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.pacsdemo.component.store.ImageFramesModel
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore.ModelDropped
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore.ShowImage
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport

/**
 * Created by charleslzq on 17-12-28.
 */
class ImageAction : RxScheduleSupport {
    private var bitmapCache = BitmapCache()
    private var model = ImageFramesModel()

    fun bindModel(imageFramesModel: ImageFramesModel, index: Int = 0): DispatchAction<ImageFramesStore> {
        return { _, dispatch, _ ->
            runOnIo {
                model = imageFramesModel
                dispatch(ModelDropped(model))
                val imageIndex = index.takeIf { (0..(model.frameUrls.size - 1)).contains(it) } ?: 0
                if (model.frameUrls.isNotEmpty()) {
                    bitmapCache.load(model.frameUrls[imageIndex])
                } else {
                    null
                }?.let { dispatch(ShowImage(it, imageIndex)) }
            }
        }
    }
}