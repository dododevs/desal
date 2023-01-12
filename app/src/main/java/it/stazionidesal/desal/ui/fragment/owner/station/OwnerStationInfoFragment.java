package it.stazionidesal.desal.ui.fragment.owner.station;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.request.PriceChangeRequest;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.model.response.GasPricesResponse;
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
import it.stazionidesal.desal.api.services.stations.Fuel;
import it.stazionidesal.desal.api.services.stations.GasPrice;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.api.services.stations.PumpType;
import it.stazionidesal.desal.ui.activity.AddPricesActivity;
import it.stazionidesal.desal.ui.activity.MainActivity;
import it.stazionidesal.desal.ui.activity.owner.OwnerStationActivity;
import it.stazionidesal.desal.ui.callback.OnPricesRefreshRequested;
import it.stazionidesal.desal.ui.fragment.employee.EmployeeStationFragment;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class OwnerStationInfoFragment extends Fragment implements OnPricesRefreshRequested {

    private CoordinatorLayout mSnackbarContainer;
    private FrameLayout mLoadingView;

    private LinearLayout mPricesContainer;
    private LinearLayout mNoPricesLayout;

    private List<CardView> mPriceCards = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_owner_station_info, container, false);

        final TextView editPricesButton = v.findViewById(R.id.fragment_owner_station_info_edit_prices);
        final Drawable editPricesIcon = Conditions.checkNotNull(IconUtils
                .drawableWithResolvedColor(getContext(), R.drawable.ic_mode_edit, R.color.colorPrimaryDark));
        editPricesIcon.setBounds(0, 0, M.dp(18.f).intValue(), M.dp(18.f).intValue());
        editPricesButton.setCompoundDrawablesWithIntrinsicBounds(editPricesIcon, null, null, null);
        editPricesButton.setOnClickListener(v1 -> startActivityForResult(new Intent(getContext(), AddPricesActivity.class)
                .putExtra("station", getStation()), EmployeeStationFragment.ADD_PRICES_REQUEST_ID));

        mPricesContainer = v.findViewById(R.id.fragment_owner_station_info_prices_container);
        mNoPricesLayout = v.findViewById(R.id.fragment_owner_station_info_no_prices_container);

        mLoadingView = v.findViewById(R.id.dim);
        mSnackbarContainer = getOwnerStationActivity().getSnackbarContainer();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        requireActivity().startPostponedEnterTransition();
        updatePriceCards(getStation().getPrices());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EmployeeStationFragment.ADD_PRICES_REQUEST_ID &&
                resultCode == Activity.RESULT_OK && data != null) {
            final List<GasPrice> prices = data.getParcelableArrayListExtra("prices");
            startLoading();
            getStationsService().updatePrices(getStation().getSid(),
                    new PriceChangeRequest(prices)).enqueue(new PriceChangeResponseCallback());
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
    public void onResume() {
        super.onResume();
        refreshPrices();
    }

    @Override
    public void refreshPrices() {
        startLoading();
        getStationsService().getPricesForStation(getStation().getSid())
                .enqueue(new GasPricesResponseCallback());
    }

    private void updatePriceCards(List<GasPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            mNoPricesLayout.setVisibility(View.VISIBLE);
            mPricesContainer.setVisibility(View.GONE);
            return;
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
                    leftLayout.setOnClickListener(v12 -> getPricesPopupMenu(leftFuelView, new EmployeeStationFragment.PriceViewMenuCallback(
                            OwnerStationInfoFragment.this, requireActivity()
                                .getSupportFragmentManager(),
                                    leftValueView, getStation(), price)).show());
                    if (fuel.getStringResource() != 0) {
                        leftFuelView.setText(fuel.getStringResource());
                    } else {
                        leftFuelView.setText(fuel.toString());
                    }
                    leftTypeView.setText(R.string.self);
                    leftValueView.setText(String.format(Locale.ITALIAN, "%.3f", price.getPrice()));
                    leftUnitView.setText(R.string.item_price_unit);
                } else if (price.getType() == PumpType.PATP) {
                    rightLayout.setOnClickListener(v1 -> getPricesPopupMenu(rightFuelView, new EmployeeStationFragment.PriceViewMenuCallback(
                            OwnerStationInfoFragment.this, requireActivity()
                                .getSupportFragmentManager(),
                                    rightFuelView, getStation(), price)).show());
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

    private PopupMenu getPricesPopupMenu(final View anchorView,
                                         final PopupMenu.OnMenuItemClickListener menuItemClickListener) {
        final PopupMenu menu = new PopupMenu(getContext(), anchorView, Gravity.CENTER);
        menu.getMenuInflater().inflate(R.menu.popup_prices, menu.getMenu());
        menu.setOnMenuItemClickListener(menuItemClickListener);
        return menu;
    }

    private GasStation getStation() {
        return getOwnerStationActivity().getStation();
    }

    private OwnerStationActivity getOwnerStationActivity() {
        return (OwnerStationActivity) requireActivity();
    }

    private StationsService getStationsService() {
        return RestAPI.getStationsService();
    }

    private class GasPricesResponseCallback implements Callback<GasPricesResponse> {
        @Override
        public void onResponse(@NonNull Call<GasPricesResponse> call, @NonNull Response<GasPricesResponse> response) {
            if (ViewUtils.isFragmentDead(OwnerStationInfoFragment.this)) {
                return;
            }

            stopLoading();
            final GasPricesResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                updatePriceCards(body.getPrices());
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }

        }

        @Override
        public void onFailure(@NonNull Call<GasPricesResponse> call, @NonNull Throwable t) {
            if (ViewUtils.isFragmentDead(OwnerStationInfoFragment.this)) {
                return;
            }
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private class PriceChangeResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
            if (ViewUtils.isFragmentDead(OwnerStationInfoFragment.this)) {
                return;
            }

            stopLoading();
            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Snacks.shorter(mSnackbarContainer, R.string.fragment_employee_station_edit_prices_success);
                refreshPrices();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            if (ViewUtils.isFragmentDead(OwnerStationInfoFragment.this)) {
                return;
            }
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
