package revolver.desal.util.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;

import revolver.desal.R;

public class ViewUtils {

    public static void colorizeMenu(Menu menu, int color) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).getIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawable getSelectableBorderlessBackgroundDrawable(Context context) {
        final TypedValue selectableBackground = new TypedValue();
        context.getTheme().resolveAttribute(
                R.attr.selectableItemBackgroundBorderless, selectableBackground, true);

        return ContextCompat.getDrawable(context, selectableBackground.resourceId);
    }

    public static Drawable getSelectableBackgroundDrawable(Context context) {
        final TypedValue selectableBackground = new TypedValue();
        context.getTheme().resolveAttribute(
                R.attr.selectableItemBackground, selectableBackground, true);

        return ContextCompat.getDrawable(context, selectableBackground.resourceId);
    }

    public static int getStatusBarHeight() {
        return Math.round(M.dp(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 25 : 24));
    }

    public static void setNoLimitsLayout(Activity activity, boolean whether) {
        if (whether) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public static void setTranslucentStatusBar(Activity activity, boolean translucent) {
        if (translucent) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        activity.getWindow().setStatusBarColor(color);
    }

    @TargetApi(23)
    public static void setLightStatusBar(Activity activity, boolean light) {
        final View decorView = activity.getWindow().getDecorView();

        int uiFlags = decorView.getSystemUiVisibility();
        if (light) {
            uiFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            uiFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        decorView.setSystemUiVisibility(uiFlags);
    }

    public static void setImageFromAssets(ImageView imageView, String path) {
        final Context context = imageView.getContext();
        final Bitmap b;
        try {
            b = BitmapFactory.decodeStream(context.getAssets().open(path));
            imageView.setImageBitmap(b);
        } catch (IOException e) {
            Log.w("setImageFromAssets", e);
        }
    }

    public static boolean isFragmentDead(Fragment fragment) {
        return !fragment.isVisible();
    }

    public static class LayoutParamsBuilder<T extends ViewGroup.LayoutParams> {
        private int topMargin, bottomMargin, startMargin, endMargin;
        private boolean matchParentW, wrapContentW;
        private boolean matchParentH, wrapContentH;
        private int width, height;
        private float weight;
        private int gravity;
        private final Class<T> clazz;

        LayoutParamsBuilder(Class<T> layoutParamsClass) {
            clazz = layoutParamsClass;
        }

        public LayoutParamsBuilder<T> topMargin(float dpMargin) {
            topMargin = M.dp(dpMargin).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> bottomMargin(float dpMargin) {
            bottomMargin = M.dp(dpMargin).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> startMargin(float dpMargin) {
            startMargin = M.dp(dpMargin).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> endMargin(float dpMargin) {
            endMargin = M.dp(dpMargin).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> horizontalMargin(float dpMargin) {
            startMargin = endMargin = M.dp(dpMargin).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> verticalMargin(float dpMargin) {
            topMargin = bottomMargin = M.dp(dpMargin).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> gravity(int gravityInt) {
            gravity = gravityInt;
            return this;
        }

        public LayoutParamsBuilder<T> width(float dpWidth) {
            width = M.dp(dpWidth).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> height(float dpHeight) {
            height = M.dp(dpHeight).intValue();
            return this;
        }

        public LayoutParamsBuilder<T> weight(float Weight) {
            weight = Weight;
            return this;
        }

        public LayoutParamsBuilder<T> matchParentInWidth() {
            matchParentW = true;
            return this;
        }

        public LayoutParamsBuilder<T> matchParentInHeight() {
            matchParentH = true;
            return this;
        }

        public LayoutParamsBuilder<T> wrapContentInWidth() {
            wrapContentW = true;
            return this;
        }

        public LayoutParamsBuilder<T> wrapContentInHeight() {
            wrapContentH = true;
            return this;
        }

        public T get() {
            final T instance;
            try {
                instance = clazz.getConstructor(int.class, int.class).newInstance(
                        matchParentW ? ViewGroup.LayoutParams.MATCH_PARENT :
                                wrapContentW ? ViewGroup.LayoutParams.WRAP_CONTENT : width,
                        matchParentH ? ViewGroup.LayoutParams.MATCH_PARENT :
                                wrapContentH ? ViewGroup.LayoutParams.WRAP_CONTENT : height
                );
            } catch (Exception e) {
                Log.w("LayoutParamsBuilder#get", e);
                return null;
            }

            if (instance instanceof ViewGroup.MarginLayoutParams) {
                final ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) instance;
                params.setMargins(startMargin, topMargin, endMargin, bottomMargin);
            }
            if (instance instanceof LinearLayout.LayoutParams) {
                final LinearLayout.LayoutParams params =
                        (LinearLayout.LayoutParams) instance;
                params.weight = weight;
            }
            try {
                clazz.getField("gravity").set(instance, gravity);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // no gravity
            }

            return instance;
        }
    }

    public static <T extends ViewGroup.LayoutParams> LayoutParamsBuilder<T> newLayoutParams(Class<T> className) {
        return new LayoutParamsBuilder<>(className);
    }

    public static LayoutParamsBuilder<ViewGroup.LayoutParams> newLayoutParams() {
        return newLayoutParams(ViewGroup.LayoutParams.class);
    }

}
