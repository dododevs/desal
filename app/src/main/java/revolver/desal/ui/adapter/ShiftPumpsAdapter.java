package revolver.desal.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import revolver.desal.R;
import revolver.desal.api.services.stations.GasPump;
import revolver.desal.ui.activity.employee.BeginOrEndShiftActivity;
import revolver.desal.util.ui.ColorUtils;
import revolver.desal.util.ui.ViewUtils;

public class ShiftPumpsAdapter extends RecyclerView.Adapter<ShiftPumpsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mFuelView;
        final ImageView mIconView;
        final TextView mNameView;
        final TextView mValueView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .width(128.f).height(128.f).horizontalMargin(8.f).verticalMargin(4.f).get());

            mFuelView = v.findViewById(R.id.item_pump_fuel);
            mIconView = v.findViewById(R.id.item_pump_icon);
            mNameView = v.findViewById(R.id.item_pump_name);
            mValueView = v.findViewById(R.id.item_pump_value);
        }
    }

    private final List<GasPump> mPumps;
    private Context mContext;
    private OnPumpCardClickListener mListener;
    private ArrayMap<GasPump, Double> mInitialValues;

    public ShiftPumpsAdapter(List<GasPump> pumps) {
        mPumps = pumps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_pump, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final GasPump pump = mPumps.get(i);
        switch (pump.getAvailableFuel()) {
            case GPL:
                viewHolder.mIconView.setColorFilter(ColorUtils.get(mContext, R.color.gpl));
                viewHolder.mFuelView.setText(R.string.item_pump_gpl);
                viewHolder.mFuelView.setTextColor(ColorUtils.get(mContext, R.color.gpl));
                break;
            case DIESEL:
                viewHolder.mIconView.setColorFilter(ColorUtils.get(mContext, R.color.diesel));
                viewHolder.mFuelView.setText(R.string.item_pump_diesel);
                viewHolder.mFuelView.setTextColor(ColorUtils.get(mContext, R.color.diesel));
                break;
            case UNLEADED:
                viewHolder.mIconView.setColorFilter(ColorUtils.get(mContext, R.color.unleaded));
                viewHolder.mFuelView.setText(R.string.item_pump_unleaded);
                viewHolder.mFuelView.setTextColor(ColorUtils.get(mContext, R.color.unleaded));
                break;
        }
        viewHolder.mNameView.setText(String.format(Locale.ITALIAN, "%s %s",
                mContext.getString(pump.getType().getStringResource()), pump.getDisplay()));

        if (mInitialValues != null && mInitialValues.containsKey(pump)) {
            final Double value = mInitialValues.get(pump);
            if (value != null) {
                viewHolder.mValueView.setText(String.format(Locale.ITALIAN, "%.2f",
                        BeginOrEndShiftActivity.roundToHundredths(value)));
            }
        } else {
            viewHolder.mValueView.setText(null);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(v, mPumps.get(viewHolder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPumps.size();
    }

    public void setInitialValues(ArrayMap<GasPump, Double> values) {
        mInitialValues = new ArrayMap<>(values);
        notifyDataSetChanged();
    }

    public void setOnPumpCardClickListener(OnPumpCardClickListener listener) {
        mListener = listener;
    }

    public interface OnPumpCardClickListener {
        void onClick(View v, GasPump pump);
    }
}
