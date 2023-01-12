package it.stazionidesal.desal.util.logic;

public class Conditions {

    public static <T> T checkNotNull(T obj, String msg) {
        if (obj != null)
            return obj;
        throw new IllegalArgumentException(msg);
    }

    public static <T> T checkNotNull(T obj) {
        return checkNotNull(obj, "obj == null");
    }

    public static void checkTrue(boolean what, String msg) {
        if (!what)
            throw new IllegalStateException(msg);
    }

    public static void checkTrue(boolean what) {
        checkTrue(what, "unmet condition: should be true");
    }

    public static void checkFalse(boolean what, String msg) {
        checkTrue(!what, msg);
    }

    public static void checkFalse(boolean what) {
        checkTrue(!what, "unmet condition: should be false");
    }

}
