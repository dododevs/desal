package revolver.desal.api.services.shifts.revision;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ActualIncomes implements Parcelable {

    @SerializedName("endFund")
    private final double endFund;

    @SerializedName("depositTotal")
    private final double depositTotal;

    @SerializedName("couponTotal")
    private final double couponTotal;

    @SerializedName("creditCardsTotal")
    private final double creditCardsTotal;

    @SerializedName("optCashTotal")
    private final double optCashTotal;

    @SerializedName("optCreditCardsTotal")
    private final double optCreditCardsTotal;

    @SerializedName("optRefundsTotal")
    private final double optRefundsTotal;

    @SerializedName("cardsTotal")
    private final double cardsTotal;

    @SerializedName("grandTotal")
    private final double grandTotal;

    public double getEndFund() {
        return endFund;
    }

    public double getDepositTotal() {
        return depositTotal;
    }

    public double getCouponTotal() {
        return couponTotal;
    }

    public double getCreditCardsTotal() {
        return creditCardsTotal;
    }

    public double getOptCashTotal() {
        return optCashTotal;
    }

    public double getOptCreditCardsTotal() {
        return optCreditCardsTotal;
    }

    public double getOptRefundsTotal() {
        return optRefundsTotal;
    }

    public double getCardsTotal() {
        return cardsTotal;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    private ActualIncomes(Parcel src) {
        this.endFund = src.readDouble();
        this.depositTotal = src.readDouble();
        this.couponTotal = src.readDouble();
        this.creditCardsTotal = src.readDouble();
        this.optCashTotal = src.readDouble();
        this.optCreditCardsTotal = src.readDouble();
        this.optRefundsTotal = src.readDouble();
        this.cardsTotal = src.readDouble();
        this.grandTotal = src.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(endFund);
        dest.writeDouble(depositTotal);
        dest.writeDouble(couponTotal);
        dest.writeDouble(creditCardsTotal);
        dest.writeDouble(optCashTotal);
        dest.writeDouble(optCreditCardsTotal);
        dest.writeDouble(optRefundsTotal);
        dest.writeDouble(cardsTotal);
        dest.writeDouble(grandTotal);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActualIncomes> CREATOR = new Creator<ActualIncomes>() {
        @Override
        public ActualIncomes createFromParcel(Parcel source) {
            return new ActualIncomes(source);
        }

        @Override
        public ActualIncomes[] newArray(int size) {
            return new ActualIncomes[size];
        }
    };
}
