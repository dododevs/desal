package it.stazionidesal.desal.api.services.stations;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.adapter.PriceFuelAdapter;
import it.stazionidesal.desal.api.adapter.PumpTypeAdapter;

public class GasPrice implements Parcelable {

    @SerializedName("type")
    @JsonAdapter(PumpTypeAdapter.class)
    private PumpType type;

    @SerializedName("fuel")
    @JsonAdapter(PriceFuelAdapter.class)
    private Fuel fuel;

    @SerializedName("price")
    private double price;

    public GasPrice() {
    }

    public GasPrice(PumpType type, Fuel fuel, double price) {
        this.type = type;
        this.fuel = fuel;
        this.price = price;
    }

    public PumpType getType() {
        return type;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public double getPrice() {
        return price;
    }

    @NonNull
    @Override
    public String toString() {
        return "GasPrice{" + getFuel().toString() + ", " + getType() + ": " + getPrice() + "}";
    }

    private GasPrice(Parcel src) {
        this.type = PumpType.fromString(src.readString());
        this.fuel = Fuel.fromString(src.readString());
        this.price = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type.toString());
        dest.writeString(fuel.toString());
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GasPrice> CREATOR = new Creator<GasPrice>() {
        @Override
        public GasPrice createFromParcel(Parcel source) {
            return new GasPrice(source);
        }

        @Override
        public GasPrice[] newArray(int size) {
            return new GasPrice[size];
        }
    };
}
