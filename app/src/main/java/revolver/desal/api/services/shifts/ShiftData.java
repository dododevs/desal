package revolver.desal.api.services.shifts;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShiftData implements Parcelable {

    @SerializedName("gplClock")
    private List<GplClockData> gplClock;

    @SerializedName("fund")
    private double fund;

    @SerializedName("pumps")
    private List<ShiftPumpData> pumpsData;

    public ShiftData() {
    }

    public ShiftData(List<GplClockData> gplClock, double fund, List<ShiftPumpData> pumpsData) {
        this.gplClock = gplClock;
        this.fund = fund;
        this.pumpsData = pumpsData;
    }

    public List<GplClockData> getGplClock() {
        return gplClock;
    }

    public double getFund() {
        return fund;
    }

    public List<ShiftPumpData> getPumpsData() {
        return pumpsData;
    }

    @NonNull
    @Override
    public String toString() {
        return "ShiftData{gplclock=" + gplClock + ", fund=" + fund + ", pumpsData=" + pumpsData + "}";
    }

    private ShiftData(Parcel src) {
        this.gplClock = src.createTypedArrayList(GplClockData.CREATOR);
        this.fund = src.readDouble();
        this.pumpsData = src.createTypedArrayList(ShiftPumpData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(gplClock);
        dest.writeDouble(fund);
        dest.writeTypedList(pumpsData);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShiftData> CREATOR = new Creator<ShiftData>() {
        @Override
        public ShiftData createFromParcel(Parcel source) {
            return new ShiftData(source);
        }

        @Override
        public ShiftData[] newArray(int size) {
            return new ShiftData[size];
        }
    };
}
