package revolver.desal.api.services.transactions.payment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PaymentMethod implements Parcelable {

    @SerializedName("type")
    private String type;

    public PaymentMethod() {
    }

    public PaymentMethod(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private PaymentMethod(Parcel src) {
        this.type = src.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PaymentMethod> CREATOR = new Creator<PaymentMethod>() {
        @Override
        public PaymentMethod createFromParcel(Parcel source) {
            final PaymentMethod paymentMethod = new PaymentMethod(source);
            switch (paymentMethod.getType()) {
                case "cash":
                    return new Cash();
                case "card":
                    return new CreditCard();
                case "coupon":
                    return new Coupon();
                default:
                    return paymentMethod;
            }
        }

        @Override
        public PaymentMethod[] newArray(int size) {
            return new PaymentMethod[size];
        }
    };
}
