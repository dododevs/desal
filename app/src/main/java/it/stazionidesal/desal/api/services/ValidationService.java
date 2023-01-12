package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.model.response.BaseResponse;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ValidationService {

    @POST("users/verify/{token}")
    Call<BaseResponse> validateUserWithToken(@Path("token") String validationToken);

}
