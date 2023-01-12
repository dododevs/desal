package it.stazionidesal.desal.api.services.shifts.revision;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class EstimatedIncomes implements Parcelable {

    @SerializedName("initialFund")
    private final double initialFund;

    @SerializedName("fortechTotal")
    private final FortechTotal fortechTotal;

    @SerializedName("gplTotal")
    private final double gplTotal;

    @SerializedName("accessoriesTotal")
    private final double accessoriesTotal;

    @SerializedName("oilTotal")
    private final double oilTotal;

    @SerializedName("whatnotTotal")
    private final double whatnotTotal;

    @SerializedName("optUnsupplied")
    private final double optUnsupplied;

    @SerializedName("grandTotal")
    private final double grandTotal;

    public double getInitialFund() {
        return initialFund;
    }

    public FortechTotal getFortechTotal() {
        return fortechTotal;
    }

    public double getGplTotal() {
        return gplTotal;
    }

    public double getAccessoriesTotal() {
        return accessoriesTotal;
    }

    public double getOilTotal() {
        return oilTotal;
    }

    public double getWhatnotTotal() {
        return whatnotTotal;
    }

    public double getOptUnsupplied() {
        return optUnsupplied;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    private EstimatedIncomes(Parcel src) {
        this.initialFund = src.readDouble();
        this.fortechTotal = src.readParcelable(FortechTotal.class.getClassLoader());
        this.gplTotal = src.readDouble();
        this.accessoriesTotal = src.readDouble();
        this.oilTotal = src.readDouble();
        this.whatnotTotal = src.readDouble();
        this.optUnsupplied = src.readDouble();
        this.grandTotal = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(initialFund);
        dest.writeParcelable(fortechTotal, 0);
        dest.writeDouble(gplTotal);
        dest.writeDouble(accessoriesTotal);
        dest.writeDouble(oilTotal);
        dest.writeDouble(whatnotTotal);
        dest.writeDouble(optUnsupplied);
        dest.writeDouble(grandTotal);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EstimatedIncomes> CREATOR = new Creator<EstimatedIncomes>() {
        @Override
        public EstimatedIncomes createFromParcel(Parcel source) {
            return new EstimatedIncomes(source);
        }

        @Override
        public EstimatedIncomes[] newArray(int size) {
            return new EstimatedIncomes[size];
        }
    };
}
