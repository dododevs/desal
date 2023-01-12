package it.stazionidesal.desal.ui.activity.employee;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.EndShiftResponse;
import it.stazionidesal.desal.api.model.response.NewShiftResponse;
import it.stazionidesal.desal.api.model.response.ShiftResponse;
import it.stazionidesal.desal.api.services.ShiftsService;
import it.stazionidesal.desal.api.services.shifts.GplClockData;
import it.stazionidesal.desal.api.services.shifts.Shift;
import it.stazionidesal.desal.api.services.shifts.ShiftData;
import it.stazionidesal.desal.ui.adapter.ShiftPumpsAdapter;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.Keyboards;
import it.stazionidesal.desal.util.ui.Snacks;
import it.stazionidesal.desal.util.ui.ViewUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.models.PdfModelCompiler;
import it.stazionidesal.desal.api.services.shifts.ShiftPumpData;
import it.stazionidesal.desal.api.services.stations.GasPump;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.MainActivity;

public class BeginOrEndShiftActivity extends AppCompatActivity {

    private List<GasPump> mPumps;
    private final ArrayMap<GasPump, Double> mPumpsValues = new ArrayMap<>();
    private ShiftPumpsAdapter mAdapter;
    private GasStation mStation;

    private LinearLayout mGplClocksContainer;
    private TextInputEditText[] mGplClockFields;
    private EditText mFundField;

    private ProgressBar mLoadingWheel;
    private TextView mConfirmView;
    private FrameLayout mLoadingView;
    private boolean isLoadingLastShift = false;
    private View mSnackbarContainer;
    
    private String mActivityMode;
    private Shift mShift;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_or_end_shift);

        mActivityMode = getIntent().getStringExtra("mode");
        mShift = getIntent().getParcelableExtra("shift");

        mStation = DeSal.getPersistentSelectedStation(this);
        if (mStation == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPumps = mStation.getPumps();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mGplClocksContainer = findViewById(R.id.activity_begin_or_end_shift_gpl_clock_container);
        mFundField = findViewById(R.id.activity_begin_or_end_shift_fund);
        mSnackbarContainer = findViewById(R.id.activity_begin_or_end_shift_snackbar);

        mAdapter = new ShiftPumpsAdapter(mPumps);
        mAdapter.setOnPumpCardClickListener((card, pump) -> getValueInputDialogForPump(card, pump).show());

        final RecyclerView pumpsList = findViewById(R.id.activity_begin_or_end_shift_pumps);
        pumpsList.setLayoutManager(
                new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false));
        pumpsList.setAdapter(mAdapter);

        mConfirmView = findViewById(R.id.activity_begin_or_end_shift_confirm);
        mConfirmView.setOnClickListener(v -> {
            Keyboards.hideOnWindowAttached(v);
            submitDataOrComplain();
        });

        mGplClockFields = new TextInputEditText[mStation.getGplClockCount()];
        populateGplClocksContainer(mStation.getGplClockCount());

        mLoadingWheel = findViewById(R.id.activity_begin_or_end_shift_wheel);
        mLoadingView = findViewById(R.id.dim);

        final TextView valuesHeader = findViewById(R.id.activity_begin_or_end_shift_values_header);
        if ("begin".equals(mActivityMode)) {
            valuesHeader.setText(R.string.activity_begin_or_end_shift_initial_data);
            toolbar.setTitle(R.string.activity_begin_new_shift_title);
            toolbar.inflateMenu(R.menu.toolbar_activity_begin_or_end_shift);
            toolbar.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.toolbar_activity_begin_or_end_shift_clear) {
                    clearAllFields();
                } else if (menuItem.getItemId() == R.id.toolbar_activity_begin_or_end_shift_restore) {
                    if (!isLoadingLastShift) {
                        getValuesFromLastShift();
                    }
                }
                return true;
            });
            getValuesFromLastShift();
        } else if ("end".equals(mActivityMode)) {
            valuesHeader.setText(R.string.activity_begin_or_end_shift_end_data);
            toolbar.setTitle(R.string.activity_end_shift_title);
        }
    }

    private void getValuesFromLastShift() {
        startLastShiftLoading();
        getShiftsService().getMostRecentEndedShift(mStation.getSid())
                .enqueue(new MostRecentShiftResponseCallback());
    }

    private void fillFromLastShift(final Shift shift) {
        final ShiftData endData = shift.getEndData();
        mFundField.setText(String.format(Locale.UK, "%.2f", endData.getFund()));
        for (int i = 0; i < mGplClockFields.length; i++) {
            if (i < endData.getGplClock().size()) {
                mGplClockFields[i].setText(String.format(Locale.UK, "%.2f",
                        endData.getGplClock().get(i).getValue()));
            }
        }
        for (final ShiftPumpData pumpData : endData.getPumpsData()) {
            GasPump gasPump = null;
            for (final GasPump pump : mPumps) {
                if (pump.getPid().equals(pumpData.getPid())) {
                    gasPump = pump;
                    break;
                }
            }
            if (gasPump == null) {
                continue;
            }
            mPumpsValues.put(gasPump, pumpData.getValue());
        }
        mAdapter.setInitialValues(mPumpsValues);
    }

    private void clearAllFields() {
        mFundField.setText(null);
        for (final TextInputEditText field : mGplClockFields) {
            field.setText(null);
        }
        mPumpsValues.clear();
        mAdapter.setInitialValues(mPumpsValues);
    }

    private void submitDataOrComplain() {
        final double fund;
        final List<GplClockData> gplClocks = new ArrayList<>();
        for (int i = 0; i < mStation.getGplClockCount(); i++) {
            try {
                double gplClock = Double.parseDouble(
                        Objects.requireNonNull(mGplClockFields[i]
                            .getText()).toString().replace(',', '.'));
                gplClocks.add(new GplClockData(String.valueOf(i + 1), gplClock));
            } catch (NumberFormatException e) {
                mGplClockFields[i].setError(getString(R.string.error_field_value_invalid));
                Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        }
        try {
            fund = Double.parseDouble(mFundField.getText().toString().replace(',', '.'));
        } catch (NumberFormatException e) {
            mFundField.setError(getString(R.string.error_field_value_invalid));
            Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            return;
        }

        final List<ShiftPumpData> data = new ArrayList<>();
        for (final GasPump pump : mPumps) {
            if (mPumpsValues.containsKey(pump)) {
                final Double value = mPumpsValues.get(pump);
                if (value != null) {
                    data.add(new ShiftPumpData(pump.getPid(), value));
                }
            } else {
                Snacks.normal(mSnackbarContainer, R.string.error_not_all_pumps_have_values);
                return;
            }
        }

        final ShiftData shiftData = new ShiftData(gplClocks, fund, data);
        if ("begin".equals(mActivityMode)) {
            startLoading();
            getShiftsService().beginShift(mStation.getSid(), shiftData)
                    .enqueue(new NewShiftResponseCallback());
        } else if ("end".equals(mActivityMode) && mShift != null) {
            startLoading();
            getShiftsService().endShift(mShift.getRid(), shiftData)
                    .enqueue(new EndShiftResponseCallback());
        }
    }

    private void startLoading() {
        mConfirmView.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    private void startLastShiftLoading() {
        isLoadingLastShift = true;
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLastShiftLoading() {
        isLoadingLastShift = false;
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    private void populateGplClocksContainer(int howMany) {
        for (int i = 0; i < howMany; i++) {
            final View v = View.inflate(this, R.layout.field_begin_new_shift_gpl_clock, null);
            mGplClockFields[i] = v.findViewById(R.id.activity_begin_new_shift_gpl_clock);
            mGplClockFields[i].setHint(getString(R.string.activity_begin_new_shift_gpl_clock, i + 1));
            mGplClocksContainer.addView(v, ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().horizontalMargin(16.f).get());
        }
    }

    private AlertDialog getValueInputDialogForPump(final View card, final GasPump pump) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View v = View.inflate(this, R.layout.dialog_pump_value_input, null);

        final TextView valueView = card.findViewById(R.id.item_pump_value);

        TextView fuelView = v.findViewById(R.id.dialog_pump_value_input_fuel);
        ImageView iconView = v.findViewById(R.id.dialog_pump_value_input_icon);
        TextView nameView = v.findViewById(R.id.dialog_pump_value_input_name);
        TextView initialValueView = v.findViewById(R.id.dialog_pump_value_input_initial);

        final EditText valueField = v.findViewById(R.id.dialog_pump_value_input_field);
        if ("begin".equals(mActivityMode)) {
            valueField.setHint(R.string.dialog_pump_initial_value_input_field_hint);
        } else if ("end".equals(mActivityMode)) {
            valueField.setHint(R.string.dialog_pump_end_value_input_field_hint);
            final ShiftPumpData pumpData = findPumpData(pump);
            if (pumpData != null) {
                initialValueView.setVisibility(View.VISIBLE);
                initialValueView.setText(getString(R.string.dialog_pump_value_input_initial, pumpData.getValue()));
            }
        }

        fuelView.setText(pump.getAvailableFuel().getStringResource());
        iconView.setColorFilter(ColorUtils.get(this, pump.getAvailableFuel().getColorResource()));

        nameView.setText(String.format(Locale.ITALIAN, "%s %s",
                getString(pump.getType().getStringResource()), pump.getDisplay()));

        if (mPumpsValues.containsKey(pump)) {
            final Double value = mPumpsValues.get(pump);
            if (value != null) {
                valueField.setText(String.format(Locale.ITALIAN, "%.2f", value));
            }
        }

        builder.setView(v);
        builder.setPositiveButton(R.string.OK, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.CANCEL, (dialog, which) -> dialog.dismiss());

        final AlertDialog alertDialog = builder.create();
        final View.OnClickListener onPositiveButtonPressedListener = v1 -> {
            double value = Double.parseDouble(valueField.getText().toString().replace(',', '.'));
            value = roundToHundredths(value);
            if ("end".equals(mActivityMode)) {
                final ShiftPumpData pumpData = findPumpData(pump);
                if (pumpData != null) {
                    final double difference = value - pumpData.getValue();
                    if (difference < 0 || difference > 6000.0) {
                        valueField.setError(getString(R.string.dialog_pump_end_value_difference_error));
                        return;
                    }
                }
            }

            mPumpsValues.put(pump, value);
            valueView.setText(String.format(Locale.ITALIAN, "%.2f", value));

            Keyboards.hideOnWindowAttached(valueField);
            alertDialog.dismiss();
        };
        alertDialog.setOnShowListener(dialog -> {
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(onPositiveButtonPressedListener);
        });

        return alertDialog;
    }

    private ShiftPumpData findPumpData(GasPump pump) {
        if (mShift == null || mShift.getInitialData() == null) {
            return null;
        }
        for (final ShiftPumpData pumpData : mShift.getInitialData().getPumpsData()) {
            if (pump.getPid().equals(pumpData.getPid())) {
                return pumpData;
            }
        }
        return null;
    }

    public static double roundToHundredths(double value) {
        return new BigDecimal(value).round(
                new MathContext((int) Math.ceil(Math.log10(value)) + 2)
        ).doubleValue();
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private class MostRecentShiftResponseCallback implements Callback<ShiftResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftResponse> call, Response<ShiftResponse> response) {
            stopLastShiftLoading();

            final ShiftResponse body = response.body();
            if (body == null) {
                Snacks.normal(mSnackbarContainer,
                        getString(R.string.activity_begin_or_end_shift_last_shift_error_generic),
                        getString(R.string.activity_begin_or_end_shift_last_shift_retry),
                        v -> getValuesFromLastShift());
                return;
            }
            if (body.isSuccessful()) {
                fillFromLastShift(body.getShift());
                Toast.makeText(BeginOrEndShiftActivity.this,
                        getString(R.string.activity_begin_or_end_shift_last_shift_success,
                                PdfModelCompiler.sDateFieldFormatter.format(
                                        new Date(body.getShift().getEnd() * 1000))),
                        Toast.LENGTH_LONG).show();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                            BeginOrEndShiftActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(BeginOrEndShiftActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        BeginOrEndShiftActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<ShiftResponse> call, @NonNull Throwable t) {
            stopLastShiftLoading();
            Snacks.normal(mSnackbarContainer,
                    getString(R.string.activity_begin_or_end_shift_last_shift_error_generic),
                    getString(R.string.activity_begin_or_end_shift_last_shift_retry),
                    v -> getValuesFromLastShift());
        }
    }

    private class NewShiftResponseCallback implements Callback<NewShiftResponse> {
        @Override
        public void onResponse(@NonNull Call<NewShiftResponse> call, Response<NewShiftResponse> response) {
            stopLoading();

            final NewShiftResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(BeginOrEndShiftActivity.this,
                        R.string.toast_begin_new_shift_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, new Intent().putExtra("rid", body.getRid()));
                onBackPressed();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status
                                .getErrorDescription(BeginOrEndShiftActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(BeginOrEndShiftActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(BeginOrEndShiftActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<NewShiftResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private class EndShiftResponseCallback implements Callback<EndShiftResponse> {
        @Override
        public void onResponse(@NonNull Call<EndShiftResponse> call, Response<EndShiftResponse> response) {
            stopLoading();

            final EndShiftResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(BeginOrEndShiftActivity.this,
                        R.string.toast_end_shift_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status
                                .getErrorDescription(BeginOrEndShiftActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(BeginOrEndShiftActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(BeginOrEndShiftActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<EndShiftResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
