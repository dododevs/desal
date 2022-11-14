package revolver.desal.ui.activity.edit;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.shifts.ShiftData;
import revolver.desal.api.services.shifts.ShiftEditDataRequest;
import revolver.desal.api.services.shifts.ShiftPumpData;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.util.ui.Snacks;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class ShiftPumpDataEditActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_shift_pump_data_edit);

        if ((mShift = getIntent().getParcelableExtra("shift")) == null) {
            Toast.makeText(this, R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        mKey = getIntent().getStringExtra("key");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final TextView titleView = findViewById(R.id.activity_shift_pump_data_edit_title);
        titleView.setText(getIntent().getStringExtra("label"));

        mInitialView = findViewById(R.id.activity_shift_pump_data_edit_initial);
        mEndView = findViewById(R.id.activity_shift_pump_data_edit_end);

        mInitialView.setText(String.format(Locale.UK, "%.2f", getInitialPumpValue()));
        mEndView.setText(String.format(Locale.UK, "%.2f", getEndPumpValue()));

        mLoadingView = findViewById(R.id.activity_shift_pump_data_edit_wheel);
        mConfirmView = findViewById(R.id.activity_shift_pump_data_edit_confirm);
        mConfirmView.setOnClickListener(v -> submitDataOrComplain());

        mSnackbarContainer = findViewById(R.id.activity_shift_pump_data_edit_snackbar);
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

        double difference = end - initial;
        if (difference <= 0 || difference > 6000.0) {
            mInitialView.setError(getString(R.string.dialog_pump_end_value_difference_error));
            mEndView.setError(getString(R.string.dialog_pump_end_value_difference_error));
            return;
        }

        final List<ShiftPumpData> initialPumpData = new ArrayList<>(mShift.getInitialData().getPumpsData());
        for (int i = 0; i < initialPumpData.size(); i++) {
            if (initialPumpData.get(i).getPid().equals(mKey)) {
                initialPumpData.set(i, new ShiftPumpData(mKey, initial));
            }
        }

        final List<ShiftPumpData> endPumpData = new ArrayList<>(mShift.getEndData().getPumpsData());
        for (int i = 0; i < endPumpData.size(); i++) {
            if (endPumpData.get(i).getPid().equals(mKey)) {
                endPumpData.set(i, new ShiftPumpData(mKey, end));
            }
        }

        final ShiftData initialData = new ShiftData(
                mShift.getInitialData().getGplClock(), mShift.getInitialData().getFund(), initialPumpData);
        final ShiftData endData = new ShiftData(
                mShift.getEndData().getGplClock(), mShift.getEndData().getFund(), endPumpData);
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

    private double getInitialPumpValue() {
        return checkNotNull(findInitialPumpDataByPid(mKey)).getValue();
    }

    private double getEndPumpValue() {
        return checkNotNull(findEndPumpDataByPid(mKey)).getValue();
    }

    private ShiftPumpData findInitialPumpDataByPid(String pid) {
        for (final ShiftPumpData data : mShift.getInitialData().getPumpsData()) {
            if (data.getPid().equals(pid)) {
                return data;
            }
        }
        return null;
    }

    private ShiftPumpData findEndPumpDataByPid(String pid) {
        for (final ShiftPumpData data : mShift.getEndData().getPumpsData()) {
            if (data.getPid().equals(pid)) {
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
                Toast.makeText(ShiftPumpDataEditActivity.this,
                        R.string.activity_shift_gpl_clock_edit_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        ShiftPumpDataEditActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(ShiftPumpDataEditActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
                setResult(RESULT_CANCELED);
            } else {
                Snacks.normal(mSnackbarContainer, Status
                        .getErrorDescription(ShiftPumpDataEditActivity.this, body.getStatus()));
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
