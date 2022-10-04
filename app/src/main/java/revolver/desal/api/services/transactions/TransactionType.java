package revolver.desal.api.services.transactions;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import revolver.desal.R;

public enum TransactionType {

    WHATNOT("whatnot", R.string.transaction_type_whatnot, R.drawable.ic_whatnot),
    DEPOSIT("deposit", R.string.transaction_type_deposit, R.drawable.ic_deposit),
    CREDIT_CARD_PAYMENT("creditCardPayment", R.string.transaction_type_credit_card, R.drawable.ic_credit_card),
    COUPON_PAYMENT("couponPayment", R.string.transaction_type_coupon_payment, R.drawable.ic_coupon),
    SALE("sale", R.string.transaction_type_sale, R.drawable.ic_store_mall_directory);

    private final String slug;
    private final @StringRes int stringResource;
    private final @DrawableRes int iconResource;

    TransactionType(String slug, @StringRes int stringResource, @DrawableRes int iconResource) {
        this.slug = slug;
        this.stringResource = stringResource;
        this.iconResource = iconResource;
    }

    public int getStringResource() {
        return stringResource;
    }

    public int getIconResource() {
        return iconResource;
    }

    @NonNull
    @Override
    public String toString() {
        return slug;
    }

    public static TransactionType fromString(String slug) {
        if (slug == null) {
            return null;
        }
        for (final TransactionType type : values()) {
            if (slug.equals(type.slug)) {
                return type;
            }
        }
        return null;
    }
}
