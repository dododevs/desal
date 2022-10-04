package revolver.desal.api.services.shifts;

import com.google.gson.annotations.SerializedName;

public class ShiftEditDataRequest {

    @SerializedName("initialData")
    private final ShiftData initialData;

    @SerializedName("endData")
    private final ShiftData endData;

    public ShiftEditDataRequest(ShiftData initialData, ShiftData endData) {
        this.initialData = initialData;
        this.endData = endData;
    }
}
