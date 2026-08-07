package com.instaflow.app.features.instagram.repository

import android.util.Log
import com.instaflow.app.features.instagram.models.InstagramUiModel
import com.instaflow.app.util.InstagramUrlType
import com.instaflow.app.util.VideoInfo

private const val TAG = "InstagramHandlerDispatch"

object InstagramHandlerDispatch {
    /**
     * Dispatches the [VideoInfo] to the appropriate mapping logic based on [urlType].
     * Currently all types are routed to [InstagramQualityRepository.mapToUiModel] 
     * with the type provided as a hint to skip secondary detection.
     */
    fun handle(
        info: VideoInfo,
        urlType: InstagramUrlType,
    ): InstagramUiModel {
        Log.i(TAG, "[Pipeline] Dispatching VideoInfo — urlType=$urlType, id=${info.id}, uploader=${info.uploader}, title='${info.title}'")
        
        return when (urlType) {
            InstagramUrlType.PROFILE_PIC,
            InstagramUrlType.REEL,
            InstagramUrlType.HIGHLIGHT,
            InstagramUrlType.STORY,
            InstagramUrlType.POST,
            InstagramUrlType.CAROUSEL,
            InstagramUrlType.UNKNOWN -> {
                InstagramQualityRepository.mapToUiModel(info, hintType = urlType)
            }
        }
    }
}
