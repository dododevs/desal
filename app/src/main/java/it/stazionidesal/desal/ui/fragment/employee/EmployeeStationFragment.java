package it.stazionidesal.desal.ui.fragment.employee;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.cardview.widget.CardView;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.request.PriceChangeRequest;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.model.response.GasPricesResponse;
import it.stazionidesal.desal.api.model.response.ShiftResponse;
import it.stazionidesal.desal.api.services.ShiftsService;
import it.stazionidesal.desal.ui.callback.OnPricesRefreshRequested;
import it.stazionidesal.desal.ui.fragment.RefreshableContent;
import it.stazionidesal.desal.util.logic.Conditions;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.IconUtils;
import it.stazionidesal.desal.util.ui.M;
import it.stazionidesal.desal.util.ui.Snacks;
import it.stazionidesal.desal.util.ui.ViewUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.StationsService;
import it.stazionidesal.desal.api.services.shifts.Shift;
import it.stazionidesal.desal.api.services.stations.Fuel;
import it.stazionidesal.desal.api.services.stations.GasPrice;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.api.services.stations.PumpType;
import it.stazionidesal.desal.ui.activity.AddPricesActivity;
import it.stazionidesal.desal.ui.activity.ShiftsArchiveActivity;
import it.stazionidesal.desal.ui.activity.employee.BeginOrEndShiftActivity;
import it.stazionidesal.desal.ui.activity.employee.EmployeeMainActivity;
import it.stazionidesal.desal.ui.activity.MainActivity;
import it.stazionidesal.desal.ui.activity.employee.ShiftActivity;
import it.stazionidesal.desal.ui.fragment.EditPriceDialog;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class EmployeeStationFragment extends Fragment
        implements OnPricesRefreshRequested, RefreshableContent {

    public static final int ADD_PRICES_REQUEST_ID = "prices!".hashCode() & 0xffff;

    private List<CardView> mPriceCards = new ArrayList<>();

    private Shift mShift;
    private boolean mIsShiftActive = false;

    private LinearLayout mNoShiftsLayout;
    private LinearLayout mShiftLayout;

    private LinearLayout mPricesContainer;
    private LinearLayout mNoPricesLayout;

    private int mPendingTasks = 0;
    private FrameLayout mLoadingView;
    private final Object mLock = new Object();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_employee_station, container, false);

        mPricesContainer = v.findViewById(R.id.fragment_employee_station_prices_container);
        ensureSelectedStation();

        final FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(v13 -> {
            if (mIsShiftActive) {
                startActivity(new Intent(getContext(),
                        ShiftActivity.class).putExtra("shift", mShift));
            } else {
                startActivity(new Intent(getContext(), BeginOrEndShiftActivity.class)
                        .putExtra("mode", "begin"));
            }
        });

        mShiftLayout = v.findViewById(R.id.fragment_employee_station_shift);
        mNoShiftsLayout = v.findViewById(R.id.fragment_employee_station_no_shifts);
        mNoPricesLayout = v.findViewById(R.id.fragment_employee_station_no_prices_container);
        mLoadingView = v.findViewById(R.id.dim);

        final TextView editPricesButton = v.findViewById(R.id.fragment_employee_station_edit_prices);
        final Drawable editPricesIcon = Conditions.checkNotNull(IconUtils
                .drawableWithResolvedColor(getContext(), R.drawable.ic_mode_edit, R.color.colorPrimaryDark));
        editPricesIcon.setBounds(0, 0, M.dp(18.f).intValue(), M.dp(18.f).intValue());
        editPricesButton.setCompoundDrawablesWithIntrinsicBounds(editPricesIcon, null, null, null);
        editPricesButton.setOnClickListener(v12 -> startActivityForResult(new Intent(getContext(), AddPricesActivity.class)
                .putExtra("station", ensureSelectedStation()), ADD_PRICES_REQUEST_ID));

        final TextView shiftsArchiveButton = v.findViewById(R.id.fragment_employee_station_shifts_archive);
        final Drawable shiftsArchiveIcon = Conditions.checkNotNull(IconUtils
                .drawableWithResolvedColor(getContext(), R.drawable.ic_restore, R.color.colorPrimaryDark));
        shiftsArchiveIcon.setBounds(0, 0, M.dp(24).intValue(), M.dp(24).intValue());
        shiftsArchiveButton.setCompoundDrawables(shiftsArchiveIcon, null, null, null);
        shiftsArchiveButton.setOnClickListener(v1 -> startActivity(new Intent(getContext(), ShiftsArchiveActivity.class)
                .putExtra("station", ensureSelectedStation())));

        updatePriceCards(ensureSelectedStation().getPrices());

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_PRICES_REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
            final List<GasPrice> prices = data.getParcelableArrayListExtra("prices");
            getStationsService().updatePrices(ensureSelectedStation().getSid(),
                    new PriceChangeRequest(prices)).enqueue(new PriceChangeResponseCallback());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshShiftsAndPrices();
    }

    public void refreshShiftsAndPrices() {
        mLoadingView.setVisibility(View.VISIBLE);
        mPendingTasks = 2;
        getStationsService().getPricesForStation(ensureSelectedStation().getSid())
                .enqueue(new GasPricesResponseCallback());
        getShiftsService().getActiveShiftForStation(ensureSelectedStation().getSid())
                .enqueue(new ShiftResponseCallback());
    }

    @Override
    public void refreshPrices() {
        refreshShiftsAndPrices();
    }

    @Override
    public void doRefresh() {
        refreshShiftsAndPrices();
    }

    private void updateShiftSummary(Shift shift) {
        if (shift == null) {
            mShiftLayout.setVisibility(View.GONE);
            mNoShiftsLayout.setVisibility(View.VISIBLE);
        } else {
            mNoShiftsLayout.setVisibility(View.GONE);
            mShiftLayout.setVisibility(View.VISIBLE);

            final Chronometer timeView = mShiftLayout
                    .findViewById(R.id.fragment_employee_station_shift_time);
            timeView.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - shift.getStart() * 1000));
            timeView.start();
        }
    }

    private void updatePriceCards(List<GasPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            mNoPricesLayout.setVisibility(View.VISIBLE);
            mPricesContainer.setVisibility(View.GONE);
            return;
        } else {
            mNoPricesLayout.setVisibility(View.GONE);
            mPricesContainer.setVisibility(View.VISIBLE);
        }
        for (View priceView : mPriceCards) {
            mPricesContainer.removeView(priceView);
        }
        mPriceCards = generateViewsForPrices(prices);
        for (View priceView : mPriceCards) {
            mPricesContainer.addView(priceView, 0);
        }
    }

    private List<CardView> generateViewsForPrices(List<GasPrice> prices) {
        ArrayMap<Fuel, List<GasPrice>> pricesByFuel = new ArrayMap<>();
        for (final GasPrice price : prices) {
            Fuel fuel = price.getFuel();
            if (!pricesByFuel.containsKey(fuel)) {
                pricesByFuel.put(fuel, new ArrayList<>());
            }
            Conditions.checkNotNull(pricesByFuel.get(price.getFuel())).add(price);
        }

        List<CardView> views = new ArrayList<>();
        for (final Fuel fuel : pricesByFuel.keySet()) {
            CardView v = (CardView) View.inflate(getContext(), R.layout.item_price, null);
            List<GasPrice> pricesForFuel = Conditions.checkNotNull(pricesByFuel.get(fuel));

            LinearLayout leftLayout = v.findViewById(R.id.card_price_layout_left);
            final TextView leftFuelView = v.findViewById(R.id.card_price_fuel_left);
            TextView leftTypeView = v.findViewById(R.id.card_price_type_left);
            final TextView leftValueView = v.findViewById(R.id.card_price_value_left);
            TextView leftUnitView = v.findViewById(R.id.card_price_unit_left);

            LinearLayout rightLayout = v.findViewById(R.id.card_price_layout_right);
            final TextView rightFuelView = v.findViewById(R.id.card_price_fuel_right);
            TextView rightTypeView = v.findViewById(R.id.card_price_type_right);
            final TextView rightValueView = v.findViewById(R.id.card_price_value_right);
            TextView rightUnitView = v.findViewById(R.id.card_price_unit_right);

            v.setCardBackgroundColor(ColorUtils.get(getContext(), fuel.getColorResource()));

            for (final GasPrice price : pricesForFuel) {
                if (price.getType() == PumpType.SELF) {
                    leftLayout.setOnClickListener(v12 -> getPricesPopupMenu(leftFuelView, new PriceViewMenuCallback(
                            EmployeeStationFragment.this, getFragmentManager(),
                                leftValueView, ensureSelectedStation(), price)).show());
                    if (fuel.getStringResource() != 0) {
                        leftFuelView.setText(fuel.getStringResource());
                    } else {
                        leftFuelView.setText(fuel.toString());
                    }
                    leftTypeView.setText(R.string.self);
                    leftValueView.setText(String.format(Locale.ITALIAN, "%.3f", price.getPrice()));
                    leftUnitView.setText(R.string.item_price_unit);
                } else if (price.getType() == PumpType.PATP) {
                    rightLayout.setOnClickListener(v1 -> getPricesPopupMenu(rightFuelView, new PriceViewMenuCallback(
                            EmployeeStationFragment.this, getFragmentManager(),
                                rightFuelView, ensureSelectedStation(), price)).show());
                    if (fuel.getStringResource() != 0) {
                        rightFuelView.setText(fuel.getStringResource());
                    } else {
                        rightFuelView.setText(fuel.toString());
                    }
                    rightTypeView.setText(R.string.patp);
                    rightValueView.setText(String.format(Locale.ITALIAN, "%.3f", price.getPrice()));
                    rightUnitView.setText(R.string.item_price_unit);
                }
            }

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight()
                        .horizontalMargin(8.f).verticalMargin(8.f).get());
            views.add(v);
        }

        return views;
    }

    private PopupMenu getPricesPopupMenu(final View anchorView, final PopupMenu.OnMenuItemClickListener menuItemClickListener) {
        final PopupMenu menu = new PopupMenu(getContext(), anchorView, Gravity.CENTER);
        menu.getMenuInflater().inflate(R.menu.popup_prices, menu.getMenu());
        menu.setOnMenuItemClickListener(menuItemClickListener);
        return menu;
    }

    private void hideLoadingViewIfDone() {
        synchronized (mLock) {
            if (mPendingTasks == 0) {
                mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
                    mLoadingView.setVisibility(View.GONE);
                    mLoadingView.setAlpha(1.f);
                }).setStartDelay(100L).start();
            }
        }
    }

    private GasStation ensureSelectedStation() {
        final GasStation station = DeSal.getPersistentSelectedStation(getContext());
        return station != null ?
                station : ((EmployeeMainActivity) requireActivity()).getSelectedStation();
    }

    private StationsService getStationsService() {
        return RestAPI.getStationsService();
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private class ShiftResponseCallback implements Callback<ShiftResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftResponse> call, @NonNull Response<ShiftResponse> response) {
            if (ViewUtils.isFragmentDead(EmployeeStationFragment.this)) {
                return;
            }

            final ShiftResponse body = response.body();
            if (body == null) {
                Snacks.shorter(getView(), R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                mIsShiftActive = true;
                mShift = body.getShift();
                updateShiftSummary(body.getShift());
            } else if (body.getStatus() == Status.SHIFT_NOT_ACTIVE) {
                mIsShiftActive = false;
                mShift = null;
                updateShiftSummary(null);
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(getView(), Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(getView(), Status.getErrorDescription(getContext(), body.getStatus()));
            }

            mPendingTasks--;
            hideLoadingViewIfDone();
        }

        @Override
        public void onFailure(@NonNull Call<ShiftResponse> call, @NonNull Throwable t) {
            if (ViewUtils.isFragmentDead(EmployeeStationFragment.this)) {
                return;
            }

            Snacks.shorter(getView(), R.string.error_generic);
            mPendingTasks--;
            hideLoadingViewIfDone();
        }
    }

    private class GasPricesResponseCallback implements Callback<GasPricesResponse> {
        @Override
        public void onResponse(@NonNull Call<GasPricesResponse> call, @NonNull Response<GasPricesResponse> response) {
            if (ViewUtils.isFragmentDead(EmployeeStationFragment.this)) {
                return;
            }

            final GasPricesResponse body = response.body();
            if (body == null) {
                Snacks.shorter(getView(), R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                updatePriceCards(body.getPrices());
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(getView(), Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(getView(), Status.getErrorDescription(getContext(), body.getStatus()));
            }

            mPendingTasks--;
            hideLoadingViewIfDone();
        }

        @Override
        public void onFailure(@NonNull Call<GasPricesResponse> call, @NonNull Throwable t) {
            if (ViewUtils.isFragmentDead(EmployeeStationFragment.this)) {
                return;
            }

            Snacks.shorter(getView(), R.string.error_generic);
            mPendingTasks--;
            hideLoadingViewIfDone();
        }
    }

    private class PriceChangeResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(getView(), R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Snacks.shorter(getView(), R.string.fragment_employee_station_edit_prices_success);
                refreshShiftsAndPrices();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(getView(), Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(getView(),
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            Snacks.shorter(getView(), R.string.error_generic);
        }
    }

    public static class PriceViewMenuCallback implements PopupMenu.OnMenuItemClickListener {
        private final TextView mAnchorValueView;
        private final FragmentManager mFragmentManager;
        private final OnPricesRefreshRequested mHost;
        private final GasStation mStation;
        private final GasPrice mPrice;
        private final Context mContext;

        public PriceViewMenuCallback(OnPricesRefreshRequested host, FragmentManager fm,
                              TextView anchorValueView, GasStation station, GasPrice price) {
            mHost = host;
            mFragmentManager = fm;
            mAnchorValueView = anchorValueView;
            mStation = station;
            mPrice = price;
            mContext = anchorValueView.getContext();
        }

        private void saveToClipboard(String price) {
            final ClipboardManager clipboard = (ClipboardManager)
                    mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(price, price));
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.popup_prices_copy) {
                saveToClipboard(mAnchorValueView.getText().toString());
                Toast.makeText(mContext,
                        R.string.fragment_employee_station_price_copied, Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.popup_prices_edit) {
                EditPriceDialog.editPriceAndSubmit(mStation, mPrice, mHost).show(mFragmentManager, "");
            }
            return true;
        }


    }
}
