package it.stazionidesal.desal.ui.activity.employee;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.LoadingActivity;
import it.stazionidesal.desal.ui.fragment.RefreshableContent;
import it.stazionidesal.desal.ui.fragment.StationSelectionFragment;
import it.stazionidesal.desal.ui.fragment.UserDetailFragment;
import it.stazionidesal.desal.ui.fragment.employee.EmployeeInventoryFragment;
import it.stazionidesal.desal.ui.fragment.employee.EmployeeStationFragment;
import it.stazionidesal.desal.ui.fragment.employee.EmployeeTransactionsFragment;
import it.stazionidesal.desal.util.ui.TextUtils;
import it.stazionidesal.desal.view.DeSalToolbarBlob;

public class EmployeeMainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private DeSalToolbarBlob mBlob;

    private List<GasStation> mStations;
    private GasStation mSelectedStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);

        mStations = getIntent().getParcelableArrayListExtra("stations");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> new UserDetailFragment().show(getSupportFragmentManager(), "userDetailDialog"));

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom);
        bottomNavigationView.setSelectedItemId(R.id.bottom_employee_station);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        mBlob = findViewById(R.id.blob);
        mBlob.setOnClickListener(v -> onUserRequestedNewStation());

        /* could be empty if coming from login fragment (not going through LoadingActivity) */
        if (mStations.size() > 0) {
            if (DeSal.isPersistentSelectedStationAvailable(this)) {
                final GasStation selectedStation = DeSal.getPersistentSelectedStation(this);
                if (selectedStation != null) {
                    onAvailableSelectedStation(selectedStation);
                } else {
                    onUnavailableSelectedStation();
                }
            } else {
                onUnavailableSelectedStation();
            }
        } else {
            onEmptyStationsList();
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new EmployeeStationFragment())
                .commit();
    }

    public void setupBlobWithStation(GasStation station) {
        final String name = station.getName();
        mBlob.setText(TextUtils.capitalizeWords(name));
        mBlob.setVisibility(View.VISIBLE);
    }

    private void onAvailableSelectedStation(GasStation station) {
        for (GasStation s : mStations) {
            if (s.getSid().equals(station.getSid())) {
                DeSal.persistSelectedStation(this, s);
                setupBlobWithStation(s);
                setSelectedStation(s);
                return;
            }
        }
        onUnavailableSelectedStation();
    }

    private void onUserRequestedNewStation() {
        StationSelectionFragment.withStations(mStations)
                .show(getSupportFragmentManager(), "stationSelectionDialog");
    }

    private void onUnavailableSelectedStation() {
        setSelectedStation(mStations.get(0));
        StationSelectionFragment.withStations(mStations)
                .show(getSupportFragmentManager(), "stationSelectionDialog");
    }

    private void onEmptyStationsList() {
        startActivity(new Intent(this, LoadingActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void requestRefresh() {
        final List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            final Fragment fragment = fragments.get(i);
            if (fragment instanceof RefreshableContent) {
                ((RefreshableContent) fragment).doRefresh();
                break;
            }
        }
    }

    public GasStation getSelectedStation() {
        return mSelectedStation;
    }

    public void setSelectedStation(GasStation station) {
        mSelectedStation = station;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.bottom_employee_station:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new EmployeeStationFragment(), "station")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;
            case R.id.bottom_employee_inventory:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new EmployeeInventoryFragment(), "inventory")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;
            case R.id.bottom_employee_transactions:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new EmployeeTransactionsFragment(), "transactions")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;
        }
        return true;
    }
}
