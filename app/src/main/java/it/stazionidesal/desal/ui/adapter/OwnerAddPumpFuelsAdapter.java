package it.stazionidesal.desal.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.Fuel;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class OwnerAddPumpFuelsAdapter extends RecyclerView.Adapter<OwnerAddPumpFuelsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox mCheckBox;
        final TextView mNameView;
        final View mDividerView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams()
                    .matchParentInWidth().wrapContentInHeight().get());

            mCheckBox = v.findViewById(R.id.item_fuel_check);
            mNameView = v.findViewById(R.id.item_fuel_name);
            mDividerView = v.findViewById(R.id.item_fuel_divider);
        }
    }

    private Context mContext;
    private final Fuel[] mValues;
    private final ArrayMap<Fuel, Boolean> mCheckedFuels = new ArrayMap<>();

    public OwnerAddPumpFuelsAdapter(Fuel[] values) {
        mValues = values;
        for (final Fuel fuel : values) {
            mCheckedFuels.put(fuel, false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_fuel, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Fuel fuel = mValues[i];
        viewHolder.mNameView.setText(fuel.getStringResource());

        final ColorStateList colorStateList = new ColorStateList(new int[][]{
                new int[] {
                        -android.R.attr.state_checked
                },
                new int[] {
                        android.R.attr.state_checked
                }
        }, new int[] {
                ColorUtils.get(mContext, fuel.getColorResource()),
                ColorUtils.get(mContext, fuel.getColorResource())
        });
        viewHolder.mCheckBox.setButtonTintList(colorStateList);
        viewHolder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final Fuel fuel1 = mValues[viewHolder.getAdapterPosition()];
            mCheckedFuels.put(fuel1, isChecked);
        });

        if (i >= getItemCount() - 1) {
            viewHolder.mDividerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.length;
    }

    public ArrayMap<Fuel, Boolean> getCheckedFuels() {
        return mCheckedFuels;
    }
}
