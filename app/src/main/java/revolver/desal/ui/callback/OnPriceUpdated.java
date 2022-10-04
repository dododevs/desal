package revolver.desal.ui.callback;

import revolver.desal.api.services.stations.GasPrice;

public interface OnPriceUpdated {
    void onPriceUpdated(GasPrice oldPrice, GasPrice newPrice);
}
