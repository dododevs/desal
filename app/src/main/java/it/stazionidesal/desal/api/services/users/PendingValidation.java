package it.stazionidesal.desal.api.services.users;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PendingValidation implements Parcelable {

    @SerializedName("token")
    private final String token;

    @SerializedName("expiration")
    private final long expiration;

    public String getToken() {
        return token;
    }

    public long getExpiration() {
        return expiration;
    }

    private PendingValidation(Parcel src) {
        this.token = src.readString();
        this.expiration = src.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
        dest.writeLong(expiration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PendingValidation> CREATOR = new Creator<PendingValidation>() {
        @Override
        public PendingValidation createFromParcel(Parcel source) {
            return new PendingValidation(source);
        }

        @Override
        public PendingValidation[] newArray(int size) {
            return new PendingValidation[size];
        }
    };
}
