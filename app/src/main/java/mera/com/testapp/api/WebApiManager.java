package mera.com.testapp.api;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class WebApiManager {
    private static final String API_ENDPOINT = "https://opensky-network.org/api/";

    private WebApiInterface mWebApiInterface;

    WebApiManager() {
        mWebApiInterface = getRetrofit().create(WebApiInterface.class);
    }

    @NonNull
    WebApiInterface getWebApiInterface() {
        return mWebApiInterface;
    }

    @NonNull
    private Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient())
                .build();
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build();
    }
}
