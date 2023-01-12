package it.stazionidesal.desal.util.ui;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

public class ColorUtils {

    private static final ArgbEvaluator sArgbEvaluator = new ArgbEvaluator();

    public static String colorToString(int color) {
        return "#" + Integer.toHexString((color >> 16) & 0xFF) +
                Integer.toHexString((color >> 8) & 0xFF) +
                Integer.toHexString(color & 0xFF);
    }

    public static @ColorInt
    int interpolateBetweenColors(@ColorInt int startColor,
                                 @ColorInt int endColor,
                                 float progress) {
        return (int) sArgbEvaluator.evaluate(progress, startColor, endColor);
    }

    public static Drawable dyeDrawable(final Drawable drawable, @ColorInt int color) {
        if (drawable != null)
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public static Drawable dyeDrawableWithResolvedColor(Context context,
                                                        final Drawable drawable,
                                                        @ColorRes int color) {
        return dyeDrawable(drawable, get(context, color));
    }

    public static @ColorInt int get(Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

}
