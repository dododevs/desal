package revolver.desal.api.services.inventory;

import androidx.annotation.StringRes;

import revolver.desal.R;

public enum Unit {

    PIECES("pcs", R.string.unit_pieces),
    LITERS("L", R.string.unit_liters);

    private final String slug;
    private final int nameStringRes;

    Unit(String slug, @StringRes int nameStringRes) {
        this.slug = slug;
        this.nameStringRes = nameStringRes;
    }

    public String getSlug() {
        return slug;
    }

    public int getNameResource() {
        return nameStringRes;
    }

    public static Unit fromString(String slug) {
        for (Unit u : values()) {
            if (u.slug.equals(slug)) {
                return u;
            }
        }
        return null;
    }
}
