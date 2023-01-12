package it.stazionidesal.desal.api.services.stations;

import androidx.annotation.NonNull;

import it.stazionidesal.desal.R;

public enum Fuel {

    GPL("gpl", R.string.item_price_gpl, R.color.gpl),
    DIESEL("diesel", R.string.item_price_diesel, R.color.diesel),
    UNLEADED("unleaded", R.string.item_price_unleaded, R.color.unleaded);

    private final String mSlugName;
    private final int mStringResource;
    private final int mColorResource;

    Fuel(String slug, int stringResource, int colorResource) {
        mSlugName = slug;
        mStringResource = stringResource;
        mColorResource = colorResource;
    }

    public static Fuel fromString(String slug) {
        if (slug == null) {
            return null;
        }
        for (final Fuel fuel : values()) {
            if (slug.equals(fuel.mSlugName)) {
                return fuel;
            }
        }
        return null;
    }

    public int getStringResource() {
        return mStringResource;
    }

    public int getColorResource() {
        return mColorResource;
    }

    @NonNull
    @Override
    public String toString() {
        return mSlugName;
    }
}
