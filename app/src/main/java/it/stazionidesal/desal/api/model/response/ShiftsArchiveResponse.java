package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.shifts.Shift;

public class ShiftsArchiveResponse extends BaseResponse {

    @SerializedName("shifts")
    private List<Shift> shifts;

    public List<Shift> getShifts() {
        return shifts;
    }
}
