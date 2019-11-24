package cz.ders.gatekeeper

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RestApiService {

    @Headers("Content-Type: application/json")
    @POST("gateapi/")
    fun postGateNumber(@Header("Cookie") sessionid: String, @Body body: Gate): Call<String>

//    @Headers("Referer: https://gatekeeper.ders.cz/accounts/login/")
    @FormUrlEncoded
    @POST("accounts/login/")
    fun postLoginFormData(@Header("Referer") referer: String, @Header("Cookie") csrftoken: String, @Field("username") username : String, @Field("password") password : String, @Field("csrfmiddlewaretoken") csrfmiddlewaretoken : String) : Call<ResponseBody>

    @GET("gatekeeper/")
    fun getGateTarget() : Call <ResponseBody>
}
