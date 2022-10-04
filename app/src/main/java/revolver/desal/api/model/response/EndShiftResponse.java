package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.shifts.ShiftDifferenceData;

public class EndShiftResponse extends BaseResponse {

    @SerializedName("endData")
    private ShiftDifferenceData shiftEndData;

    public ShiftDifferenceData getEndData() {
        return shiftEndData;
    }

}
