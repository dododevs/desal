package it.stazionidesal.desal.api.services.transactions.model;

import android.os.Parcel;
import android.os.Parcelable;

import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.api.services.transactions.TransactionType;
import it.stazionidesal.desal.api.services.transactions.payment.Cash;

public class Deposit extends Transaction implements Parcelable {

    public Deposit(double amount) {
        super(TransactionType.DEPOSIT, amount, new Cash());
    }

    private Deposit(Parcel src) {
        super(src);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tid);
        dest.writeString(type.toString());
        dest.writeDouble(amount);
        dest.writeParcelable(paymentMethod, 0);
    }

    public static final Creator<Deposit> CREATOR = new Creator<Deposit>() {
        @Override
        public Deposit createFromParcel(Parcel source) {
            return new Deposit(source);
        }

        @Override
        public Deposit[] newArray(int size) {
            return new Deposit[size];
        }
    };
}
