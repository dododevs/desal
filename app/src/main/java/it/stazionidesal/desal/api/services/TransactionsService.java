package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.model.response.TransactionsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.model.request.MultipleTransactionsRegisterRequest;

public interface TransactionsService {

    @POST("transactions/register")
    Call<BaseResponse> registerTransactions(@Query("sid") String sid,
                                            @Body MultipleTransactionsRegisterRequest transactions);

    @POST("transactions/{rid}")
    Call<TransactionsResponse> getTransactionsForShift(@Path("rid") String rid);

    @POST("transactions/{tid}/remove")
    Call<BaseResponse> removeTransaction(@Path("tid") String tid);
}
