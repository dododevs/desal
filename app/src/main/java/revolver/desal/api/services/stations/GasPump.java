package revolver.desal.api.services.stations;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import revolver.desal.api.adapter.PumpTypeAdapter;
import revolver.desal.api.adapter.PumpFuelAdapter;

public class GasPump implements Parcelable {

    @SerializedName("pid")
    String pid;

    @SerializedName("display")
    String display;

    @SerializedName("fuel")
    @JsonAdapter(PumpFuelAdapter.class)
    private Fuel availableFuel;

    @SerializedName("type")
    @JsonAdapter(PumpTypeAdapter.class)
    PumpType type;

    GasPump() {
    }

    public GasPump(String display, Fuel availableFuel, PumpType type) {
        this.display = display;
        this.availableFuel = availableFuel;
        this.type = type;
    }

    public String getPid() {
        return pid;
    }

    public String getDisplay() {
        return display;
    }

    public Fuel getAvailableFuel() {
        return availableFuel;
    }

    public PumpType getType() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return "GasPump{" + getPid() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof GasPump && pid != null && pid.equals(((GasPump) obj).getPid()));
    }

    private GasPump(Parcel src) {
        this.pid = src.readString();
        this.display = src.readString();
        this.availableFuel = Fuel.fromString(src.readString());
        this.type = PumpType.fromString(src.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeString(display);
        dest.writeString(availableFuel.toString());
        dest.writeString(type.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GasPump> CREATOR = new Creator<GasPump>() {
        @Override
        public GasPump createFromParcel(Parcel source) {
            return new GasPump(source);
        }

        @Override
        public GasPump[] newArray(int size) {
            return new GasPump[size];
        }
    };
}
