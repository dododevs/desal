package it.stazionidesal.desal.api.services.shifts;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class GplClockData implements Parcelable {

    @SerializedName("ref")
    private String ref;

    @SerializedName("value")
    private double value;

    public GplClockData() {
    }

    public GplClockData(String ref, double value) {
        this.ref = ref;
        this.value = value;
    }

    public String getRef() {
        return ref;
    }

    public double getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return "GplClockData{ref=" + this.ref + ", value=" + this.value + "}";
    }

    private GplClockData(Parcel src) {
        this.ref = src.readString();
        this.value = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ref);
        dest.writeDouble(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GplClockData> CREATOR = new Creator<GplClockData>() {
        @Override
        public GplClockData createFromParcel(Parcel source) {
            return new GplClockData(source);
        }

        @Override
        public GplClockData[] newArray(int size) {
            return new GplClockData[size];
        }
    };
}
