package it.stazionidesal.desal.api.services.shifts;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ShiftPumpData implements Parcelable {

    @SerializedName("pid")
    private String pid;

    @SerializedName("value")
    private double value;

    public ShiftPumpData() {
    }

    public ShiftPumpData(String pid, double value) {
        this.pid = pid;
        this.value = value;
    }

    public String getPid() {
        return pid;
    }

    public double getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return "ShiftPumpData{" + pid + ": " + value + "}";
    }

    private ShiftPumpData(Parcel src) {
        this.pid = src.readString();
        this.value = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeDouble(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShiftPumpData> CREATOR = new Creator<ShiftPumpData>() {
        @Override
        public ShiftPumpData createFromParcel(Parcel source) {
            return new ShiftPumpData(source);
        }

        @Override
        public ShiftPumpData[] newArray(int size) {
            return new ShiftPumpData[size];
        }
    };
}
