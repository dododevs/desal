package it.stazionidesal.desal.api.model.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.stations.GasPrice;

public class PriceChangeRequest {

    @SerializedName("prices")
    private List<GasPrice> prices;

    public PriceChangeRequest(List<GasPrice> prices) {
        this.prices = prices;
    }

    public void setPrices(List<GasPrice> prices) {
        this.prices = prices;
    }
}
