package revolver.desal.api.model.request;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.inventory.Item;

public class ItemCreationRequest {

    @SerializedName("item")
    private final Item item;

    public ItemCreationRequest(Item item) {
        this.item = item;
    }

}
