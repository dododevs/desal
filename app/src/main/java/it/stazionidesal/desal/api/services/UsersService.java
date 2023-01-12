package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.model.response.PendingValidationResponse;
import it.stazionidesal.desal.api.services.users.Session;
import it.stazionidesal.desal.api.services.users.UserCredentials;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UsersService {

    @POST("users/create")
    Call<PendingValidationResponse> createUser(@Body UserCredentials credentials);

    @POST("users/login")
    Call<Session> login(@Query("username") String username, @Query("password") String password);

}
