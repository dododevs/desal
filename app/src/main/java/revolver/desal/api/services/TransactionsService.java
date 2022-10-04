package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.request.MultipleTransactionsRegisterRequest;
import revolver.desal.api.model.response.TransactionsResponse;

public interface TransactionsService {

    @POST("transactions/register")
    Call<BaseResponse> registerTransactions(@Query("sid") String sid,
                                            @Body MultipleTransactionsRegisterRequest transactions);

    @POST("transactions/{rid}")
    Call<TransactionsResponse> getTransactionsForShift(@Path("rid") String rid);

    @POST("transactions/onPumpRemoved/{tid}")
    Call<BaseResponse> removeTransaction(@Path("tid") String tid);
}
