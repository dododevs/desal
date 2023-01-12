package it.stazionidesal.desal.api.services.transactions;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.adapter.TransactionTypeAdapter;
import it.stazionidesal.desal.api.services.transactions.payment.PaymentMethod;

public class Transaction implements Parcelable {

    @SerializedName("tid")
    protected String tid;

    @JsonAdapter(TransactionTypeAdapter.class)
    @SerializedName("type")
    protected TransactionType type;

    @SerializedName("amount")
    protected double amount;

    @SerializedName("paymentMethod")
    protected PaymentMethod paymentMethod;

    public Transaction() {
    }

    public Transaction(TransactionType type, double amount, PaymentMethod paymentMethod) {
        this.type = type;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getTid() {
        return tid;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    protected Transaction(Parcel src) {
        this.tid = src.readString();
        this.type = TransactionType.fromString(src.readString());
        this.amount = src.readDouble();
        this.paymentMethod = src.readParcelable(PaymentMethod.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tid);
        dest.writeString(type.toString());
        dest.writeDouble(amount);
        dest.writeParcelable(paymentMethod, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel source) {
            return new Transaction(source);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
}
