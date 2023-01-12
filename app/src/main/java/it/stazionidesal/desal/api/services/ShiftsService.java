package it.stazionidesal.desal.api.services;

import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.model.response.ShiftResponse;
import it.stazionidesal.desal.api.services.shifts.ShiftData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import it.stazionidesal.desal.api.model.request.ShiftRevisionRequest;
import it.stazionidesal.desal.api.model.response.EndShiftResponse;
import it.stazionidesal.desal.api.model.response.NewShiftResponse;
import it.stazionidesal.desal.api.model.response.ShiftsArchiveResponse;
import it.stazionidesal.desal.api.services.shifts.ShiftEditDataRequest;

public interface ShiftsService {

    @POST("shifts/begin")
    Call<NewShiftResponse> beginShift(@Query("sid") String sid, @Body ShiftData initialData);

    @POST("shifts/end/{rid}")
    Call<EndShiftResponse> endShift(@Path("rid") String rid, @Body ShiftData endData);

    @POST("shifts/{rid}")
    Call<ShiftResponse> getShift(@Path("rid") String rid);

    @POST("shifts/active/{sid}")
    Call<ShiftResponse> getActiveShiftForStation(@Path("sid") String sid);

    @POST("shifts/ended/{sid}")
    Call<ShiftsArchiveResponse> getEndedShiftsForStation(@Path("sid") String sid);

    @POST("shifts/mostRecent/{sid}")
    Call<ShiftResponse> getMostRecentEndedShift(@Path("sid") String sid);

    @POST("shifts/edit/{rid}")
    Call<BaseResponse> editShift(@Path("rid") String rid, @Body ShiftEditDataRequest shiftEditDataRequest);

    @POST("shifts/revise/{rid}")
    Call<BaseResponse> reviseShift(@Path("rid") String rid, @Body ShiftRevisionRequest revisionRequest);

    @POST("shifts/unrevise/{rid}")
    Call<BaseResponse> unreviseShift(@Path("rid") String rid);

    @POST("shifts/onPumpRemoved/{rid}")
    Call<BaseResponse> removeShift(@Path("rid") String rid);
}
