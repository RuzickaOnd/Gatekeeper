package com.example.gatekeeper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitInstanceNoRedirect {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://gatekeeper.ders.cz/";
    private static RestApiService service;

    public static Retrofit getRetrofitInstanceNoRedirect() {

        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .serializeNulls()
                    .create();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.followRedirects(false);
            OkHttpClient client = builder.build();


            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static RestApiService getRetrofitService(){
        service = RetrofitInstanceNoRedirect.getRetrofitInstanceNoRedirect().create(RestApiService.class);
        return service;
    }
}
