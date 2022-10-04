package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {

    @SerializedName("success")
    private boolean isSuccessful;

    @SerializedName("status")
    private int status;

    @SerializedName("reason")
    private String error;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

}
