package it.stazionidesal.desal.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.Fuel;
import it.stazionidesal.desal.api.services.stations.GasPrice;
import it.stazionidesal.desal.api.services.stations.GasPump;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.api.services.stations.PumpType;
import it.stazionidesal.desal.ui.fragment.EditPriceDialog;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.util.logic.Conditions;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class AddPricesActivity extends AppCompatActivity {

    private GasStation mStation;

    private List<CardView> mPriceCards = new ArrayList<>();
    private List<GasPrice> mPrices;

    private LinearLayout mLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prices);

        mLayout = findViewById(R.id.activity_owner_edit_prices_container);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final View confirmView = findViewById(R.id.activity_owner_edit_prices_confirm);
        confirmView.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent()
                    .putParcelableArrayListExtra("prices", new ArrayList<>(mPrices)));
            finish();
        });

        mStation = getIntent().getParcelableExtra("station");
        if (mStation.getPrices() != null && !mStation.getPrices().isEmpty()) {
            mPrices = mStation.getPrices();
        } else {
            ensureDummyPriceList();
        }

        updatePriceCards(mPrices);
    }

    private void updatePriceCards(List<GasPrice> prices) {
        for (View priceView : mPriceCards) {
            mLayout.removeView(priceView);
        }
        mPriceCards = generateViewsForPrices(prices);
        for (View priceView : mPriceCards) {
            mLayout.addView(priceView);
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
            CardView v = (CardView) View.inflate(this, R.layout.item_price, null);
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

            v.setCardBackgroundColor(ColorUtils.get(this, fuel.getColorResource()));

            for (final GasPrice price : pricesForFuel) {
                if (price.getType() == PumpType.SELF) {
                    leftLayout.setOnClickListener(v1 -> {});
                    if (fuel.getStringResource() != 0) {
                        leftFuelView.setText(fuel.getStringResource());
                    } else {
                        leftFuelView.setText(fuel.toString());
                    }
                    leftTypeView.setText(R.string.self);
                    leftUnitView.setText(R.string.item_price_unit);

                    leftValueView.setText(String.format(Locale.ITALIAN, "%.3f", price.getPrice()));
                    leftValueView.setFocusable(true);
                    leftValueView.setClickable(true);
                    leftValueView.setOnClickListener(v12 ->
                            EditPriceDialog.editPrice(mStation, price, (oldPrice, newPrice) -> {
                                int index = mPrices.indexOf(oldPrice);
                                mPrices.remove(oldPrice);
                                mPrices.add(index, newPrice);
                                updatePriceCards(mPrices);
                            }).show(getSupportFragmentManager(), "editPriceDialog"));
                } else if (price.getType() == PumpType.PATP) {
                    rightLayout.setOnClickListener(v13 -> {});
                    if (fuel.getStringResource() != 0) {
                        rightFuelView.setText(fuel.getStringResource());
                    } else {
                        rightFuelView.setText(fuel.toString());
                    }
                    rightTypeView.setText(R.string.patp);
                    rightUnitView.setText(R.string.item_price_unit);

                    rightValueView.setText(String.format(Locale.ITALIAN, "%.3f", price.getPrice()));
                    rightValueView.setFocusable(true);
                    rightValueView.setClickable(true);
                    rightValueView.setOnClickListener(v14 ->
                            EditPriceDialog.editPrice(mStation, price, (oldPrice, newPrice) -> {
                        int index = mPrices.indexOf(oldPrice);
                        mPrices.remove(oldPrice);
                        mPrices.add(index, newPrice);
                        updatePriceCards(mPrices);
                    }).show(getSupportFragmentManager(), "editPriceDialog"));
                }
            }

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight()
                    .horizontalMargin(8.f).verticalMargin(8.f).get());
            views.add(v);
        }

        return views;
    }

    private void ensureDummyPriceList() {
        final List<GasPrice> prices = new ArrayList<>();
        final ArrayMap<Fuel, List<PumpType>> pricesByFuelAndType = new ArrayMap<>();
        for (final GasPump pump : mStation.getPumps()) {
            final Fuel fuel = pump.getAvailableFuel();
            final PumpType type = pump.getType();
            if (!pricesByFuelAndType.containsKey(fuel)) {
                pricesByFuelAndType.put(fuel, new ArrayList<>());
            }
            if (!Conditions.checkNotNull(pricesByFuelAndType.get(fuel)).contains(type)) {
                Conditions.checkNotNull(pricesByFuelAndType.get(fuel)).add(type);
                prices.add(new GasPrice(type, fuel, 0.000));
            }
        }
        mPrices = prices;
    }
}
