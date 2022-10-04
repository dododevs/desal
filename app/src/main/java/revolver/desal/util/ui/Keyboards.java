package revolver.desal.util.ui;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Keyboards {

    public static void hideOnWindowAttached(@Nullable View v) {
        if (v == null) {
            return;
        }
        getInputMethodManager(v).hideSoftInputFromWindow(
                v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showOnWindowAttached(@Nullable View v) {
        if (v == null) {
            return;
        }
        getInputMethodManager(v).toggleSoftInputFromWindow(
                v.getWindowToken(), InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private static InputMethodManager getInputMethodManager(View v) {
        return (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


}
