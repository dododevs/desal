package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.request.ShiftRevisionRequest;
import revolver.desal.api.model.response.EndShiftResponse;
import revolver.desal.api.model.response.NewShiftResponse;
import revolver.desal.api.model.response.ShiftResponse;
import revolver.desal.api.model.response.ShiftsArchiveResponse;
import revolver.desal.api.services.shifts.ShiftData;
import revolver.desal.api.services.shifts.ShiftEditDataRequest;

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
