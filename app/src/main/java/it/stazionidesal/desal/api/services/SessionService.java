package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.services.users.Session;
import retrofit2.Call;
import retrofit2.http.POST;

public interface SessionService {

    @POST("session/check")
    Call<Session> checkSession();

}
