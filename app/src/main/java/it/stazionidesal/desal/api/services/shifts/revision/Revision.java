package it.stazionidesal.desal.api.services.shifts.revision;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.shifts.ShiftDifferenceData;
import it.stazionidesal.desal.api.services.transactions.Transaction;

public class Revision implements Parcelable {

    @SerializedName("estimatedIncomes")
    private final EstimatedIncomes estimatedIncomes;

    @SerializedName("actualIncomes")
    private final ActualIncomes actualIncomes;

    @SerializedName("differenceData")
    private final ShiftDifferenceData shiftDifferenceData;

    public EstimatedIncomes getEstimatedIncomes() {
        return estimatedIncomes;
    }

    public ActualIncomes getActualIncomes() {
        return actualIncomes;
    }

    public List<Transaction> getTransactions() {
        return shiftDifferenceData != null ? shiftDifferenceData.getTransactions() : null;
    }

    private Revision(Parcel src) {
        this.estimatedIncomes = src.readParcelable(EstimatedIncomes.class.getClassLoader());
        this.actualIncomes = src.readParcelable(ActualIncomes.class.getClassLoader());
        this.shiftDifferenceData = src.readParcelable(ShiftDifferenceData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(estimatedIncomes, 0);
        dest.writeParcelable(actualIncomes, 0);
        dest.writeParcelable(shiftDifferenceData, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Revision> CREATOR = new Creator<Revision>() {
        @Override
        public Revision createFromParcel(Parcel source) {
            return new Revision(source);
        }

        @Override
        public Revision[] newArray(int size) {
            return new Revision[size];
        }
    };
}
