package revolver.desal.api.services.shifts.revision;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import revolver.desal.api.adapter.FortechTotalsAdapter;

public class EstimatedIncomes implements Parcelable {

    @SerializedName("initialFund")
    private final double initialFund;

    @SerializedName("fortechTotals")
    @JsonAdapter(FortechTotalsAdapter.class)
    private final List<FortechTotal> fortechTotals;

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

    public List<FortechTotal> getFortechTotals() {
        return fortechTotals;
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
        this.fortechTotals = src.createTypedArrayList(FortechTotal.CREATOR);
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
        dest.writeTypedList(fortechTotals);
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
