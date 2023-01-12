package it.stazionidesal.desal.ui.callback;

import it.stazionidesal.desal.api.services.stations.GasPrice;

public interface OnPriceUpdated {
    void onPriceUpdated(GasPrice oldPrice, GasPrice newPrice);
}
