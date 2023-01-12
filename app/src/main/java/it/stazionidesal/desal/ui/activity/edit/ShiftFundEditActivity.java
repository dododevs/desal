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

import java.util.Locale;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.services.ShiftsService;
import it.stazionidesal.desal.api.services.shifts.Shift;
import it.stazionidesal.desal.api.services.shifts.ShiftData;
import it.stazionidesal.desal.util.ui.Snacks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.shifts.ShiftEditDataRequest;
import it.stazionidesal.desal.ui.activity.MainActivity;

public class ShiftFundEditActivity extends AppCompatActivity {

    private Shift mShift;

    private EditText mInitialView;
    private EditText mEndView;

    private TextView mConfirmView;
    private ProgressBar mLoadingView;

    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_fund_edit);

        if ((mShift = getIntent().getParcelableExtra("shift")) == null) {
            Toast.makeText(this, R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mInitialView = findViewById(R.id.activity_shift_fund_edit_initial);
        mEndView = findViewById(R.id.activity_shift_fund_edit_end);

        mInitialView.setText(String.format(Locale.UK, "%.2f", mShift.getInitialData().getFund()));
        mEndView.setText(String.format(Locale.UK, "%.2f", mShift.getEndData().getFund()));

        mLoadingView = findViewById(R.id.activity_shift_fund_edit_wheel);
        mConfirmView = findViewById(R.id.activity_shift_fund_edit_confirm);
        mConfirmView.setOnClickListener(v -> submitDataOrComplain());

        mSnackbarContainer = findViewById(R.id.activity_shift_fund_edit_snackbar);
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

        final ShiftData initialData = new ShiftData(
                mShift.getInitialData().getGplClock(), initial, mShift.getInitialData().getPumpsData());
        final ShiftData endData = new ShiftData(
                mShift.getEndData().getGplClock(), end, mShift.getEndData().getPumpsData());
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
                Toast.makeText(ShiftFundEditActivity.this,
                        R.string.activity_shift_gpl_clock_edit_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        ShiftFundEditActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(ShiftFundEditActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
                setResult(RESULT_CANCELED);
            } else {
                Snacks.normal(mSnackbarContainer, Status
                        .getErrorDescription(ShiftFundEditActivity.this, body.getStatus()));
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
