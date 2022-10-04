package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.users.PendingValidation;

public class PendingValidationResponse extends BaseResponse {

    @SerializedName("validation")
    private PendingValidation pendingValidation;

    public PendingValidation getPendingValidation() {
        return pendingValidation;
    }
}
