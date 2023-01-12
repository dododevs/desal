package it.stazionidesal.desal.util.ui;

import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.View;

public class Snacks {

    public static void normal(View view, String text, String actionText, View.OnClickListener actionClickListener) {
        if (view != null) {
            if (actionText != null && actionClickListener != null) {
                Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                        .setAction(actionText, actionClickListener).show();
            } else {
                Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
            }
        } else {
            Log.w("Snacks", "Cancelled Snackbar on null view. (" + text + ")");
        }
    }

    public static void normal(View view, String text) {
        normal(view, text, null, null);
    }

    public static void normal(View view, @StringRes int text) {
        if (view != null) {
            final String str = view.getResources().getString(text);
            normal(view, str);
        } else {
            normal(null, null);
        }
    }

    public static void shorter(View view, String text, String actionText, View.OnClickListener actionClickListener) {
        if (view != null) {
            if (actionText != null && actionClickListener != null) {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
                        .setAction(actionText, actionClickListener).show();
            } else {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Log.w("Snacks", "Cancelled Snackbar on null view. (" + text + ")");
        }
    }

    public static void shorter(View view, String text) {
        shorter(view, text, null, null);
    }

    public static void shorter(View view, @StringRes int text) {
        if (view != null) {
            final String str = view.getResources().getString(text);
            shorter(view, str);
        } else {
            shorter(null, null);
        }
    }

    public static void longer(View view, String text, String actionText, View.OnClickListener actionClickListener) {
        if (view != null) {
            if (actionText != null && actionClickListener != null) {
                Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
                        .setAction(actionText, actionClickListener).show();
            } else {
                Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE).show();
            }
        } else {
            Log.w("Snacks", "Cancelled Snackbar on null view. (" + text + ")");
        }
    }

    public static void longer(View view, String text) {
        longer(view, text, null, null);
    }

    public static void longer(View view, @StringRes int text) {
        if (view != null) {
            final String str = view.getResources().getString(text);
            longer(view, str);
        } else {
            longer(null, null);
        }
    }

}
