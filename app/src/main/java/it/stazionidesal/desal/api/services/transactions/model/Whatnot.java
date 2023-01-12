package it.stazionidesal.desal.api.services.transactions.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import it.stazionidesal.desal.api.adapter.TransactionDirectionAdapter;
import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.api.services.transactions.TransactionDirection;
import it.stazionidesal.desal.api.services.transactions.TransactionType;
import it.stazionidesal.desal.api.services.transactions.payment.Cash;

public class Whatnot extends Transaction implements Parcelable {

    @SerializedName("what")
    private final String what;

    @JsonAdapter(TransactionDirectionAdapter.class)
    @SerializedName("direction")
    private final TransactionDirection direction;

    public Whatnot(double amount, String what, TransactionDirection direction) {
        super(TransactionType.WHATNOT, amount, new Cash());
        this.what = what;
        this.direction = direction;
    }

    public String getWhat() {
        return what;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    private Whatnot(Parcel src) {
        super(src);
        what = src.readString();
        direction = TransactionDirection.fromString(src.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(what);
        dest.writeString(direction.toString());
    }

    public static final Creator<Whatnot> CREATOR = new Creator<Whatnot>() {
        @Override
        public Whatnot createFromParcel(Parcel source) {
            return new Whatnot(source);
        }

        @Override
        public Whatnot[] newArray(int size) {
            return new Whatnot[size];
        }
    };
}
