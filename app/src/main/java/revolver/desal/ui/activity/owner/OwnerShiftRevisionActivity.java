package revolver.desal.ui.activity.owner;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.request.ShiftRevisionRequest;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.shifts.revision.RevisionData;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.adapter.FortechTotalsInputAdapter;
import revolver.desal.ui.adapter.ShiftsArchiveAdapter;
import revolver.desal.util.ui.Keyboards;
import revolver.desal.util.ui.Snacks;

public class OwnerShiftRevisionActivity extends AppCompatActivity {

    private static final SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("EEE dd MMMM", Locale.ITALIAN);

    private GasStation mStation;
    private Shift mShift;

    private FortechTotalsInputAdapter mAdapter;

    private TextInputEditText mOptCashView;
    private TextInputEditText mOptCreditCardsView;
    private TextInputEditText mOptRefundsView;
    private TextInputEditText mOptUnsuppliedView;
    private TextInputEditText mPrivateCardsView;

    private ProgressBar mLoadingView;
    private TextView mConfirmView;
    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_shift_revision);

        mStation = getIntent().getParcelableExtra("station");
        if (mStation == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mShift = getIntent().getParcelableExtra("shift");
        if (mShift == null) {
            Toast.makeText(this, R.string.activity_owner_shift_revision_no_shift_given,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final Toolbar toolbar1 = findViewById(R.id.toolbar1);
        toolbar1.setTitle(String.format(Locale.ITALIAN, "%s  •  %s  →  %s",
                sDateFormatter.format(new Date(mShift.getEnd() * 1000)),
                ShiftsArchiveAdapter.sHourFormatter.format(new Date(mShift.getStart() * 1000)),
                ShiftsArchiveAdapter.sHourFormatter.format(new Date(mShift.getEnd() * 1000)))
        );

        final View employeeDataContainer = findViewById(R.id.activity_owner_shift_revision_employee_data);
        employeeDataContainer.setOnClickListener(v -> startActivity(new Intent(OwnerShiftRevisionActivity.this, OwnerShiftRevisionEmployeeDataActivity.class)
                .putExtra("station", mStation)
                .putExtra("shift", mShift)));

        final RecyclerView fortechContainer =
                findViewById(R.id.activity_owner_shift_revision_fortech_totals_container);
        fortechContainer.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        fortechContainer.setAdapter(mAdapter = new FortechTotalsInputAdapter(mStation.getPrices()));

        mOptCashView = findViewById(R.id.activity_owner_shift_revision_opt_cash);
        mOptCreditCardsView = findViewById(R.id.activity_owner_shift_revision_opt_credit_card);
        mOptRefundsView = findViewById(R.id.activity_owner_shift_revision_opt_refunds);
        mOptUnsuppliedView = findViewById(R.id.activity_owner_shift_revision_opt_unsupplied);
        mPrivateCardsView = findViewById(R.id.activity_owner_shift_revision_cards);
        mPrivateCardsView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Keyboards.hideOnWindowAttached(v);
            }
            return true;
        });

        mLoadingView = findViewById(R.id.activity_owner_shift_revision_wheel);
        mConfirmView = findViewById(R.id.activity_owner_shift_revision_confirm);
        mConfirmView.setOnClickListener(v -> submitDataOrComplain());

        mSnackbarContainer = findViewById(R.id.activity_owner_shift_revision_snackbar);
    }

    private void submitDataOrComplain() {
        if (!mAdapter.isComplete()) {
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_totals_are_filled);
            return;
        }

        final double optCashTotal, optCreditCardTotal,
                optRefunds, optUnsupplied, privateCardsTotal;
        if (mOptCashView.getText() != null && mOptCashView.getText().length() > 0) {
            try {
                optCashTotal = Double.parseDouble(mOptCashView.getText().toString());
            } catch (NumberFormatException e) {
                mOptCashView.setError(getString(R.string.error_field_value_invalid));
                Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        } else {
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            mOptCashView.setError(getString(R.string.error_field_value_invalid));
            return;
        }

        if (mOptCreditCardsView.getText() != null && mOptCreditCardsView.getText().length() > 0) {
            try {
                optCreditCardTotal = Double.parseDouble(mOptCreditCardsView.getText().toString());
            } catch (NumberFormatException e) {
                mOptCreditCardsView.setError(getString(R.string.error_field_value_invalid));
                Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        } else {
            mOptCreditCardsView.setError(getString(R.string.error_field_value_invalid));
            return;
        }

        if (mOptRefundsView.getText() != null && mOptRefundsView.getText().length() > 0) {
            try {
                optRefunds = Double.parseDouble(mOptRefundsView.getText().toString());
            } catch (NumberFormatException e) {
                mOptRefundsView.setError(getString(R.string.error_field_value_invalid));
                Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        } else {
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            mOptRefundsView.setError(getString(R.string.error_field_value_invalid));
            return;
        }

        if (mOptUnsuppliedView.getText() != null && mOptUnsuppliedView.getText().length() > 0) {
            try {
                optUnsupplied = Double.parseDouble(mOptUnsuppliedView.getText().toString());
            } catch (NumberFormatException e) {
                mOptUnsuppliedView.setError(getString(R.string.error_field_value_invalid));
                Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        } else {
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            mOptUnsuppliedView.setError(getString(R.string.error_field_value_invalid));
            return;
        }

        if (mPrivateCardsView.getText() != null && mPrivateCardsView.getText().length() > 0) {
            try {
                privateCardsTotal = Double.parseDouble(mPrivateCardsView.getText().toString());
            } catch (NumberFormatException e) {
                mPrivateCardsView.setError(getString(R.string.error_field_value_invalid));
                Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        } else {
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            mPrivateCardsView.setError(getString(R.string.error_field_value_invalid));
            return;
        }

        final RevisionData revisionData = new RevisionData(mAdapter.getTotals(),
                new RevisionData.Incomes(privateCardsTotal,
                        new RevisionData.Incomes.Opt(optCashTotal, optCreditCardTotal,
                                optRefunds, optUnsupplied)));
        startLoading();
        getShiftsService().reviseShift(mShift.getRid(), new ShiftRevisionRequest(revisionData))
                .enqueue(new ShiftRevisionResponseCallback());
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

    private class ShiftRevisionResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(OwnerShiftRevisionActivity.this,
                        R.string.activity_owner_shift_revision_success, Toast.LENGTH_SHORT).show();
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                            OwnerShiftRevisionActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(OwnerShiftRevisionActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        OwnerShiftRevisionActivity.this, body.getStatus()));
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<BaseResponse> call, Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

}