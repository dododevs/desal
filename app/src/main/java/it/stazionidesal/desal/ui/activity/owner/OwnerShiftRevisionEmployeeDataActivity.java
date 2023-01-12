package it.stazionidesal.desal.ui.activity.owner;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.ShiftResponse;
import it.stazionidesal.desal.api.services.ShiftsService;
import it.stazionidesal.desal.api.services.shifts.Shift;
import it.stazionidesal.desal.ui.adapter.EndedShiftDifferenceDataAdapter2;
import it.stazionidesal.desal.ui.fragment.RefreshableContent;
import it.stazionidesal.desal.util.ui.Snacks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.MainActivity;

public class OwnerShiftRevisionEmployeeDataActivity
        extends AppCompatActivity implements RefreshableContent {

    private GasStation mStation;
    private Shift mShift;

    private RecyclerView mEmployeeDataView;

    private View mLoadingView;
    private View mFailedView;
    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_shift_revision_employee_data);

        if ((mStation = getIntent().getParcelableExtra("station")) == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if ((mShift = getIntent().getParcelableExtra("shift")) == null) {
            Toast.makeText(this, R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mEmployeeDataView = findViewById(R.id.activity_owner_shift_revision_employee_data_container);
        mFailedView = findViewById(R.id.activity_owner_shift_revision_employee_data_failed);

        View mRetryView = findViewById(R.id.activity_owner_shift_revision_employee_data_retry);
        mRetryView.setOnClickListener(v -> doRefresh());

        mLoadingView = findViewById(R.id.dim);
        mSnackbarContainer = findViewById(R.id.activity_owner_shift_revision_employee_data);

        refreshShift();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EndedShiftDifferenceDataAdapter2.SHIFT_DATA_EDIT_REQUEST && resultCode == RESULT_OK) {
            doRefresh();
        }
    }

    private void refreshShift() {
        startLoading();
        getShiftsService().getShift(mShift.getRid()).enqueue(new ShiftReloadResponseCallback());
    }

    @Override
    public void doRefresh() {
        refreshShift();
    }

    private void generateShiftSummaryView() {
        mEmployeeDataView.setVisibility(View.VISIBLE);
        mEmployeeDataView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        mEmployeeDataView.setAdapter(new EndedShiftDifferenceDataAdapter2(this, mStation, mShift));
        if (mEmployeeDataView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) mEmployeeDataView.getItemAnimator())
                    .setSupportsChangeAnimations(false);
        }
    }

    private void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        mFailedView.setVisibility(View.GONE);
    }

    private void stopLoading() {
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    private void onShiftReloadFailed() {
        mEmployeeDataView.setVisibility(View.GONE);
        mFailedView.setVisibility(View.VISIBLE);
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private class ShiftReloadResponseCallback implements Callback<ShiftResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftResponse> call, Response<ShiftResponse> response) {
            stopLoading();

            final ShiftResponse body = response.body();
            if (body == null) {
                onShiftReloadFailed();
                return;
            }
            if (body.isSuccessful()) {
                mShift = body.getShift();
                generateShiftSummaryView();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(OwnerShiftRevisionEmployeeDataActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(OwnerShiftRevisionEmployeeDataActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
                onShiftReloadFailed();
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        OwnerShiftRevisionEmployeeDataActivity.this, body.getStatus()));
                onShiftReloadFailed();
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<ShiftResponse> call, Throwable t) {
            stopLoading();
            onShiftReloadFailed();
        }
    }
}
