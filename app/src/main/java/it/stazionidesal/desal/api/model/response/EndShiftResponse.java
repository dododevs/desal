package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.services.shifts.ShiftDifferenceData;

public class EndShiftResponse extends BaseResponse {

    @SerializedName("endData")
    private ShiftDifferenceData shiftEndData;

    public ShiftDifferenceData getEndData() {
        return shiftEndData;
    }

}
