package revolver.desal.api.model.request;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.shifts.revision.RevisionData;

public class ShiftRevisionRequest {

    @SerializedName("revision")
    private final RevisionData revisionData;

    public ShiftRevisionRequest(RevisionData data) {
        this.revisionData = data;
    }
}
