package it.stazionidesal.desal.util.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class IconUtils {

    public static Drawable drawableWithResolvedColor(final Context context,
                                                     @DrawableRes int drawableRes,
                                                     @ColorRes int color) {
        if (context == null) {
            return null;
        }

        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        if (drawable != null) {
            drawable = drawable.mutate();
            if (color != 0) {
                drawable.setColorFilter(
                        ColorUtils.get(context, color), PorterDuff.Mode.SRC_ATOP);
            }
            return drawable;
        }

        return null;
    }

    public static Drawable drawable(final Context context, @DrawableRes int drawableRes) {
        return drawableWithResolvedColor(context, drawableRes, 0);
    }

}
