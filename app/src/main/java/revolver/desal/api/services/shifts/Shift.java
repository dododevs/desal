package revolver.desal.api.services.shifts;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.shifts.revision.Revision;

public class Shift implements Parcelable {

    @SerializedName("rid")
    private final String rid;

    @SerializedName("start")
    private final long start;

    @SerializedName("end")
    private final long end;

    @SerializedName("done")
    private final boolean isDone;

    @SerializedName("uid")
    private final String uid;

    @SerializedName("fullName")
    private final String fullName;

    @SerializedName("sid")
    private final String sid;

    @SerializedName("initialData")
    private final ShiftData initialData;

    @SerializedName("endData")
    private final ShiftData endData;

    @SerializedName("differenceData")
    private final ShiftDifferenceData differenceData;

    @SerializedName("revision")
    private final Revision revision;

    public String getRid() {
        return rid;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getUid() {
        return uid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSid() {
        return sid;
    }

    public ShiftData getInitialData() {
        return initialData;
    }

    public ShiftData getEndData() {
        return endData;
    }

    public ShiftDifferenceData getDifferenceData() {
        return differenceData;
    }

    public Revision getRevision() {
        return revision;
    }

    private Shift(Parcel src) {
        this.rid = src.readString();
        this.start = src.readLong();
        this.end = src.readLong();
        this.isDone = src.readInt() == 1;
        this.uid = src.readString();
        this.fullName = src.readString();
        this.sid = src.readString();
        this.initialData = src.readParcelable(ShiftData.class.getClassLoader());
        this.endData = src.readParcelable(ShiftData.class.getClassLoader());
        this.differenceData = src.readParcelable(ShiftDifferenceData.class.getClassLoader());
        this.revision = src.readParcelable(Revision.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rid);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeInt(isDone ? 1 : 0);
        dest.writeString(uid);
        dest.writeString(fullName);
        dest.writeString(sid);
        dest.writeParcelable(initialData, 0);
        dest.writeParcelable(endData, 0);
        dest.writeParcelable(differenceData, 0);
        dest.writeParcelable(revision, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Shift> CREATOR = new Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel source) {
            return new Shift(source);
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
        }
    };
}
