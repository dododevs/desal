package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.shifts.Shift;

public class ShiftResponse extends BaseResponse {

    @SerializedName("shift")
    private Shift shift;

    public Shift getShift() {
        return shift;
    }

}
