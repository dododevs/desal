package revolver.desal.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

import revolver.desal.R;
import revolver.desal.api.services.shifts.GplClockData;
import revolver.desal.api.services.shifts.ShiftData;
import revolver.desal.api.services.shifts.ShiftDifferenceData;
import revolver.desal.api.services.shifts.ShiftPumpData;
import revolver.desal.api.services.stations.GasPump;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.util.ui.ViewUtils;

public class EndedShiftDifferenceDataAdapter
        extends RecyclerView.Adapter<EndedShiftDifferenceDataAdapter.ViewHolder> {

    private static final NumberFormat sTwoFractionDigitsFormatter = NumberFormat.getNumberInstance();

    static {
        sTwoFractionDigitsFormatter.setMaximumFractionDigits(2);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mValueView;
        final ImageView mIconView;
        final TextView mNameView;
        final ImageView mExpandView;
        final LinearLayout mUpperContainer;
        final LinearLayout mLowerContainer;
        final TextView mInitialValueView;
        final TextView mEndValueView;
        boolean mExpanded;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().topMargin(4.f).get());

            mValueView = v.findViewById(R.id.item_ended_shift_difference_data_value);
            mIconView = v.findViewById(R.id.item_ended_shift_difference_data_icon);
            mNameView = v.findViewById(R.id.item_ended_shift_difference_data_name);
            mExpandView = v.findViewById(R.id.item_ended_shift_difference_data_expand);
            mUpperContainer = v.findViewById(R.id.item_ended_shift_difference_data_upper_container);
            mLowerContainer = v.findViewById(R.id.item_ended_shift_difference_data_lower_container);
            mInitialValueView = v.findViewById(R.id.item_ended_shift_difference_data_initial_value);
            mEndValueView = v.findViewById(R.id.item_ended_shift_difference_data_end_value);
            mExpanded = false;
        }
    }

    private Context mContext;

    private final GasStation mStation;
    private final ShiftData mInitialData;
    private final ShiftDifferenceData mDifferenceData;
    private final ShiftData mEndData;

    public EndedShiftDifferenceDataAdapter(final GasStation station,
                                           final ShiftData initialData,
                                           final ShiftDifferenceData data,
                                           final ShiftData endData) {
        mStation = station;
        mInitialData = initialData;
        mDifferenceData = data;
        mEndData = endData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_ended_shift_difference_data, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        if (position == 0) {
            String value = (mDifferenceData.getFund() >= 0 ? "+" : "-") +
                    sTwoFractionDigitsFormatter.format(mDifferenceData.getFund()) + " €";
            viewHolder.mValueView.setText(value);
            viewHolder.mIconView.setImageResource(R.drawable.ic_local_atm);
            viewHolder.mNameView.setText(
                    R.string.item_ended_shift_difference_data_fund);
            viewHolder.mInitialValueView.setText(mContext.getString(
                    R.string.item_ended_shift_difference_data_fund_value,
                        mInitialData.getFund()));
            viewHolder.mEndValueView.setText(mContext.getString(
                    R.string.item_ended_shift_difference_data_fund_value,
                        mEndData.getFund()));
        } else if (position > 0 && position <= mDifferenceData.getGplClock().size()) {
            position--;
            final GplClockData initial = mInitialData.getGplClock().get(position);
            final GplClockData end = mEndData.getGplClock().get(position);
            final GplClockData difference = mDifferenceData.getGplClock().get(position);

            String value = (difference.getValue() >= 0 ? "+" : "-") +
                    sTwoFractionDigitsFormatter.format(difference.getValue());
            viewHolder.mValueView.setText(value);
            viewHolder.mIconView.setImageResource(R.drawable.ic_gpl_clock);
            viewHolder.mNameView.setText(mContext.getString(
                    R.string.item_ended_shift_difference_data_gpl_clock, initial.getRef()));
            viewHolder.mInitialValueView.setText(
                    sTwoFractionDigitsFormatter.format(initial.getValue()));
            viewHolder.mEndValueView.setText(
                    sTwoFractionDigitsFormatter.format(end.getValue()));
        } else if (position > mDifferenceData.getGplClock().size() &&
                position <= mDifferenceData.getPumpsData().size() + mDifferenceData.getGplClock().size()) {
            position -= mDifferenceData.getGplClock().size() + 1;
            final ShiftPumpData initial = mInitialData.getPumpsData().get(position);
            final ShiftPumpData end = mEndData.getPumpsData().get(position);
            final ShiftPumpData difference = mDifferenceData.getPumpsData().get(position);
            final GasPump pump = findPumpByPid(initial.getPid());

            String value = (difference.getValue() >= 0 ? "+" : "-") +
                    sTwoFractionDigitsFormatter.format(difference.getValue()) + " L";
            viewHolder.mValueView.setText(value);
            viewHolder.mIconView.setImageResource(R.drawable.ic_local_gas_station);
            viewHolder.mNameView.setText(mContext.getString(
                    R.string.item_ended_shift_difference_data_pump,
                        mContext.getString(pump.getAvailableFuel().getStringResource()),
                            mContext.getString(pump.getType().getStringResource()),
                                pump.getDisplay()));
            viewHolder.mInitialValueView.setText(
                    mContext.getString(R.string.item_ended_shift_difference_data_pump_value,
                            initial.getValue()));
            viewHolder.mEndValueView.setText(
                    mContext.getString(R.string.item_ended_shift_difference_data_pump_value,
                            end.getValue()));
        }

        if (viewHolder.mExpanded) {
            viewHolder.mLowerContainer.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mLowerContainer.setVisibility(View.GONE);
        }

        viewHolder.mUpperContainer.setOnClickListener(v -> {
            if (viewHolder.mExpanded) {
                viewHolder.mExpandView.animate().rotation(0.f)
                        .setDuration(320L).setInterpolator(new OvershootInterpolator()).start();
            } else {
                viewHolder.mExpandView.animate().rotation(180.f)
                        .setDuration(320L).setInterpolator(new OvershootInterpolator()).start();
            }
            viewHolder.mExpanded = !viewHolder.mExpanded;
            notifyItemChanged(viewHolder.getAdapterPosition());
        });

        viewHolder.mLowerContainer.setOnClickListener(v -> {
            if (viewHolder.mExpanded) {
                viewHolder.mExpandView.animate().rotation(0.f)
                        .setDuration(320L).setInterpolator(new OvershootInterpolator()).start();
            } else {
                viewHolder.mExpandView.animate().rotation(180.f)
                        .setDuration(320L).setInterpolator(new OvershootInterpolator()).start();
            }
            viewHolder.mExpanded = !viewHolder.mExpanded;
            notifyItemChanged(viewHolder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return 1 /* fund */ + mDifferenceData.getGplClock().size() +
                mDifferenceData.getPumpsData().size();
    }

    private GasPump findPumpByPid(String pid) {
        final List<GasPump> pumps = mStation.getPumps();
        if (pid == null || pumps == null || pumps.isEmpty()) {
            return null;
        }
        for (GasPump pump : pumps) {
            if (pid.equals(pump.getPid())) {
                return pump;
            }
        }
        return null;
    }
}
