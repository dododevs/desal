package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.POST;
import revolver.desal.api.model.response.PdfModelsResponse;

public interface PdfModelsService {

    @POST("models")
    Call<PdfModelsResponse> getModels();

}
