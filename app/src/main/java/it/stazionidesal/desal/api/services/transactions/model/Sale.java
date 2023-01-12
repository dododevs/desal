package it.stazionidesal.desal.api.services.transactions.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.services.inventory.Item;
import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.api.services.transactions.TransactionType;
import it.stazionidesal.desal.api.services.transactions.payment.PaymentMethod;

public class Sale extends Transaction implements Parcelable {

    @SerializedName("item")
    private Item item;

    public Sale(double amount, PaymentMethod paymentMethod) {
        super(TransactionType.SALE, amount, paymentMethod);
    }

    public Sale(PaymentMethod paymentMethod) {
        super(TransactionType.SALE, 0.0, paymentMethod);
    }

    public Item getItem() {
        return item;
    }

    private Sale(Parcel src) {
        super(src);
        item = src.readParcelable(Item.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(item, 0);
    }

    public static final Creator<Sale> CREATOR = new Creator<Sale>() {
        @Override
        public Sale createFromParcel(Parcel source) {
            return new Sale(source);
        }

        @Override
        public Sale[] newArray(int size) {
            return new Sale[size];
        }
    };
}
