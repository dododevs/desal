package revolver.desal.api.services.transactions;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import revolver.desal.R;

public enum TransactionDirection {

    INBOUND("inbound", R.string.transaction_direction_inbound, R.drawable.arrow_left),
    OUTBOUND("outbound", R.string.transaction_direction_outbound, R.drawable.arrow_right);

    private final String slug;
    private final @StringRes int stringResource;
    private final @DrawableRes int iconResource;

    TransactionDirection(String slug, @StringRes int stringResource, @DrawableRes int iconResource) {
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

    public static TransactionDirection fromString(String slug) {
        if (slug == null) {
            return null;
        }
        for (final TransactionDirection direction : values()) {
            if (slug.equals(direction.slug)) {
                return direction;
            }
        }
        return null;
    }
}
