package it.stazionidesal.desal.ui.fragment.owner;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.Fuel;
import it.stazionidesal.desal.api.services.stations.GasPump;
import it.stazionidesal.desal.api.services.stations.PumpType;
import it.stazionidesal.desal.ui.activity.owner.OwnerNewStationActivity;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.Keyboards;
import it.stazionidesal.desal.util.ui.M;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.util.logic.Conditions;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class OwnerAddPumpFragment extends DialogFragment {

    private ImageView mTypeSelfView;
    private ImageView mTypePatpView;
    private TextView mTypeView;
    private EditText mDisplayView;

    private PumpType mType = PumpType.SELF;
    private Fuel mFuel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_owner_add_pump, container, false);

        final RadioGroup fuelSelector = v.findViewById(R.id.fragment_owner_add_pump_fuel_selector);
        for (final Fuel fuel : Fuel.values()) {
            final RadioButton fuelButton = new RadioButton(getContext());
            fuelButton.setText(fuel.getStringResource());
            fuelButton.setId(fuel.ordinal());

            final ColorStateList colorStateList = new ColorStateList(new int[][]{
                    new int[] {
                            -android.R.attr.state_checked
                    },
                    new int[] {
                            android.R.attr.state_checked
                    }
            }, new int[] {
                    ColorUtils.get(getContext(), fuel.getColorResource()),
                    ColorUtils.get(getContext(), fuel.getColorResource())
            });
            fuelButton.setButtonTintList(colorStateList);
            fuelButton.setTextSize(16.8f);
            fuelButton.setPadding(M.dp(8.f).intValue(), 0, 0, 0);
            fuelSelector.addView(fuelButton, ViewUtils.newLayoutParams()
                    .matchParentInWidth().wrapContentInHeight().get());
        }
        fuelSelector.setOnCheckedChangeListener((group, checkedId) -> mFuel = Fuel.values()[checkedId]);

        mTypeSelfView = v.findViewById(R.id.fragment_owner_add_pump_type_self);
        mTypePatpView = v.findViewById(R.id.fragment_owner_add_pump_type_patp);
        mDisplayView = v.findViewById(R.id.fragment_owner_add_pump_display);

        mTypeView = v.findViewById(R.id.fragment_owner_add_pump_type);
        mTypeView.setText(mType.getStringResource());

        final FrameLayout typeViewContainer = v.findViewById(R.id.fragment_owner_add_pump_type_container);
        typeViewContainer.setOnClickListener(v13 -> {
            if (mType == PumpType.PATP) {
                switchToType(PumpType.SELF);
            } else {
                switchToType(PumpType.PATP);
            }
        });

        final TextView confirmView = v.findViewById(R.id.fragment_owner_add_pump_done);
        confirmView.setOnClickListener(v12 -> {
            Keyboards.hideOnWindowAttached(v12);
            submitDataOrComplain();
        });

        final TextView cancelView = v.findViewById(R.id.fragment_owner_add_pump_cancel);
        cancelView.setOnClickListener(v1 -> {
            Keyboards.hideOnWindowAttached(v1);
            dismiss();
        });

        return v;
    }

    private void switchToType(final PumpType newType) {
        if (newType == mType) {
            return;
        }
        if (newType == PumpType.PATP) {
            mTypeSelfView.animate().translationX(48.f)
                    .setDuration(300L).setInterpolator(new AccelerateInterpolator()).start();
            mTypePatpView.animate().translationX(0.f).alpha(1.f)
                    .setDuration(300L).setInterpolator(new DecelerateInterpolator()).start();
            mType = PumpType.PATP;
        } else if (mType == PumpType.PATP) {
            mTypePatpView.animate().translationX(-24.f).alpha(0.f)
                    .setDuration(175L).setInterpolator(new AccelerateInterpolator()).start();
            mTypeSelfView.animate().translationX(0.f)
                    .setDuration(175L).setInterpolator(new DecelerateInterpolator()).start();
            mType = newType;
        }
        mTypeView.setText(newType.getStringResource());
    }

    private void submitDataOrComplain() {
        if (mFuel == null) {
            Toast.makeText(getContext(),
                    R.string.fragment_owner_add_pump_no_fuel_checked, Toast.LENGTH_SHORT).show();
            return;
        }
        final String display = mDisplayView.getText().toString();
        if (!display.matches("[0-9]")) {
            Toast.makeText(getContext(),
                    R.string.fragment_owner_add_pump_bad_display, Toast.LENGTH_SHORT).show();
            return;
        }

        final GasPump pump = new GasPump(display, mFuel, mType);
        Conditions.checkNotNull((OwnerNewStationActivity) getActivity()).onNewPumpAdded(pump);
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(requireContext(), R.style.RoundedAdaptiveDialog);
    }
}
