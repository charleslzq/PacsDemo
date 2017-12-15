package com.github.charleslzq.pacsdemo.support

import android.util.Log
import android.util.LruCache
import com.github.charleslzq.dicom.data.DicomPatient

/**
 * Created by charleslzq on 17-12-15.
 */
object CacheUtil {
    val PATIENT = "patient"
    private val registry = mutableMapOf<CacheKey<*>, LruCache<String, *>>()

    init {
        with(registry) {
            val patientCache = LruCache<String, DicomPatient>(3)
            put(CacheKey(PATIENT, DicomPatient::class.java), patientCache)
        }
    }

    fun <T> cache(
            cacheName: String,
            storageType: Class<T>,
            vararg args: String,
            keyGenerator: (Array<String>) -> String = { it.joinToString("#") },
            onMiss: (Array<String>) -> T?) : T? {
        val key = CacheKey(cacheName, storageType)
        if (!registry.containsKey(key)) {
            throw IllegalStateException("Cache for $cacheName, $storageType is not configured")
        } else {
            @Suppress("UNCHECKED_CAST")
            val cache = registry[key]!! as LruCache<String, T>
            val arguments = arrayOf(*args)
            val objectKey = keyGenerator(arguments)
            val objectInCache = storageType.cast(cache[objectKey])
            val debugOnMiss: (Array<String>) -> T? = {
                Log.d("Cache $cacheName", "miss for $objectKey")
                onMiss(it)
            }
            return objectInCache ?: debugOnMiss(arguments)?.also { cache.put(objectKey, it) }
        }
    }

    data class CacheKey<T>(
            val name: String,
            val type: Class<T>
    )
}