package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import revolver.desal.api.services.stations.GasPrice;

public class GasPricesResponse extends BaseResponse {

    @SerializedName("prices")
    private List<GasPrice> prices;

    public List<GasPrice> getPrices() {
        return prices;
    }
}
