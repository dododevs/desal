package revolver.desal.api.services.inventory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import revolver.desal.api.adapter.ShopItemUnitAdapter;

public class Item implements Parcelable {

    @SerializedName("oid")
    private String oid;

    @SerializedName("sid")
    private String sid;

    @SerializedName("name")
    private String name;

    @SerializedName("quantity")
    private int availableQuantity;

    @JsonAdapter(ShopItemUnitAdapter.class)
    @SerializedName("unit")
    private Unit unit;

    @SerializedName("price")
    private double price;

    @SerializedName("description")
    private String description;

    public Item() {
    }

    public Item(String oid, String sid, String name, int quantity, Unit unit, double price, String description) {
        this.oid = oid;
        this.sid = sid;
        this.name = name;
        this.availableQuantity = quantity;
        this.unit = unit;
        this.price = price;
        this.description = description;
    }

    public String getOid() {
        return oid;
    }

    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    private Item(Parcel src) {
        this.oid = src.readString();
        this.sid = src.readString();
        this.name = src.readString();
        this.availableQuantity = src.readInt();
        this.unit = Unit.fromString(src.readString());
        this.price = src.readDouble();
        this.description = src.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(oid);
        dest.writeString(sid);
        dest.writeString(name);
        dest.writeInt(availableQuantity);
        dest.writeString(unit.getSlug());
        dest.writeDouble(price);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
