package it.stazionidesal.desal.api.services.users;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import it.stazionidesal.desal.R;

public enum UserType {

    EMPLOYEE("D", R.string.user_type_employee),
    OWNER("T", R.string.user_type_owner);

    private final String slug;
    private final @StringRes int stringResource;

    UserType(String slug, int stringResource) {
        this.slug = slug;
        this.stringResource = stringResource;
    }

    public @StringRes int getStringResource() {
        return stringResource;
    }

    public static UserType fromString(String slug) {
        if (slug == null) {
            return null;
        }
        for (final UserType userType : values()) {
            if (slug.equals(userType.slug)) {
                return userType;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return slug;
    }
}
