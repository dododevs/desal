package it.stazionidesal.desal.api.services.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.stations.GasPump;

public class Attributes implements Parcelable {

    @SerializedName("name")
    private final String name;

    @SerializedName("pumps")
    private final List<GasPump> pumps;

    public String getName() {
        return name;
    }

    public List<GasPump> getPumps() {
        return pumps;
    }

    private Attributes(Parcel src) {
        this.name = src.readString();
        this.pumps = src.createTypedArrayList(GasPump.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(pumps);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Attributes> CREATOR = new Creator<Attributes>() {
        @Override
        public Attributes createFromParcel(Parcel source) {
            return new Attributes(source);
        }

        @Override
        public Attributes[] newArray(int size) {
            return new Attributes[size];
        }
    };
}
