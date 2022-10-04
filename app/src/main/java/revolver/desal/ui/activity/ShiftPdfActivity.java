package revolver.desal.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.PdfModelsResponse;
import revolver.desal.api.services.PdfModelsService;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.adapter.PdfModelsAdapter;
import revolver.desal.util.ui.Snacks;

public class ShiftPdfActivity extends AppCompatActivity {

    private PdfModelsAdapter mAdapter;

    private CoordinatorLayout mSnackbarContainer;
    private FrameLayout mLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_pdf);

        final GasStation station = getIntent().getParcelableExtra("station");
        if (station == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Shift shift = getIntent().getParcelableExtra("shift");
        if (shift == null) {
            Toast.makeText(this, R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new PdfModelsAdapter(new ArrayList<>()));

        mAdapter.setOnModelClickListener(model -> startActivity(new Intent(ShiftPdfActivity.this, ShiftPdfPrintActivity.class)
                .putExtra("station", station)
                .putExtra("shift", shift)
                .putExtra("model", model)));

        mLoadingView = findViewById(R.id.dim);
        mSnackbarContainer = findViewById(R.id.activity_shift_pdf);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshModels();
    }

    private void refreshModels() {
        startLoading();
        getModelsService().getModels().enqueue(new PdfModelsResponseCallback());
    }

    private void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    private PdfModelsService getModelsService() {
        return RestAPI.getModelsService();
    }

    private class PdfModelsResponseCallback implements Callback<PdfModelsResponse> {
        @Override
        public void onResponse(@NonNull Call<PdfModelsResponse> call, Response<PdfModelsResponse> response) {
            stopLoading();

            final PdfModelsResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                mAdapter.updateModels(body.getPdfModels());
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                            ShiftPdfActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(ShiftPdfActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        ShiftPdfActivity.this, body.getStatus()));
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<PdfModelsResponse> call, Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

}
