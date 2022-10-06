package revolver.desal.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import revolver.desal.R;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.fragment.owner.station.OwnerStationShiftsFragment;

public class ShiftsArchiveActivity extends AppCompatActivity implements DeSalFragmentHostActivity {

    private GasStation mStation;
    private View mLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts_archive);

        mStation = getIntent().getParcelableExtra("station");
        if (mStation == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mLoadingView = findViewById(R.id.dim);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new OwnerStationShiftsFragment())
                .commit();
    }

    @Override
    public void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoading() {
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    @Override
    public View getSnackbarContainer() {
        return findViewById(R.id.activity_shifts_archive);
    }

    @Override
    public GasStation getStation() {
        return mStation;
    }
}
