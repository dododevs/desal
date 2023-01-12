package it.stazionidesal.desal.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.ui.adapter.ColorPickerAdapter;

public class RestrictedColorPickerFragment extends DialogFragment {

    private OnColorSelectedListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_restricted_color_picker, container, false);

        final RecyclerView colors = v.findViewById(R.id.colors);
        colors.setLayoutManager(new GridLayoutManager(
                getContext(), 3, LinearLayoutManager.VERTICAL, false));

        final ColorPickerAdapter adapter = new ColorPickerAdapter(getContext(), null);
        colors.setAdapter(adapter);
        adapter.setOnColorSelectedListener(color -> {
            if (mListener != null) {
                mListener.onColorSelected(color);
                dismiss();
            }
        });

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(requireContext(), R.style.RoundedAdaptiveDialog);
    }

    public static RestrictedColorPickerFragment withListener(@NonNull OnColorSelectedListener l) {
        final RestrictedColorPickerFragment fragment = new RestrictedColorPickerFragment();
        fragment.mListener = l;
        return fragment;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
