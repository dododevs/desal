package revolver.desal.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.ShiftResponse;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.shifts.revision.ActualIncomes;
import revolver.desal.api.services.shifts.revision.EstimatedIncomes;
import revolver.desal.api.services.shifts.revision.FortechTotal;
import revolver.desal.api.services.shifts.revision.Revision;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.adapter.EndedShiftDifferenceDataAdapter2;
import revolver.desal.ui.fragment.RefreshableContent;
import revolver.desal.util.ui.ColorUtils;
import revolver.desal.util.ui.Snacks;
import revolver.desal.util.ui.ViewUtils;

public class ShiftSummaryActivity extends AppCompatActivity implements RefreshableContent {

    private GasStation mStation;
    private Shift mShift;

    private LinearLayout mSumContainer;
    private RecyclerView mEmployeeDataView;
    private View mOwnerDataContainer;
    private View mNotRevisedView;

    private View mLoadingView;
    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_summary);

        if ((mStation = getIntent().getParcelableExtra("station")) == null) {
            Toast.makeText(this, R.string.error_no_station_selected,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mShift = getIntent().getParcelableExtra("shift");
        if (mShift == null) {
            Toast.makeText(this, R.string.activity_shift_summary_no_shift_given,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.toolbar_activity_shift_summary);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.toolbar_activity_shift_summary_pdf) {
                if (mShift.getRevision() != null) {
                    startActivity(new Intent(ShiftSummaryActivity.this, ShiftPdfActivity.class)
                            .putExtra("station", mStation)
                            .putExtra("shift", mShift));
                } else {
                    Toast.makeText(ShiftSummaryActivity.this,
                            R.string.activity_shift_summary_no_revision, Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        mEmployeeDataView = findViewById(R.id.activity_shift_summary_employee_data_container);
        mSumContainer = findViewById(R.id.activity_shift_summary_sum_container);
        mOwnerDataContainer = findViewById(R.id.activity_shift_summary_owner_data_container);
        mNotRevisedView = findViewById(R.id.activity_shift_summary_not_revised);

        mLoadingView = findViewById(R.id.dim);
        mSnackbarContainer = findViewById(R.id.activity_shift_summary_snackbar);

        generateShiftSummaryView();
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
        mEmployeeDataView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        mEmployeeDataView.setAdapter(new EndedShiftDifferenceDataAdapter2(this, mStation, mShift));
        if (mEmployeeDataView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) mEmployeeDataView.getItemAnimator())
                    .setSupportsChangeAnimations(false);
        }
        if (mShift.getRevision() == null) {
            mNotRevisedView.setVisibility(View.VISIBLE);
            mOwnerDataContainer.setVisibility(View.GONE);
        } else {
            mNotRevisedView.setVisibility(View.GONE);
            mOwnerDataContainer.setVisibility(View.VISIBLE);
            generateSumView();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EndedShiftDifferenceDataAdapter2.SHIFT_DATA_EDIT_REQUEST && resultCode == RESULT_OK) {
            doRefresh();
        }
    }

    private void generateSumView() {
        final Revision revision = mShift.getRevision();
        final LinearLayout.LayoutParams params = ViewUtils.newLayoutParams(LinearLayout.LayoutParams.class)
                .matchParentInWidth().wrapContentInHeight().horizontalMargin(8.f).verticalMargin(3.6f).get();

        final ActualIncomes actualIncomes = revision.getActualIncomes();
        mSumContainer.addView(buildSumRow(actualIncomes.getEndFund(),
                R.string.activity_shift_summary_end_fund, R.drawable.ic_counter, "  "), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getOptCashTotal(),
                R.string.activity_shift_summary_opt_cash, R.drawable.ic_local_atm, "+"), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getOptCreditCardsTotal(),
                R.string.activity_shift_summary_opt_credit_cards, R.drawable.ic_credit_card, "+"), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getOptRefundsTotal(),
                R.string.activity_shift_summary_opt_refunds, R.drawable.ic_refund, "+"), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getDepositTotal(),
                R.string.activity_shift_summary_deposit, R.drawable.ic_deposit, "+"), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getCreditCardsTotal(),
                R.string.activity_shift_summary_credit_cards, R.drawable.ic_credit_card, "+"), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getCouponTotal(),
                R.string.activity_shift_summary_coupons, R.drawable.ic_coupon, "+"), params);
        mSumContainer.addView(buildSumRow(actualIncomes.getCardsTotal(),
                R.string.activity_shift_summary_private_cards, R.drawable.ic_coupon, "+"), params);

        final EstimatedIncomes estimatedIncomes = revision.getEstimatedIncomes();
        mSumContainer.addView(buildSumRow(estimatedIncomes.getInitialFund(),
                R.string.activity_shift_summary_initial_fund, R.drawable.ic_counter, "-"), params);
        mSumContainer.addView(buildSumRow(estimatedIncomes.getFortechTotal().getTotalProfit(),
                R.string.activity_shift_summary_fortech_total,
                    R.drawable.ic_local_gas_station, "-"), params);
        mSumContainer.addView(buildSumRow(estimatedIncomes.getGplTotal(),
                R.string.activity_shift_summary_gpl, R.drawable.ic_local_gas_station, "-"), params);
        mSumContainer.addView(buildSumRow(estimatedIncomes.getAccessoriesTotal(),
                R.string.activity_shift_summary_accessories, R.drawable.ic_accessories, "-"), params);
        mSumContainer.addView(buildSumRow(estimatedIncomes.getOilTotal(),
                R.string.activity_shift_summary_oil, R.drawable.ic_oil, "-"), params);
        mSumContainer.addView(buildSumRow(estimatedIncomes.getWhatnotTotal(),
                R.string.activity_shift_summary_whatnot, R.drawable.ic_whatnot, "-"), params);
        mSumContainer.addView(buildSumRow(estimatedIncomes.getOptUnsupplied(),
                R.string.activity_shift_summary_unsupplied, R.drawable.ic_unsupplied, "-"), params);

        final double estimated = estimatedIncomes.getGrandTotal();
        final double actual = actualIncomes.getGrandTotal();
        final double difference = actual - estimated;

        mSumContainer.addView(buildResultDivider(), ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                .matchParentInWidth().height(1.f).startMargin(24.f).endMargin(8.f).verticalMargin(16.f).get());
        mSumContainer.addView(buildDifferenceRow(difference), params);
    }

    private View buildSumRow(double amount, @StringRes int stringRes, @DrawableRes int iconRes, String sign) {
        final View rootView = View.inflate(this, R.layout.row_shift_summary_sum, null);
        ImageView iconView = rootView.findViewById(R.id.row_shift_summary_sum_icon);
        TextView valueView = rootView.findViewById(R.id.row_shift_summary_sum_value);
        TextView labelView = rootView.findViewById(R.id.row_shift_summary_sum_label);
        TextView signView = rootView.findViewById(R.id.row_shift_summary_sum_sign);

        iconView.setImageResource(iconRes);
        labelView.setText(stringRes);
        valueView.setText(String.format(Locale.ITALIAN, "%.2f €", amount));
        signView.setText(sign);

        return rootView;
    }

    private View buildSumRow(double amount, CharSequence charSequence, @DrawableRes int iconRes, String sign) {
        final View rootView = View.inflate(this, R.layout.row_shift_summary_sum, null);
        ImageView iconView = rootView.findViewById(R.id.row_shift_summary_sum_icon);
        TextView valueView = rootView.findViewById(R.id.row_shift_summary_sum_value);
        TextView labelView = rootView.findViewById(R.id.row_shift_summary_sum_label);
        TextView signView = rootView.findViewById(R.id.row_shift_summary_sum_sign);

        iconView.setImageResource(iconRes);
        labelView.setText(charSequence);
        valueView.setText(String.format(Locale.ITALIAN, "%.2f €", amount));
        signView.setText(sign);

        return rootView;
    }

    private View buildDifferenceRow(double amount) {
        final View rootView = buildSumRow(amount,
                getString(R.string.activity_shift_summary_difference), 0, "=");
        TextView valueView = rootView.findViewById(R.id.row_shift_summary_sum_value);
        TextView labelView = rootView.findViewById(R.id.row_shift_summary_sum_label);
        valueView.setTextSize(24.f);
        labelView.setTextSize(16.8f);
        return rootView;
    }

    private View buildResultDivider() {
        final View divider = new View(this);
        divider.setBackgroundColor(ColorUtils.get(this, R.color.almostWhite));
        return divider;
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
                Snacks.normal(mSnackbarContainer,
                        getString(R.string.activity_shift_summary_reload_error_generic),
                        getString(R.string.activity_shift_summary_reload_retry), v -> doRefresh());
                return;
            }
            if (body.isSuccessful()) {
                mShift = body.getShift();
                generateShiftSummaryView();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer,
                        getString(R.string.activity_shift_summary_reload_error,
                                Status.getErrorDescription(ShiftSummaryActivity.this, body.getStatus())),
                        getString(R.string.action_login), v -> startActivity(new Intent(ShiftSummaryActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        getString(R.string.activity_shift_summary_reload_error,
                                Status.getErrorDescription(ShiftSummaryActivity.this, body.getStatus())));
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<ShiftResponse> call, Throwable t) {
            stopLoading();
            Snacks.normal(mSnackbarContainer,
                    getString(R.string.activity_shift_summary_reload_error_generic),
                    getString(R.string.activity_shift_summary_reload_retry), v -> doRefresh());
        }
    }
}
