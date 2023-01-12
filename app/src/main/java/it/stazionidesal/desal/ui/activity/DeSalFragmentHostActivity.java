package it.stazionidesal.desal.ui.activity;

import android.view.View;

import it.stazionidesal.desal.api.services.stations.GasStation;

public interface DeSalFragmentHostActivity {
    void startLoading();
    void stopLoading();
    View getSnackbarContainer();
    GasStation getStation();
}
