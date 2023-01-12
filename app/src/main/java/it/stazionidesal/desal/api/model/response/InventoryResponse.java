package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.inventory.Item;

public class InventoryResponse extends BaseResponse {

    @SerializedName("items")
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

}
