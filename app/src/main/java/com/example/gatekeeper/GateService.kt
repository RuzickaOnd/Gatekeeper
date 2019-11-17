package com.example.gatekeeper

import android.content.Context
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
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

        val sharedPreference = SharedPreference(context)

        val gate = Gate(number)

        val service = RetrofitInstance.getRetrofitService()

        val call = service.postGateNumber(sharedPreference.getValueString(sessionId).toString(),gate)

        Log.wtf("URL Called", call.request().url().toString() + "")
        Log.wtf("Header Cookie Called", call.request().header("Cookie").toString() + "")
        Log.wtf("Body Called", call.request().body().toString())

        call.enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                println(message = "Error: "+t.message + "; cause: " + t.cause)
                Snackbar.make(rootView,context.resources.getString(R.string.service_communication_error), Snackbar.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {

                val code = response.code()

                when {
                    response.isSuccessful -> {
                        if(number>0){
                            println(message = "Response: "+response.body())
                            Snackbar.make(rootView,response.body()?:context.resources.getString(R.string.no_returned_message)+" ("+context.resources.getString(R.string.status_code)+" "+code+")", Snackbar.LENGTH_SHORT).show()
                        }else{
                            println(message = "Authorization Success")
                            Snackbar.make(rootView,context.resources.getString(R.string.authorization_success), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    code==403 -> {
                        if(number>0){
                            println(message = "Status code $code (Forbidden) ... trying again")
                            Snackbar.make(rootView,context.resources.getString(R.string.status_code)+" "+code+" ("+context.resources.getString(R.string.forbidden)+")", Snackbar.LENGTH_SHORT).show()
                        }else{
                            println(message = "Authorization Failed")
                            Snackbar.make(rootView,context.resources.getString(R.string.authorization_failed)+" ... "+context.resources.getString(R.string.trying_again), Snackbar.LENGTH_SHORT).show()
                        }
                        //retry login
                        getCsrfTokenFromGate(rootView, context)
                    }
                    code==400 -> {
                        println(message = "Status code $code (Bad request)")
                        Snackbar.make(rootView,context.resources.getString(R.string.status_code)+" "+code+" ("+context.resources.getString(R.string.bad_request)+")", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {
                        println(message = "Status code $code")
                        Snackbar.make(rootView,context.resources.getString(R.string.status_code)+" "+code, Snackbar.LENGTH_SHORT).show()
                    }
                }

            }

        })
    }

    fun getCsrfTokenFromGate(rootView : View, context : Context){
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

                        println(message = context.resources.getString(R.string.status_code)+" "+code)

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

                        //call login
                        login(rootView, context,inputCsrfmiddlewaretoken,inputCsrfToken)

                    }
                    code==403 -> {
                        println(message = "Status code $code (Forbidden)")
                        Snackbar.make(rootView,context.resources.getString(R.string.status_code)+" "+code+" ("+context.resources.getString(R.string.forbidden)+")", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {
                        println(message = "Status code $code")
                        Snackbar.make(rootView,context.resources.getString(R.string.status_code)+" "+code, Snackbar.LENGTH_SHORT).show()
                    }
                }

            }

        })
    }

    fun login(rootView : View, context : Context, csrfmiddlewaretoken : String, csrftoken : String){
        val service = RetrofitInstanceNoRedirect.getRetrofitService()

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val username = prefs.getString("username","") ?: ""
        val password = prefs.getString("password","") ?: ""

        if(username.isEmpty() || password.isEmpty()){
            Snackbar.make(rootView,context.resources.getString(R.string.username_password_empty), Snackbar.LENGTH_INDEFINITE).show()
            return
        }

        val call = service.postLoginFormData(csrftoken,username,password,csrfmiddlewaretoken)

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
                        Snackbar.make(rootView,context.resources.getString(R.string.login_failed), Snackbar.LENGTH_INDEFINITE).show()
                    }
                    code==302 -> {

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

                                if(s.isNotEmpty()){
                                    Snackbar.make(rootView,context.resources.getString(R.string.login_success), Snackbar.LENGTH_LONG).show()
                                }
                            }
                            if(it.contains("csrftoken=")){
                                val c = it.split(";")[0]
                                println("Save csrftoken pref: $c")
                                sharedPreference.save(csfrToken,c)
                            }
                        }

                    }
                    else -> {

                        println(message = context.resources.getString(R.string.status_code)+" "+code)
                        Snackbar.make(rootView,context.resources.getString(R.string.status_code)+" "+code+" => "+context.resources.getString(R.string.login_failed), Snackbar.LENGTH_LONG).show()
                    }
                }

            }

        })
    }

}