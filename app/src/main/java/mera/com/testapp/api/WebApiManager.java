package mera.com.testapp.api;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
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
    private static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient())
                .build();
    }

    private static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build();
    }

    <T> T execute(Call<T> request) throws IOException {
        final Response<T> response = request.execute();
        return response.body();
    }
}
