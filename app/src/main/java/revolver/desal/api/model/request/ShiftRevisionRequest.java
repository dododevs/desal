package revolver.desal.api.model.request;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.shifts.revision.RevisionData;

public class ShiftRevisionRequest {

    @SerializedName("revision")
    private final RevisionData revisionData;

    @SerializedName("ignoreMaximumDifference")
    private final boolean ignoreMaximumDifference;

    public ShiftRevisionRequest(RevisionData data, boolean ignoreMaximumDifference) {
        this.revisionData = data;
        this.ignoreMaximumDifference = ignoreMaximumDifference;
    }
}
