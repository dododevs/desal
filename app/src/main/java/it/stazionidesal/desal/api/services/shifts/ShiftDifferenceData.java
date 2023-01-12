package it.stazionidesal.desal.api.services.shifts;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.stazionidesal.desal.api.adapter.EndShiftPumpDataAdapter;
import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.api.services.transactions.TransactionType;
import it.stazionidesal.desal.api.services.transactions.model.CouponPayment;
import it.stazionidesal.desal.api.services.transactions.model.CreditCardPayment;
import it.stazionidesal.desal.api.services.transactions.model.Deposit;
import it.stazionidesal.desal.api.services.transactions.model.Sale;
import it.stazionidesal.desal.api.services.transactions.model.Whatnot;

public class ShiftDifferenceData implements Parcelable {

    @SerializedName("gplClock")
    private List<GplClockData> gplClock;

    @SerializedName("fund")
    private double fund;

    @JsonAdapter(EndShiftPumpDataAdapter.class)
    @SerializedName("pumps")
    private List<ShiftPumpData> pumpsData;

    @SerializedName("transactions")
    private List<Transaction> transactions;

    public ShiftDifferenceData() {
    }

    public List<GplClockData> getGplClock() {
        return gplClock;
    }

    public double getFund() {
        return fund;
    }

    public List<ShiftPumpData> getPumpsData() {
        return pumpsData;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private ShiftDifferenceData(Parcel src) {
        this.gplClock = src.createTypedArrayList(GplClockData.CREATOR);
        this.fund = src.readDouble();
        this.pumpsData = src.createTypedArrayList(ShiftPumpData.CREATOR);

        /* hack-fix for poor inheritance implementation in Parcelable, shame on you Google. */
        int length = src.readInt();
        // manually create a list of the correct length
        this.transactions = new ArrayList<>(length);
        // looping {size} times...
        for (int i = 0; i < length; i++) {
            // ...fetch the type String first and turn it into the corresponding enum object...
            final TransactionType type = TransactionType.fromString(src.readString());
            if (type == null) {
                // ...ditch this entry if no type is available...
                continue;
            }

            final Transaction transaction;
            // ...invoke the right creator based on the type...
            switch (type) {
                case SALE:
                    transaction = Sale.CREATOR.createFromParcel(src);
                    break;
                case WHATNOT:
                    transaction = Whatnot.CREATOR.createFromParcel(src);
                    break;
                case DEPOSIT:
                    transaction = Deposit.CREATOR.createFromParcel(src);
                    break;
                case CREDIT_CARD_PAYMENT:
                    transaction = CreditCardPayment.CREATOR.createFromParcel(src);
                    break;
                case COUPON_PAYMENT:
                    transaction = CouponPayment.CREATOR.createFromParcel(src);
                    break;
                default:
                    transaction = Transaction.CREATOR.createFromParcel(src);
                    break;
            }
            // ...and finally add the resulting object to the list.
            this.transactions.add(transaction);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(gplClock);
        dest.writeDouble(fund);
        dest.writeTypedList(pumpsData);

        /* hack-fix for poor inheritance implementation in Parcelable, shame on you Google. */
        // write list length to Parcel
        dest.writeInt(transactions.size());
        // for each entry in the list...
        for (final Transaction transaction : transactions) {
            // ...write the String version of its type... (with each type corresponding to a different class later)
            dest.writeString(transaction.getType().toString());
            // ...and then write the actual object to the Parcel.
            transaction.writeToParcel(dest, 0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShiftDifferenceData> CREATOR = new Creator<ShiftDifferenceData>() {
        @Override
        public ShiftDifferenceData createFromParcel(Parcel source) {
            return new ShiftDifferenceData(source);
        }

        @Override
        public ShiftDifferenceData[] newArray(int size) {
            return new ShiftDifferenceData[size];
        }
    };
}
