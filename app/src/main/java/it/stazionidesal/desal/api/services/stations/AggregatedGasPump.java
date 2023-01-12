package it.stazionidesal.desal.api.services.stations;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AggregatedGasPump extends GasPump implements Parcelable {

    private final List<Fuel> fuels;

    public AggregatedGasPump(String display, List<Fuel> fuels, PumpType type) {
        super(display, null, type);
        this.fuels = fuels;
    }

    public List<Fuel> getFuels() {
        return fuels;
    }

    private AggregatedGasPump(Parcel src) {
        this.pid = src.readString();
        this.display = src.readString();

        this.fuels = new ArrayList<>();
        int count = src.readInt();
        for (int i = 0; i < count; i++) {
            this.fuels.add(Fuel.fromString(src.readString()));
        }

        this.type = PumpType.fromString(src.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeString(display);
        dest.writeInt(fuels.size());
        for (final Fuel fuel : fuels) {
            dest.writeString(fuel.toString());
        }
        dest.writeString(type.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AggregatedGasPump> CREATOR = new Creator<AggregatedGasPump>() {
        @Override
        public AggregatedGasPump createFromParcel(Parcel source) {
            return new AggregatedGasPump(source);
        }

        @Override
        public AggregatedGasPump[] newArray(int size) {
            return new AggregatedGasPump[size];
        }
    };
}
