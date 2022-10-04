package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.POST;
import revolver.desal.api.services.users.Session;

public interface SessionService {

    @POST("session/check")
    Call<Session> checkSession();

}
