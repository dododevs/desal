package revolver.desal.api.services.transactions.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.api.services.transactions.TransactionType;
import revolver.desal.api.services.transactions.payment.Coupon;

public class CouponPayment extends Transaction implements Parcelable {

    @SerializedName("customer")
    private final String customer;

    public CouponPayment(double amount, String customer) {
        super(TransactionType.COUPON_PAYMENT, amount, new Coupon());
        this.customer = customer;
    }

    public String getCustomer() {
        return customer;
    }

    private CouponPayment(Parcel src) {
        super(src);
        this.customer = src.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(customer);
    }

    public static final Creator<CouponPayment> CREATOR = new Creator<CouponPayment>() {
        @Override
        public CouponPayment createFromParcel(Parcel source) {
            return new CouponPayment(source);
        }

        @Override
        public CouponPayment[] newArray(int size) {
            return new CouponPayment[size];
        }
    };

}
