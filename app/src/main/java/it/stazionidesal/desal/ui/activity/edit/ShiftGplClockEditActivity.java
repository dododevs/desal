package it.stazionidesal.desal.ui.activity.edit;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.services.ShiftsService;
import it.stazionidesal.desal.api.services.shifts.GplClockData;
import it.stazionidesal.desal.api.services.shifts.Shift;
import it.stazionidesal.desal.api.services.shifts.ShiftData;
import it.stazionidesal.desal.util.logic.Conditions;
import it.stazionidesal.desal.util.ui.Snacks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.shifts.ShiftEditDataRequest;
import it.stazionidesal.desal.ui.activity.MainActivity;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class ShiftGplClockEditActivity extends AppCompatActivity {

    private Shift mShift;
    private String mKey;

    private EditText mInitialView;
    private EditText mEndView;

    private TextView mConfirmView;
    private ProgressBar mLoadingView;

    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_gpl_clock_edit);

        if ((mShift = getIntent().getParcelableExtra("shift")) == null) {
            Toast.makeText(this, R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        mKey = getIntent().getStringExtra("key");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final TextView titleView = findViewById(R.id.activity_shift_gpl_clock_edit_title);
        titleView.setText(getIntent().getStringExtra("label"));

        mInitialView = findViewById(R.id.activity_shift_gpl_clock_edit_initial);
        mEndView = findViewById(R.id.activity_shift_gpl_clock_edit_end);

        mInitialView.setText(String.format(Locale.UK, "%.2f", getInitialGplClockValue()));
        mEndView.setText(String.format(Locale.UK, "%.2f", getEndGplClockValue()));

        mLoadingView = findViewById(R.id.activity_shift_gpl_clock_edit_wheel);
        mConfirmView = findViewById(R.id.activity_shift_gpl_clock_edit_confirm);
        mConfirmView.setOnClickListener(v -> submitDataOrComplain());

        mSnackbarContainer = findViewById(R.id.activity_shift_gpl_clock_edit_snackbar);
    }

    private void submitDataOrComplain() {
        double initial, end;
        try {
            initial = Double.parseDouble(mInitialView.getText().toString());
        } catch (NumberFormatException e) {
            mInitialView.setError(getString(R.string.error_field_value_invalid));
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            return;
        }
        try {
            end = Double.parseDouble(mEndView.getText().toString());
        } catch (NumberFormatException e) {
            mEndView.setError(getString(R.string.error_field_value_invalid));
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            return;
        }

        final List<GplClockData> initialGplClockData = new ArrayList<>(mShift.getInitialData().getGplClock());
        for (int i = 0; i < initialGplClockData.size(); i++) {
            if (initialGplClockData.get(i).getRef().equals(mKey)) {
                initialGplClockData.set(i, new GplClockData(mKey, initial));
            }
        }

        final List<GplClockData> endGplClockData = new ArrayList<>(mShift.getEndData().getGplClock());
        for (int i = 0; i < endGplClockData.size(); i++) {
            if (endGplClockData.get(i).getRef().equals(mKey)) {
                endGplClockData.set(i, new GplClockData(mKey, end));
            }
        }

        final ShiftData initialData = new ShiftData(initialGplClockData,
                mShift.getInitialData().getFund(), mShift.getInitialData().getPumpsData());
        final ShiftData endData = new ShiftData(endGplClockData,
                mShift.getEndData().getFund(), mShift.getEndData().getPumpsData());
        startLoading();
        getShiftsService().editShift(mShift.getRid(), new ShiftEditDataRequest(initialData, endData))
                .enqueue(new ShiftEditDataResponseCallback());
    }

    private void startLoading() {
        mConfirmView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingView.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    private double getInitialGplClockValue() {
        return Conditions.checkNotNull(findInitialGplClockDataByRef(mKey)).getValue();
    }

    private double getEndGplClockValue() {
        return Conditions.checkNotNull(findEndGplClockDataByRef(mKey)).getValue();
    }

    private GplClockData findInitialGplClockDataByRef(String ref) {
        for (final GplClockData data : mShift.getInitialData().getGplClock()) {
            if (data.getRef().equals(ref)) {
                return data;
            }
        }
        return null;
    }

    private GplClockData findEndGplClockDataByRef(String ref) {
        for (final GplClockData data : mShift.getEndData().getGplClock()) {
            if (data.getRef().equals(ref)) {
                return data;
            }
        }
        return null;
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private class ShiftEditDataResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                setResult(RESULT_CANCELED);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(ShiftGplClockEditActivity.this,
                        R.string.activity_shift_gpl_clock_edit_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        ShiftGplClockEditActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(ShiftGplClockEditActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
                setResult(RESULT_CANCELED);
            } else {
                Snacks.normal(mSnackbarContainer, Status
                        .getErrorDescription(ShiftGplClockEditActivity.this, body.getStatus()));
                setResult(RESULT_CANCELED);
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
            setResult(RESULT_CANCELED);
        }
    }
}
