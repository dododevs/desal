package revolver.desal.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import revolver.desal.R;
import revolver.desal.api.services.shifts.revision.FortechTotal;
import revolver.desal.api.services.stations.Fuel;
import revolver.desal.api.services.stations.GasPrice;
import revolver.desal.util.ui.ViewUtils;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class FortechTotalsInputAdapter extends RecyclerView.Adapter<FortechTotalsInputAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mLitresView;
        final TextView mProfitView;
        final TextView mFuelView;
        final TextView mTypeView;
        final LinearLayout mLowerContainer;
        final View mDividerView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().get());

            mLitresView = v.findViewById(R.id.item_fortech_total_input_litres);
            mProfitView = v.findViewById(R.id.item_fortech_total_input_profit);
            mFuelView = v.findViewById(R.id.item_fortech_total_input_fuel);
            mTypeView = v.findViewById(R.id.item_fortech_total_input_type);
            mLowerContainer = v.findViewById(R.id.item_fortech_total_input_lower_container);
            mDividerView = v.findViewById(R.id.item_fortech_total_input_divider);
        }
    }

    private Context mContext;
    private final List<GasPrice> mPrices;

    private final Map<GasPrice, FortechTotal> mTotals = new ArrayMap<>();

    public FortechTotalsInputAdapter(List<GasPrice> prices) {
        mPrices = prices;
        for (int i = mPrices.size() - 1; i >= 0; i--) {
            if (mPrices.get(i).getFuel() == Fuel.GPL) {
                mPrices.remove(i);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_fortech_total_input, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final GasPrice price = mPrices.get(i);
        viewHolder.mFuelView.setText(price.getFuel().getStringResource());
        viewHolder.mTypeView.setText(price.getType().getStringResource());
        viewHolder.itemView.setOnClickListener(v -> buildFortechTotalInputDialog(viewHolder,
                mPrices.get(viewHolder.getAdapterPosition())).show());
        if (viewHolder.getAdapterPosition() == getItemCount() - 1) {
            viewHolder.mDividerView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return mPrices.size();
    }

    public boolean isComplete() {
        for (final GasPrice price : mPrices) {
            if (!mTotals.containsKey(price)) {
                return false;
            }
        }
        return true;
    }

    public List<FortechTotal> getTotals() {
        return new ArrayList<>(mTotals.values());
    }

    private Dialog buildFortechTotalInputDialog(final ViewHolder viewHolder, final GasPrice price) {
        final Dialog dialog = new Dialog(mContext, R.style.RoundedAdaptiveDialog);
        final View rootView = View.inflate(mContext, R.layout.dialog_fortech_total_input, null);
        final EditText litresView = rootView.findViewById(R.id.dialog_fortech_total_input_litres);
        final EditText profitView = rootView.findViewById(R.id.dialog_fortech_total_input_profit);

        dialog.setContentView(rootView);

        if (mTotals.containsKey(price)) {
            final FortechTotal total = checkNotNull(mTotals.get(price));
            litresView.setText(String.format(Locale.UK, "%.2f", total.getTotalLitres()));
            profitView.setText(String.format(Locale.UK, "%.2f", total.getTotalProfit()));
        }

        TextView closeButton = rootView.findViewById(R.id.dialog_fortech_total_input_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        TextView confirmButton = rootView.findViewById(R.id.dialog_fortech_total_input_confirm);
        confirmButton.setOnClickListener(v -> {
            final double litres, profit;
            try {
                litres = Double.parseDouble(litresView.getText().toString());
            } catch (NumberFormatException e) {
                litresView.setError(mContext.getString(R.string.error_field_value_invalid));
                return;
            }
            try {
                profit = Double.parseDouble(profitView.getText().toString());
            } catch (NumberFormatException e) {
                profitView.setError(mContext.getString(R.string.error_field_value_invalid));
                return;
            }
            viewHolder.mLitresView.setText(String.format(Locale.ITALIAN, "%.2f", litres));
            viewHolder.mProfitView.setText(String.format(Locale.ITALIAN, "%.2f", profit));
            viewHolder.mLowerContainer.setVisibility(View.VISIBLE);

            mTotals.put(price, new FortechTotal(
                    price.getFuel(), price.getType(), litres, profit));
            dialog.dismiss();
        });

        return dialog;
    }
}
