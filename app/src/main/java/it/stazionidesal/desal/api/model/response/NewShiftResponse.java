package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

public class NewShiftResponse extends BaseResponse {

    @SerializedName("rid")
    private String rid;

    public NewShiftResponse() {
    }

    public NewShiftResponse(String rid) {
        this.rid = rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getRid() {
        return rid;
    }
}
