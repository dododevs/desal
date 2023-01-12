package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.services.shifts.Shift;

public class ShiftResponse extends BaseResponse {

    @SerializedName("shift")
    private Shift shift;

    public Shift getShift() {
        return shift;
    }

}
