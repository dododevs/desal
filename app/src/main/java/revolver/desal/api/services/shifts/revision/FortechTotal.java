package revolver.desal.api.services.shifts.revision;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.stations.Fuel;
import revolver.desal.api.services.stations.PumpType;

public class FortechTotal implements Parcelable {

    @SerializedName("fuel")
    private final Fuel fuel;

    @SerializedName("type")
    private final PumpType type;

    @SerializedName("litres")
    private final double totalLitres;

    @SerializedName("profit")
    private final double totalProfit;

    public FortechTotal(Fuel fuel, PumpType type, double totalLitres, double totalProfit) {
        this.fuel = fuel;
        this.type = type;
        this.totalLitres = totalLitres;
        this.totalProfit = totalProfit;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public PumpType getType() {
        return type;
    }

    public double getTotalLitres() {
        return totalLitres;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    private FortechTotal(Parcel src) {
        this.fuel = Fuel.fromString(src.readString());
        this.type = PumpType.fromString(src.readString());
        this.totalLitres = src.readDouble();
        this.totalProfit = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fuel.toString());
        dest.writeString(type.toString());
        dest.writeDouble(totalLitres);
        dest.writeDouble(totalProfit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FortechTotal> CREATOR = new Creator<FortechTotal>() {
        @Override
        public FortechTotal createFromParcel(Parcel source) {
            return new FortechTotal(source);
        }

        @Override
        public FortechTotal[] newArray(int size) {
            return new FortechTotal[size];
        }
    };
}
