package revolver.desal.api.model.request;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.inventory.Item;

public class ItemUpdateRequest {

    @SerializedName("item")
    private final Item item;

    public ItemUpdateRequest(Item item) {
        this.item = item;
    }

}
