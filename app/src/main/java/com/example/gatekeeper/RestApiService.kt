package com.example.gatekeeper

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RestApiService {

    @Headers("Content-Type: application/json")
    @POST("gateapi/")
    fun postGateNumber(@Header("Cookie") sessionId: String, @Body body: Gate): Call<String>

    @GET("gatekeeper/")
    fun getGateTarget() : Call <ResponseBody>
}
