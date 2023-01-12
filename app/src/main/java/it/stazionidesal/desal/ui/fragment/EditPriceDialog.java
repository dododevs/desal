package it.stazionidesal.desal.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.request.PriceChangeRequest;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.services.StationsService;
import it.stazionidesal.desal.ui.callback.OnPriceUpdated;
import it.stazionidesal.desal.ui.callback.OnPricesRefreshRequested;
import it.stazionidesal.desal.util.logic.Conditions;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.Keyboards;
import it.stazionidesal.desal.view.SimpleTextWatcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasPrice;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.MainActivity;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class EditPriceDialog extends DialogFragment {

    private GasStation mStation;
    private GasPrice mOldPrice;

    private TextView mLoadingLabel;
    private ProgressBar mLoadingWheel;

    private EditText mHiddenPriceField;
    private final TextView[] mPriceDigits = new TextView[4];
    private int mCurrentPriceDigit = 0;

    private OnPricesRefreshRequested mPriceUpdater;
    private OnPriceUpdated mPriceUpdatedListener;
    private boolean mShouldSubmitEditedPrice = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.dialog_edit_price, container, false);

        if (getArguments() == null || (mStation = getArguments().getParcelable("station")) == null) {
            Toast.makeText(getContext(), R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            dismiss();
            return null;
        }
        mOldPrice = requireArguments().getParcelable("price");

        final LinearLayout digitsContainerView = v.findViewById(R.id.dialog_edit_price_digits_container);
        digitsContainerView.setOnClickListener(v13 -> Keyboards.showOnWindowAttached(v13));

        final TextView fuelView = v.findViewById(R.id.dialog_edit_price_fuel);
        fuelView.setText(String.format(Locale.ITALIAN, "%s %s",
                getString(mOldPrice.getFuel().getStringResource()),
                    getString(mOldPrice.getType().getStringResource())));

        mHiddenPriceField = v.findViewById(R.id.dialog_edit_price_hidden_field);
        /*
        mHiddenPriceField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        Keyboards.hideOnWindowAttached(v);
                        onPriceConfirmed();
                    } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (getPriceFromDigits() > 0.0 || mCurrentPriceDigit != 0) {
                            onBackspacePressed();
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismiss();
                    } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                        final int digit = keyCode - KeyEvent.KEYCODE_0;
                        onNewDigitTyped(digit);
                    }
                }
                highlightCurrentDigit();
                return true;
            }
        });
        */
        mHiddenPriceField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 1) {
                    return;
                }
                if (count > 0) {
                    final char c = s.charAt(start);
                    if (c >= 48 && c <= 57) {
                        onNewDigitTyped(c - 48);
                    }
                } else {
                    if (getPriceFromDigits() > 0.0 || mCurrentPriceDigit != 0) {
                        onBackspacePressed();
                    }
                }

                highlightCurrentDigit();
            }

            @Override
            public void afterTextChanged(Editable s) {
                /* keep the text field non-empty to trigger the backspace event when needed */
                if (s.length() == 0) {
                    s.append("-");
                }
            }
        });
        mHiddenPriceField.setOnEditorActionListener((v12, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Keyboards.hideOnWindowAttached(v12);
                onPriceConfirmed();
            }
            return true;
        });

        final View confirmView = v.findViewById(R.id.dialog_edit_price_confirm);
        confirmView.setOnClickListener(v1 -> {
            Keyboards.hideOnWindowAttached(v1);
            onPriceConfirmed();
        });

        mPriceDigits[0] = v.findViewById(R.id.dialog_edit_price_digit_0);
        mPriceDigits[1] = v.findViewById(R.id.dialog_edit_price_digit_1);
        mPriceDigits[2] = v.findViewById(R.id.dialog_edit_price_digit_2);
        mPriceDigits[3] = v.findViewById(R.id.dialog_edit_price_digit_3);

        mLoadingWheel = v.findViewById(R.id.dialog_edit_price_confirm_wheel);
        mLoadingLabel = v.findViewById(R.id.dialog_edit_price_confirm_label);

        highlightCurrentDigit();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHiddenPriceField != null) {
            mHiddenPriceField.requestFocus();
            mHiddenPriceField.postDelayed(() -> Keyboards.showOnWindowAttached(mHiddenPriceField), 150L);
        }
    }

    private void onNewDigitTyped(int digit) {
        mPriceDigits[mCurrentPriceDigit].setText(String.valueOf(digit));
        mCurrentPriceDigit++;
        if (mCurrentPriceDigit > 3) {
            mCurrentPriceDigit = 0;
        }
    }

    private void onBackspacePressed() {
        mCurrentPriceDigit--;
        if (mCurrentPriceDigit < 0) {
            mCurrentPriceDigit = 3;
        }
        mPriceDigits[mCurrentPriceDigit].setText(String.valueOf(0));
    }

    private void highlightCurrentDigit() {
        mPriceDigits[mCurrentPriceDigit].setTextColor(
                ColorUtils.get(getContext(), R.color.colorAccent));
        for (int i = 0; i < mPriceDigits.length; i++) {
            if (i != mCurrentPriceDigit) {
                mPriceDigits[i].setTextColor(Color.WHITE);
            }
        }
    }

    private void onPriceConfirmed() {
        final double price = getPriceFromDigits();
        final GasPrice newPrice = new GasPrice(mOldPrice.getType(), mOldPrice.getFuel(), price);
        if (mPriceUpdatedListener != null) {
            mPriceUpdatedListener.onPriceUpdated(mOldPrice, newPrice);
        }

        if (mShouldSubmitEditedPrice) {
            startLoading();
            getStationsService().updatePrices(mStation.getSid(),
                    new PriceChangeRequest(new ArrayList<>(Collections.singletonList(newPrice))))
                        .enqueue(new PriceChangeResponseCallback());
        } else {
            dismiss();
        }
    }

    private double getPriceFromDigits() {
        return Double.parseDouble(String.format(
                Locale.ITALIAN, "%s.%s%s%s", mPriceDigits[0].getText(),
                    mPriceDigits[1].getText(), mPriceDigits[2].getText(), mPriceDigits[3].getText()));
    }

    private void startLoading() {
        mLoadingLabel.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mLoadingLabel.setVisibility(View.VISIBLE);
    }

    private StationsService getStationsService() {
        return RestAPI.getStationsService();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        final Window window = Conditions.checkNotNull(dialog.getWindow());
        window.setBackgroundDrawable(new ColorDrawable(
                ColorUtils.get(getContext(), android.R.color.transparent)));

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        return dialog;
    }

    public static EditPriceDialog editPrice(final GasStation station,
                                            final GasPrice price,
                                            final OnPriceUpdated onPriceUpdatedListener) {
        final EditPriceDialog dialog = new EditPriceDialog();
        final Bundle args = new Bundle();
        args.putParcelable("station", station);
        args.putParcelable("price", price);
        dialog.setArguments(args);
        dialog.mShouldSubmitEditedPrice = false;
        dialog.mPriceUpdatedListener = onPriceUpdatedListener;
        return dialog;
    }

    public static EditPriceDialog editPriceAndSubmit(final GasStation station,
                                                     final GasPrice price,
                                                     final OnPricesRefreshRequested host) {
        final EditPriceDialog dialog = new EditPriceDialog();
        final Bundle args = new Bundle();
        args.putParcelable("station", station);
        args.putParcelable("price", price);
        dialog.setArguments(args);
        dialog.mShouldSubmitEditedPrice = true;
        dialog.mPriceUpdater = host;
        return dialog;
    }

    private class PriceChangeResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(getContext(), R.string.dialog_edit_price_success, Toast.LENGTH_SHORT).show();
                if (mPriceUpdater != null) {
                    mPriceUpdater.refreshPrices();
                }
                dismiss();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Toast.makeText(getContext(),
                        Status.getErrorDescription(getContext(), body.getStatus()),
                        Toast.LENGTH_LONG
                ).show();
                startActivity(new Intent(getContext(),
                        MainActivity.class).putExtra("mode", "login"));
            } else {
                Toast.makeText(getContext(), Status.getErrorDescription(
                        getContext(), body.getStatus()), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            stopLoading();
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }
}
