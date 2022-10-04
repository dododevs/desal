package revolver.desal.util.ui;

import android.util.DisplayMetrics;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class M {

    private static DisplayMetrics sDisplayMetrics;

    public static Float dp(float dp) {
        checkNotNull(sDisplayMetrics);
        return dp * sDisplayMetrics.density;
    }

    public static int screenWidth() {
        checkNotNull(sDisplayMetrics);
        return sDisplayMetrics.widthPixels;
    }

    public static int screenHeight() {
        checkNotNull(sDisplayMetrics);
        return sDisplayMetrics.heightPixels;
    }

    public static void setDisplayMetrics(DisplayMetrics metrics) {
        sDisplayMetrics = new DisplayMetrics();
        sDisplayMetrics.setTo(metrics);
    }

}
