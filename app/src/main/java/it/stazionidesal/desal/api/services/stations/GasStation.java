package it.stazionidesal.desal.api.services.stations;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GasStation implements Parcelable {

    @SerializedName("sid")
    private String sid;

    @SerializedName("name")
    private String name;

    @SerializedName("gplClockCount")
    private int gplClockCount;

    @SerializedName("lastActivity")
    private long lastActivity;

    @SerializedName("pumps")
    private List<GasPump> pumps;

    @SerializedName("prices")
    private List<GasPrice> prices;

    public GasStation() {
    }

    public GasStation(String name, int gplClockCount, List<GasPump> pumps, List<GasPrice> prices) {
        this.name = name;
        this.pumps = pumps;
        this.prices = prices;
        this.gplClockCount = gplClockCount;
    }

    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public int getGplClockCount() {
        return gplClockCount;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public List<GasPump> getPumps() {
        return pumps;
    }

    public List<GasPrice> getPrices() {
        return prices;
    }

    @NonNull
    @Override
    public String toString() {
        return "GasStation{" + getSid() + "}";
    }

    private GasStation(Parcel src) {
        this.sid = src.readString();
        this.name = src.readString();
        this.gplClockCount = src.readInt();
        this.lastActivity = src.readLong();
        this.pumps = src.createTypedArrayList(GasPump.CREATOR);
        this.prices = src.createTypedArrayList(GasPrice.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sid);
        dest.writeString(name);
        dest.writeInt(gplClockCount);
        dest.writeLong(lastActivity);
        dest.writeTypedList(pumps);
        dest.writeTypedList(prices);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GasStation> CREATOR = new Creator<GasStation>() {
        @Override
        public GasStation createFromParcel(Parcel source) {
            return new GasStation(source);
        }

        @Override
        public GasStation[] newArray(int size) {
            return new GasStation[size];
        }
    };
}
