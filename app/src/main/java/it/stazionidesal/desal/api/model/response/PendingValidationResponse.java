package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.services.users.PendingValidation;

public class PendingValidationResponse extends BaseResponse {

    @SerializedName("validation")
    private PendingValidation pendingValidation;

    public PendingValidation getPendingValidation() {
        return pendingValidation;
    }
}
