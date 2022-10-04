package revolver.desal.ui.adapter;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.List;

import revolver.desal.R;
import revolver.desal.api.services.shifts.GplClockData;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.shifts.ShiftData;
import revolver.desal.api.services.shifts.ShiftDifferenceData;
import revolver.desal.api.services.shifts.ShiftPumpData;
import revolver.desal.api.services.stations.GasPump;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.edit.ShiftFundEditActivity;
import revolver.desal.ui.activity.edit.ShiftGplClockEditActivity;
import revolver.desal.ui.activity.edit.ShiftPumpDataEditActivity;
import revolver.desal.util.ui.ColorUtils;
import revolver.desal.util.ui.ViewUtils;

public class EndedShiftDifferenceDataAdapter2
        extends RecyclerView.Adapter<EndedShiftDifferenceDataAdapter2.ViewHolder> {

    public static final int SHIFT_DATA_EDIT_REQUEST = "lemmeFixThem!".hashCode() & 0xFFFF;
    private static final NumberFormat sTwoFractionDigitsFormatter = NumberFormat.getNumberInstance();

    static {
        sTwoFractionDigitsFormatter.setMaximumFractionDigits(2);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mValueView;
        final ImageView mIconView;
        final TextView mNameView;
        final ImageView mExpandView;
        final View mUpperContainer;
        final View mMiddleContainer;
        final View mLowerContainer;
        final LinearLayout mBadgeContainer;
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
            mMiddleContainer = v.findViewById(R.id.item_ended_shift_difference_data_middle_container);
            mLowerContainer = v.findViewById(R.id.item_ended_shift_difference_data_lower_container);
            mBadgeContainer = v.findViewById(R.id.item_ended_shift_difference_data_badge_container);
            mInitialValueView = v.findViewById(R.id.item_ended_shift_difference_data_initial_value);
            mEndValueView = v.findViewById(R.id.item_ended_shift_difference_data_end_value);
            mExpanded = false;
        }
    }

    private Context mContext;

    private final GasStation mStation;
    private final Shift mShift;
    private final ShiftData mInitialData;
    private final ShiftDifferenceData mDifferenceData;
    private final ShiftData mEndData;

    private final Activity mHost;

    public EndedShiftDifferenceDataAdapter2(final Activity host,
                                            final GasStation station,
                                            final Shift shift) {
        mHost = host;
        mStation = station;
        mShift = shift;
        mInitialData = mShift.getInitialData();
        mDifferenceData = mShift.getDifferenceData();
        mEndData = mShift.getEndData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_ended_shift_difference_data_2, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        String value;
        double totalDifference;

        switch (position) {
            case 0: // fund
                value = (mDifferenceData.getFund() >= 0 ? "+" : "") +
                        sTwoFractionDigitsFormatter.format(mDifferenceData.getFund()) + " €";
                viewHolder.mValueView.setText(value);
                viewHolder.mValueView.setVisibility(View.VISIBLE);
                viewHolder.mIconView.setImageResource(R.drawable.ic_local_atm);
                viewHolder.mNameView.setText(R.string.item_ended_shift_difference_data_fund);
                viewHolder.mInitialValueView.setText(mContext.getString(
                        R.string.item_ended_shift_difference_data_fund_value,
                            mInitialData.getFund()));
                viewHolder.mEndValueView.setText(mContext.getString(
                        R.string.item_ended_shift_difference_data_fund_value,
                            mEndData.getFund()));
                viewHolder.mBadgeContainer.removeAllViews();
                break;
            case 1: // gpl clocks
                totalDifference = computeTotalGplClocksDifference(mDifferenceData.getGplClock());
                value = (totalDifference >= 0 ? "+" : "") +
                        sTwoFractionDigitsFormatter.format(totalDifference);
                viewHolder.mValueView.setText(value);
                viewHolder.mIconView.setImageResource(R.drawable.ic_gpl_clock);
                viewHolder.mNameView.setText(R.string.item_ended_shift_difference_data_2_gpl_clock);
                viewHolder.mBadgeContainer.removeAllViews();

                for (int i = 0; i < mInitialData.getGplClock().size(); i++) {
                    populateGplClocksContainer(viewHolder.mBadgeContainer,
                            mInitialData.getGplClock().get(i),
                                mDifferenceData.getGplClock().get(i),
                                    mEndData.getGplClock().get(i));
                }
                break;
            case 2: // pumps
                totalDifference = computeTotalPumpsDataDifference(mDifferenceData.getPumpsData());
                value = (totalDifference >= 0 ? "+" : "") +
                        sTwoFractionDigitsFormatter.format(totalDifference) + " L";
                viewHolder.mValueView.setText(value);
                viewHolder.mIconView.setImageResource(R.drawable.ic_local_gas_station);
                viewHolder.mNameView.setText(R.string.item_ended_shift_difference_data_2_pump);
                viewHolder.mBadgeContainer.removeAllViews();

                for (int i = 0; i < mInitialData.getPumpsData().size(); i++) {
                    final ShiftPumpData initialPumpData = mInitialData.getPumpsData().get(i);
                    final ShiftPumpData differencePumpData =
                            findPumpDataByPid(mDifferenceData.getPumpsData(), initialPumpData.getPid());
                    final ShiftPumpData endPumpData = mEndData.getPumpsData().get(i);
                    populatePumpsDataContainer(viewHolder.mBadgeContainer,
                            initialPumpData, differencePumpData, endPumpData);
                }
                break;
            case 3:
                viewHolder.itemView.setVisibility(View.INVISIBLE);
                return;
        }

        if (viewHolder.mExpanded) {
            if (viewHolder.getAdapterPosition() == 0) {
                viewHolder.mMiddleContainer.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mLowerContainer.setVisibility(View.VISIBLE);
            }
        } else {
            if (viewHolder.getAdapterPosition() == 0) {
                viewHolder.mMiddleContainer.setVisibility(View.GONE);
            } else {
                viewHolder.mLowerContainer.setVisibility(View.GONE);
            }
        }

        final View.OnClickListener clickListener = v -> {
            if (viewHolder.mExpanded) {
                viewHolder.mExpandView.animate().rotation(0.f)
                        .setDuration(320L).setInterpolator(new OvershootInterpolator()).start();
            } else {
                viewHolder.mExpandView.animate().rotation(180.f)
                        .setDuration(320L).setInterpolator(new OvershootInterpolator()).start();
            }
            viewHolder.mExpanded = !viewHolder.mExpanded;
            notifyItemChanged(viewHolder.getAdapterPosition());
        };

        viewHolder.mUpperContainer.setOnClickListener(clickListener);
        if (viewHolder.getAdapterPosition() == 0) {
            viewHolder.mMiddleContainer.setOnClickListener(v -> {
                if (mShift.getRevision() == null) {
                    mHost.startActivityForResult(new Intent(mContext, ShiftFundEditActivity.class)
                            .putExtra("shift", mShift), SHIFT_DATA_EDIT_REQUEST);
                } else {
                    Toast.makeText(mContext, R.string.item_ended_shift_difference_edit_revisioned,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            viewHolder.mMiddleContainer.setOnClickListener(clickListener);
        }
        viewHolder.mLowerContainer.setOnClickListener(clickListener);

        ((LinearLayout) viewHolder.itemView).getLayoutTransition()
                .enableTransitionType(LayoutTransition.DISAPPEARING);
        ((LinearLayout) viewHolder.itemView).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    private double computeTotalGplClocksDifference(List<GplClockData> differenceClockData) {
        double total = 0;
        for (final GplClockData clockData : differenceClockData) {
            total += clockData.getValue();
        }
        return total;
    }

    private double computeTotalPumpsDataDifference(List<ShiftPumpData> differencePumpData) {
        double total = 0;
        for (final ShiftPumpData pumpData : differencePumpData) {
            total += pumpData.getValue();
        }
        return total;
    }

    private void populateGplClocksContainer(final LinearLayout scrollView,
                                            final GplClockData initialClockData,
                                            final GplClockData differenceClockData,
                                            final GplClockData endClockData) {
        final TextView badgeView = (TextView)
                View.inflate(mContext, R.layout.item_ended_shift_difference_data_badge, null);
        badgeView.setText(mContext.getString(
                R.string.item_ended_shift_difference_data_gpl_clock, initialClockData.getRef()));
        badgeView.setBackgroundResource(R.drawable.badge_item_ended_shift_difference_data_gpl_clock);
        badgeView.setTextColor(Color.BLACK);
        badgeView.setOnClickListener(v -> buildGplClockDifferenceDialog(initialClockData, differenceClockData, endClockData).show());
        scrollView.addView(badgeView, ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                .wrapContentInWidth().wrapContentInHeight().horizontalMargin(8.f)
                    .verticalMargin(2.f).gravity(Gravity.CENTER_VERTICAL).get());
    }

    private Dialog buildGplClockDifferenceDialog(final GplClockData initialClockData,
                                                 final GplClockData differenceClockData,
                                                 final GplClockData endClockData) {
        final Dialog dialog = new Dialog(mContext, R.style.RoundedAdaptiveDialog);
        final View rootView = View.inflate(mContext, R.layout.dialog_ended_shift_difference, null);
        final TextView titleView = rootView.findViewById(R.id.dialog_ended_shift_difference_title);
        titleView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_gpl_clock, initialClockData.getRef()));
        TextView initialValueView = rootView.findViewById(R.id.dialog_ended_shift_difference_initial_value);
        initialValueView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_gpl_clock_value, initialClockData.getValue()));
        TextView endValueView = rootView.findViewById(R.id.dialog_ended_shift_difference_end_value);
        endValueView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_gpl_clock_value, endClockData.getValue()));
        TextView differenceValueView = rootView.findViewById(R.id.dialog_ended_shift_difference_value);
        differenceValueView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_gpl_clock_value, differenceClockData.getValue()));
        View closeButton = rootView.findViewById(R.id.dialog_ended_shift_difference_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        TextView editButton = rootView.findViewById(R.id.dialog_ended_shift_difference_edit);
        if (mShift.getRevision() == null) {
            editButton.setTextColor(ColorUtils.get(mContext, R.color.colorPrimaryDark));
            editButton.setEnabled(true);
            editButton.setOnClickListener(v -> {
                mHost.startActivityForResult(new Intent(mContext, ShiftGplClockEditActivity.class)
                        .putExtra("shift", mShift)
                        .putExtra("key", initialClockData.getRef())
                        .putExtra("label", titleView.getText().toString()), SHIFT_DATA_EDIT_REQUEST);
                dialog.dismiss();
            });
        } else {
            editButton.setTextColor(ColorUtils.get(mContext, R.color.darkGray));
            editButton.setEnabled(false);
            editButton.setOnClickListener(null);
        }
        dialog.setContentView(rootView);
        return dialog;
    }

    private void populatePumpsDataContainer(final LinearLayout scrollView,
                                            final ShiftPumpData initialPumpData,
                                            final ShiftPumpData differencePumpData,
                                            final ShiftPumpData endPumpData) {
        final GasPump pump = findPumpByPid(mStation.getPumps(), initialPumpData.getPid());
        final TextView badgeView = (TextView)
                View.inflate(mContext, R.layout.item_ended_shift_difference_data_badge, null);
        badgeView.setText(mContext.getString(
                R.string.item_ended_shift_difference_data_pump,
                    mContext.getString(pump.getAvailableFuel().getStringResource()),
                        mContext.getString(pump.getType().getStringResource()), pump.getDisplay()));
        switch (pump.getAvailableFuel()) {
            case GPL:
                badgeView.setBackgroundResource(R.drawable.badge_item_ended_shift_difference_data_gpl);
                break;
            case DIESEL:
                badgeView.setBackgroundResource(R.drawable.badge_item_ended_shift_difference_data_diesel);
                break;
            case UNLEADED:
                badgeView.setBackgroundResource(R.drawable.badge_item_ended_shift_difference_data_unleaded);
                break;
        }
        badgeView.setOnClickListener(v -> buildPumpDataDifferenceDialog(pump, initialPumpData, differencePumpData, endPumpData).show());
        scrollView.addView(badgeView, ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                .wrapContentInWidth().wrapContentInHeight().horizontalMargin(8.f)
                    .verticalMargin(2.f).gravity(Gravity.CENTER_VERTICAL).get());
    }

    private Dialog buildPumpDataDifferenceDialog(final GasPump pump,
                                                 final ShiftPumpData initialPumpData,
                                                 final ShiftPumpData differencePumpData,
                                                 final ShiftPumpData endPumpData) {
        final Dialog dialog = new Dialog(mContext, R.style.RoundedAdaptiveDialog);
        final View rootView = View.inflate(mContext, R.layout.dialog_ended_shift_difference, null);
        final TextView titleView = rootView.findViewById(R.id.dialog_ended_shift_difference_title);
        titleView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_pump,
                    mContext.getString(pump.getAvailableFuel().getStringResource()),
                        mContext.getString(pump.getType().getStringResource()),
                            pump.getDisplay()));
        TextView initialValueView = rootView.findViewById(R.id.dialog_ended_shift_difference_initial_value);
        initialValueView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_pump_value, initialPumpData.getValue()));
        TextView endValueView = rootView.findViewById(R.id.dialog_ended_shift_difference_end_value);
        endValueView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_pump_value, endPumpData.getValue()));
        TextView differenceValueView = rootView.findViewById(R.id.dialog_ended_shift_difference_value);
        differenceValueView.setText(mContext.getString(
                R.string.dialog_ended_shift_difference_pump_value, differencePumpData.getValue()));
        View closeButton = rootView.findViewById(R.id.dialog_ended_shift_difference_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        TextView editButton = rootView.findViewById(R.id.dialog_ended_shift_difference_edit);
        if (mShift.getRevision() == null) {
            editButton.setTextColor(ColorUtils.get(mContext, R.color.colorPrimaryDark));
            editButton.setEnabled(true);
            editButton.setOnClickListener(v -> {
                mHost.startActivityForResult(new Intent(mContext, ShiftPumpDataEditActivity.class)
                        .putExtra("shift", mShift)
                        .putExtra("key", pump.getPid())
                        .putExtra("label", titleView.getText().toString()), SHIFT_DATA_EDIT_REQUEST);
                dialog.dismiss();
            });
        } else {
            editButton.setTextColor(ColorUtils.get(mContext, R.color.darkGray));
            editButton.setEnabled(true);
            editButton.setOnClickListener(null);
        }
        dialog.setContentView(rootView);
        return dialog;
    }

    @Override
    public int getItemCount() {
        return 3 + 1 /* dummy empty view for better animations */;
    }

    private GasPump findPumpByPid(List<GasPump> pumps, String pid) {
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

    private ShiftPumpData findPumpDataByPid(List<ShiftPumpData> pumps, String pid) {
        if (pid == null || pumps == null || pumps.isEmpty()) {
            return null;
        }
        for (ShiftPumpData pump : pumps) {
            if (pid.equals(pump.getPid())) {
                return pump;
            }
        }
        return null;
    }
}
