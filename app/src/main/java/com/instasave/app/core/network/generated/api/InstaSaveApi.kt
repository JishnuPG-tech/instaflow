package com.instasave.app.core.network.generated.api

import com.instasave.app.core.network.generated.model.HealthResponse
import com.instasave.app.core.network.generated.model.MediaInfo
import com.instasave.app.core.network.generated.model.ResolveRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InstaSaveApi {

    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    @POST("api/resolve")
    suspend fun resolveMedia(
        @Body request: ResolveRequest
    ): Response<MediaInfo>
}
