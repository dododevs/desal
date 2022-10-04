package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import revolver.desal.api.model.response.PendingValidationResponse;
import revolver.desal.api.services.users.Session;
import revolver.desal.api.services.users.UserCredentials;

public interface UsersService {

    @POST("users/create")
    Call<PendingValidationResponse> createUser(@Body UserCredentials credentials);

    @POST("users/login")
    Call<Session> login(@Query("username") String username, @Query("password") String password);

}
