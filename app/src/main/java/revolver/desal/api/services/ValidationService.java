package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;
import revolver.desal.api.model.response.BaseResponse;

public interface ValidationService {

    @POST("users/verify/{token}")
    Call<BaseResponse> validateUserWithToken(@Path("token") String validationToken);

}
