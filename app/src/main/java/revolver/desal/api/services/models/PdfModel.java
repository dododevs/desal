package revolver.desal.api.services.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import revolver.desal.api.adapter.Base64PdfModelAdapter;

public class PdfModel implements Parcelable {

    @JsonAdapter(Base64PdfModelAdapter.class)
    @SerializedName("page1")
    private String page1;

    @JsonAdapter(Base64PdfModelAdapter.class)
    @SerializedName("page2")
    private String page2;

    @SerializedName("attributes")
    private final Attributes attributes;

    PdfModel(String page1, String page2, Attributes attributes) {
        this.page1 = page1;
        this.page2 = page2;
        this.attributes = attributes;
    }

    void setPage1(String page1) {
        this.page1 = page1;
    }

    void setPage2(String page2) {
        this.page2 = page2;
    }

    public String getPage1() {
        return page1;
    }

    public String getPage2() {
        return page2;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    private PdfModel(Parcel src) {
        this.page1 = src.readString();
        this.page2 = src.readString();
        this.attributes = src.readParcelable(Attributes.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(page1);
        dest.writeString(page2);
        dest.writeParcelable(attributes, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PdfModel> CREATOR = new Creator<PdfModel>() {
        @Override
        public PdfModel createFromParcel(Parcel source) {
            return new PdfModel(source);
        }

        @Override
        public PdfModel[] newArray(int size) {
            return new PdfModel[size];
        }
    };
}
