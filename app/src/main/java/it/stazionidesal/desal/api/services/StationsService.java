package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.model.request.PriceChangeRequest;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.model.response.GasPricesResponse;
import it.stazionidesal.desal.api.model.response.StationsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import it.stazionidesal.desal.api.services.stations.GasStation;

public interface StationsService {

    @POST("stations")
    Call<StationsResponse> getStations();

    @POST("stations/create")
    Call<BaseResponse> createStation(@Body GasStation station);

    @POST("stations/{sid}/prices")
    Call<GasPricesResponse> getPricesForStation(@Path("sid") String sid);

    @POST("stations/{sid}/prices")
    Call<BaseResponse> updatePrices(@Path("sid") String sid, @Body PriceChangeRequest prices);

}
