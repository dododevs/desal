package revolver.desal.util.platform;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unchecked")
public class Prefs {

    public static <T> T get(Context ctx, String key) {
        return (T) PreferenceManager.getDefaultSharedPreferences(ctx).getAll().get(key);
    }

    public static <T> T get(Context ctx, String key, T defaultValue) {
        if (exists(ctx, key)) {
            return get(ctx, key);
        }
        return defaultValue;
    }

    public static <T> void put(Context ctx, String key, T value) {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof List) {
            editor.putStringSet(key, new HashSet<>((List<String>) value));
        }
        editor.apply();
    }

    public static boolean exists(Context ctx, String key) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).contains(key);
    }

    public static void remove(Context ctx, String key) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().remove(key).apply();
    }
}
