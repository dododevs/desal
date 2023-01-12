package it.stazionidesal.desal.api.services.shifts.revision;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class FortechTotal implements Parcelable {

    @SerializedName("litres")
    private final double totalLitres;

    @SerializedName("profit")
    private final double totalProfit;

    public FortechTotal(double totalLitres, double totalProfit) {
        this.totalLitres = totalLitres;
        this.totalProfit = totalProfit;
    }

    public double getTotalLitres() {
        return totalLitres;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    private FortechTotal(Parcel src) {
        this.totalLitres = src.readDouble();
        this.totalProfit = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
