package revolver.desal.ui.activity;

import android.view.View;

import revolver.desal.api.services.stations.GasStation;

public interface DeSalFragmentHostActivity {
    void startLoading();
    void stopLoading();
    View getSnackbarContainer();
    GasStation getStation();
}
