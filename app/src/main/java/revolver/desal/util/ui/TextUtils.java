package revolver.desal.util.ui;

public final class TextUtils {

    public static String capitalizeWords(final String s) {
        final StringBuilder sb = new StringBuilder();
        final char[] chars = s.toCharArray();
        boolean shouldCapitalize = true;
        for (char c : chars) {
            if (shouldCapitalize && Character.isLetterOrDigit(c)) {
                c = Character.toUpperCase(c);
                shouldCapitalize = false;
            } else {
                c = Character.toLowerCase(c);
            }
            if (Character.isWhitespace(c)) {
                shouldCapitalize = true;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String capitalizeFirst(final String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
