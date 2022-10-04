package revolver.desal.util.platform;

import android.os.Build;

import revolver.desal.BuildConfig;

public class UserAgent {

    private static final String sUserAgent;

    static {
        sUserAgent = "DeSal/" + getAppVersion() +
                " (Linux; Android " + getAndroidVersion() + ")";
    }

    private static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    private static String getAndroidVersion() {
        return Build.VERSION.CODENAME;
    }

    public static String get() {
        return sUserAgent;
    }

}
