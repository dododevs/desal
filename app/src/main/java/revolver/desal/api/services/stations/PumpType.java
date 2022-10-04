package revolver.desal.api.services.stations;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import revolver.desal.R;

public enum PumpType {

    SELF("self", R.string.self),
    PATP("patp", R.string.patp);

    private final String mSlugName;
    private final int mStringResource;

    PumpType(String slug, @StringRes int stringResource) {
        mSlugName = slug;
        mStringResource = stringResource;
    }

    public static PumpType fromString(String slug) {
        if (slug == null) {
            return null;
        }
        for (final PumpType type : values()) {
            if (slug.equals(type.mSlugName)) {
                return type;
            }
        }
        return null;
    }

    public int getStringResource() {
        return mStringResource;
    }

    @NonNull
    @Override
    public String toString() {
        return mSlugName;
    }

}
