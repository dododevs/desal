package revolver.desal.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import revolver.desal.DeSal;
import revolver.desal.R;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.employee.EmployeeMainActivity;
import revolver.desal.util.ui.ColorUtils;
import revolver.desal.util.ui.ViewUtils;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class StationSelectionFragment extends DialogFragment {

    private List<GasStation> mStations;
    private int mSelectedStationIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_station_selection, container, false);

        mStations = checkNotNull(requireArguments().getParcelableArrayList("stations"));

        final RadioGroup radioGroup = v.findViewById(R.id.fragment_station_selection_container);
        for (int i = 0; i < mStations.size(); i++) {
            final GasStation station = mStations.get(i);
            final RadioButton button = (RadioButton) View.inflate(
                    getContext(), R.layout.item_station_selection, null);
            button.setText(station.getName());
            button.setId(i);
            radioGroup.addView(button, ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .wrapContentInWidth().wrapContentInHeight().verticalMargin(2.f).get());
        }

        final TextView chooseButton = v.findViewById(R.id.fragment_station_selection_choose);
        chooseButton.setOnClickListener(v12 -> onSelectionConfirmed());

        final TextView closeButton = v.findViewById(R.id.fragment_station_selection_cancel);
        closeButton.setOnClickListener(v1 -> dismiss());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mSelectedStationIndex = checkedId;
            chooseButton.setEnabled(true);
            chooseButton.setTextColor(ColorUtils.get(getContext(), R.color.colorAccentDark));
        });

        return v;
    }

    private void onSelectionConfirmed() {
        final GasStation selected = mStations.get(mSelectedStationIndex);
        DeSal.persistSelectedStation(getContext(), selected);

        final Activity activity = getActivity();
        if (activity instanceof EmployeeMainActivity) {
            final EmployeeMainActivity parent = (EmployeeMainActivity) activity;
            parent.setSelectedStation(selected);
            parent.setupBlobWithStation(selected);
            parent.requestRefresh();
        }

        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(requireContext(), R.style.RoundedAdaptiveDialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    public static StationSelectionFragment withStations(List<GasStation> stations) {
        final StationSelectionFragment fragment = new StationSelectionFragment();
        final Bundle args = new Bundle();
        args.putParcelableArrayList("stations", new ArrayList<>(stations));
        fragment.setArguments(args);
        return fragment;
    }
}
