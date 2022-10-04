package revolver.desal.api.services.transactions.model;

import android.os.Parcel;
import android.os.Parcelable;

import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.api.services.transactions.TransactionType;
import revolver.desal.api.services.transactions.payment.CreditCard;

public class CreditCardPayment extends Transaction implements Parcelable {

    public CreditCardPayment(double amount) {
        super(TransactionType.CREDIT_CARD_PAYMENT, amount, new CreditCard());
    }

    private CreditCardPayment(Parcel src) {
        super(src);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<CreditCardPayment> CREATOR = new Creator<CreditCardPayment>() {
        @Override
        public CreditCardPayment createFromParcel(Parcel source) {
            return new CreditCardPayment(source);
        }

        @Override
        public CreditCardPayment[] newArray(int size) {
            return new CreditCardPayment[size];
        }
    };
}
