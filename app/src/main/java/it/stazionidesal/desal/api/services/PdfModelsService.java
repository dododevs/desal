package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.model.response.PdfModelsResponse;
import retrofit2.Call;
import retrofit2.http.POST;

public interface PdfModelsService {

    @POST("models")
    Call<PdfModelsResponse> getModels();

}
