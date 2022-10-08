package revolver.desal.api;

import android.content.Context;

import revolver.desal.R;

public class Status {

    public static final int OK                        = 0x00;

    public static final int USERNAME_TOO_LONG         = 0x10;
    public static final int USERNAME_TOO_SHORT        = 0x11;
    public static final int USERNAME_BAD_CHARS        = 0x12;
    public static final int USERNAME_ALREADY_TAKEN    = 0x13;
    public static final int PASSWORD_TOO_LONG         = 0x14;
    public static final int PASSWORD_TOO_SHORT        = 0x15;
    public static final int PASSWORD_BAD_CHARS        = 0x16;
    public static final int PASSWORD_SAME_AS_OLD      = 0x17;

    public static final int WRONG_CREDENTIALS         = 0x20;

    public static final int VALIDATION_TOKEN_INVALID  = 0x30;
    public static final int VALIDATION_TOKEN_EXPIRED  = 0x31;
    public static final int SESSION_TOKEN_INVALID     = 0x32;
    public static final int WRONG_USER_TYPE           = 0x33;

    public static final int SID_INVALID               = 0x40;
    public static final int OID_INVALID               = 0x41;
    public static final int RID_INVALID               = 0x42;
    public static final int TID_INVALID               = 0x4E;
    public static final int SELL_NOT_ENOUGH_AVAILABLE = 0x43;
    public static final int SHIFT_ACTIVE_FOR_SID      = 0x44;
    public static final int SHIFT_ACTIVE_FOR_UID      = 0x45;
    public static final int SHIFT_NOT_ACTIVE          = 0x46;
    public static final int SHIFT_UID_NOT_MATCH       = 0x47;
    public static final int TRANSACTION_TYPE_INVALID  = 0x48;
    public static final int PAYMENT_METHOD_INVALID    = 0x49;
    public static final int END_SHIFT_GPLCLOCK_MISMATCH = 0x4A;
    public static final int END_SHIFT_PUMP_MISMATCH   = 0x4B;
    public static final int SHIFT_ALREADY_REVISED     = 0x4C;
    public static final int SHIFT_NOT_REVISED         = 0x4D;
    public static final int MAX_DIFF_EXCEEDED         = 0x4F;

    public static final int BAD_REQUEST               = 0x190;
    public static final int ENDPOINT_NOT_FOUND        = 0x194;

    public static String getErrorDescription(Context context, int error) {
        if (context == null) {
            return null;
        }
        switch (error) {
            case USERNAME_TOO_LONG:
                return context.getString(R.string.error_username_too_long);
            case USERNAME_TOO_SHORT:
                return context.getString(R.string.error_username_too_short);
            case USERNAME_BAD_CHARS:
                return context.getString(R.string.error_username_bad_chars);
            case USERNAME_ALREADY_TAKEN:
                return context.getString(R.string.error_username_taken);
            case PASSWORD_TOO_LONG:
                return context.getString(R.string.error_password_too_long);
            case PASSWORD_TOO_SHORT:
                return context.getString(R.string.error_password_too_short);
            case PASSWORD_BAD_CHARS:
                return context.getString(R.string.error_password_bad_chars);
            case PASSWORD_SAME_AS_OLD:
                return context.getString(R.string.error_new_password_same_as_old);
            case WRONG_CREDENTIALS:
                return context.getString(R.string.error_wrong_credentials);
            case VALIDATION_TOKEN_INVALID:
                return context.getString(R.string.error_validation_invalid);
            case VALIDATION_TOKEN_EXPIRED:
                return context.getString(R.string.error_validation_expired);
            case SESSION_TOKEN_INVALID:
                return context.getString(R.string.error_session_invalid);
            case WRONG_USER_TYPE:
                return context.getString(R.string.error_wrong_user_type);
            case SID_INVALID:
                return context.getString(R.string.error_sid_invalid);
            case OID_INVALID:
                return context.getString(R.string.error_oid_invalid);
            case RID_INVALID:
                return context.getString(R.string.error_rid_invalid);
            case TID_INVALID:
                return context.getString(R.string.error_tid_invalid);
            case SELL_NOT_ENOUGH_AVAILABLE:
                return context.getString(R.string.error_not_enough_items_available);
            case SHIFT_ACTIVE_FOR_SID:
                return context.getString(R.string.error_shift_active_sid);
            case SHIFT_ACTIVE_FOR_UID:
                return context.getString(R.string.error_shift_active_uid);
            case SHIFT_NOT_ACTIVE:
                return context.getString(R.string.error_shift_not_active);
            case SHIFT_UID_NOT_MATCH:
                return context.getString(R.string.error_shift_uid_not_match);
            case TRANSACTION_TYPE_INVALID:
                return context.getString(R.string.error_transaction_type_invalid);
            case PAYMENT_METHOD_INVALID:
                return context.getString(R.string.error_payment_method_invalid);
            case END_SHIFT_GPLCLOCK_MISMATCH:
                return context.getString(R.string.error_end_shift_gplclock_mismatch);
            case END_SHIFT_PUMP_MISMATCH:
                return context.getString(R.string.error_end_shift_pump_mismatch);
            case SHIFT_ALREADY_REVISED:
                return context.getString(R.string.error_shift_already_revised);
            case SHIFT_NOT_REVISED:
                return context.getString(R.string.error_shift_not_revised);
            case MAX_DIFF_EXCEEDED:
                return context.getString(R.string.error_max_diff_exceeded);
            case BAD_REQUEST:
                return context.getString(R.string.error_bad_request);
            case ENDPOINT_NOT_FOUND:
                return context.getString(R.string.error_endpoint_not_found);
            default:
                return context.getString(R.string.error_generic);
        }
    }

}
