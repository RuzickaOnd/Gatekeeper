package com.example.gatekeeper

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.jsoup.Jsoup


class GateService {

    val csfrToken = "csrftoken"
    val csrfMiddlewareToken = "csrfmiddlewaretoken"
    val sessionId = "sessionid"

    fun openGate(number: Int, rootView : View, context : Context){

        val gate = Gate(number)

        val sharedPreference = SharedPreference(context)

        val service = RetrofitInstance.getRetrofitService()

//        val call = service.postGateNumber("sessionid=znk8h1038vs9676krnuy6e5d0qwav39",gate)
        val call = service.postGateNumber(sharedPreference.getValueString(sessionId).toString(),gate)

        Log.wtf("URL Called", call.request().url().toString() + "")
        Log.wtf("Header Cookie Called", call.request().header("Cookie").toString() + "")
        Log.wtf("Body Called", call.request().body().toString())

        call.enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                println(message = "Error: "+t.message + "; cause: " + t.cause)
                Snackbar.make(rootView,"Service communication error!", Snackbar.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {

                val code = response.code()

                when {
                    response.isSuccessful -> {
                        if(number>0){
                            println(message = "Response: "+response.body())
                            Snackbar.make(rootView,response.body()?:"No returned message (status $code)", Snackbar.LENGTH_SHORT).show()
                        }else{
                            println(message = "Authorization Success")
                            Snackbar.make(rootView,"Authorization Success", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    code==403 -> {
                        if(number>0){
                            println(message = "Status code $code (Forbidden)")
                            Snackbar.make(rootView,"Status code $code (Forbidden)", Snackbar.LENGTH_SHORT).show()
                        }else{
                            println(message = "Authorization Failed")
                            Snackbar.make(rootView,"Authorization Failed", Snackbar.LENGTH_SHORT).show()
                            getCsrfTokenFromGate(context)
                        }

                    }
                    code==400 -> {
                        println(message = "Status code $code (Bad request)")
                        Snackbar.make(rootView,"Status code $code (Bad request)", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {
                        println(message = "Status code $code")
                        Snackbar.make(rootView,"Status code $code", Snackbar.LENGTH_SHORT).show()
                    }
                }

            }

        })
    }

    fun getCsrfTokenFromGate(context : Context){
        val service = RetrofitInstance.getRetrofitService()
        val call = service.getGateTarget()

        Log.wtf("URL Called", call.request().url().toString() + "")

        call.enqueue(object : Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(message = "Error: "+t.message + "; cause: " + t.cause)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val sharedPreference = SharedPreference(context)

                val code = response.code()

                when {
                    response.isSuccessful -> {

                        println(message = "Status code: $code")

                        //get csrftoken
                        var inputCsrfToken =""
                        val headerResponse : Headers = response.headers()
                        val headerMapList : Map<String, List<String>> = headerResponse.toMultimap()
                        val allCookies : List<String> = headerMapList["Set-Cookie"] ?: emptyList()
                        //val cookies = listOf(response.headers().get("Set-Cookie"))

                        allCookies.forEach {
                            if(it.contains("csrftoken=")){
                                val c = it.split(";")[0]
                                println("Save csrftoken pref: $c")
                                sharedPreference.save(csfrToken,c)
                                inputCsrfToken = c

                            }

                        }

                        //get csrfmiddlewaretoken
                        val html = response.body()?.string()
                        //println(message = "Html: $html")
                        val document = Jsoup.parse(html)
                        val inputCsrfmiddlewaretoken = document.getElementsByAttributeValue("name","csrfmiddlewaretoken").attr("value")
                        println(message = "Save $csrfMiddlewareToken pref: $inputCsrfmiddlewaretoken")
                        sharedPreference.save(csrfMiddlewareToken, inputCsrfmiddlewaretoken)

                        login(context,inputCsrfmiddlewaretoken,inputCsrfToken)

                    }
                    code==403 -> {
                        println(message = "Status code $code (Forbidden)")
                    }
                    else -> {
                        println(message = "Status code $code")
                    }
                }

            }

        })
    }

    fun login(context : Context, csrfmiddlewaretoken : String, csrftoken : String){
        val service = RetrofitInstanceNoRedirect.getRetrofitService()

        //TODO
        val call = service.postLoginFormData(csrftoken,"ruzicka","ders.147",csrfmiddlewaretoken)

        Log.wtf("URL Called", call.request().url().toString() + "")

        call.enqueue(object : Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(message = "Error: "+t.message + "; cause: " + t.cause)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val sharedPreference = SharedPreference(context)

                val code = response.code()

                when {
                    response.isSuccessful -> {

                        println(message = "Status code: $code")

                    }
                    code==302 ->{
                        println(message = "Status code: $code")


                        val headerResponse : Headers = response.headers()
                        val headerMapList : Map<String, List<String>> = headerResponse.toMultimap()
                        val allCookies : List<String> = headerMapList["Set-Cookie"] ?: emptyList()
                        allCookies.forEach{
                            println(message = "One of all cookies: $it")

                            if(it.contains("sessionid=")){
                                val s = it.split(";")[0]
                                println("Save sessionid pref: $s")
                                sharedPreference.save(sessionId,s)
                            }
                            if(it.contains("csrftoken=")){
                                val c = it.split(";")[0]
                                println("Save csrftoken pref: $c")
                                sharedPreference.save(csfrToken,c)
                            }
                        }
                    }
                    else -> {
                        println(message = "Status code $code")
                    }
                }

            }

        })
    }

}