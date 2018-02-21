package mera.com.testapp.api;

import mera.com.testapp.api.models.States;
import retrofit2.Call;
import retrofit2.http.GET;

public interface WebApiInterface {
    @GET("states/all")
    Call<States> getStates();
}
