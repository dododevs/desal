package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import revolver.desal.api.services.stations.GasStation;

public class StationsResponse extends BaseResponse {

    @SerializedName("stations")
    private List<GasStation> stations;

    public StationsResponse() {
    }

    public List<GasStation> getStations() {
        return stations;
    }
}
