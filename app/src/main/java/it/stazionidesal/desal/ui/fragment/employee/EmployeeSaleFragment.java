package it.stazionidesal.desal.ui.fragment.employee;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.services.InventoryService;
import it.stazionidesal.desal.api.services.inventory.Unit;
import it.stazionidesal.desal.util.logic.Conditions;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.IconUtils;
import it.stazionidesal.desal.util.ui.M;
import it.stazionidesal.desal.util.ui.TextUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.view.HorizontalNumberPicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.inventory.Item;
import it.stazionidesal.desal.api.services.inventory.SaleItem;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.api.services.transactions.model.Sale;
import it.stazionidesal.desal.api.services.transactions.payment.Cash;
import it.stazionidesal.desal.api.services.transactions.payment.Coupon;
import it.stazionidesal.desal.api.services.transactions.payment.CreditCard;
import it.stazionidesal.desal.api.services.transactions.payment.PaymentMethod;
import it.stazionidesal.desal.ui.activity.MainActivity;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class EmployeeSaleFragment extends BottomSheetDialogFragment {

    private LinearLayout mCollapsedLayout;
    private LinearLayout mExpandedLayout;

    private ProgressBar mLoadingWheel;
    private TextView mLoadingLabel;

    private BottomSheetBehavior<FrameLayout> mDialogBehavior;

    private Item mItem;
    private int mSaleQuantity = 1;
    private PaymentMethod mPaymentMethod = new Cash();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_employee_sale, container, false);

        mCollapsedLayout = v.findViewById(R.id.fragment_employee_sale_collapsed);
        mExpandedLayout = v.findViewById(R.id.fragment_employee_sale_expanded);
        mLoadingWheel = v.findViewById(R.id.fragment_employee_sale_expanded_confirm_wheel);
        mLoadingLabel = v.findViewById(R.id.fragment_employee_sale_expanded_confirm_label);

        mItem = Conditions.checkNotNull(requireArguments().getParcelable("item"));

        final Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v17 -> dismiss());

        final TextView titleView = v.findViewById(R.id.fragment_employee_sale_title);
        if (mItem.getUnit() == Unit.LITERS) {
            titleView.setText(R.string.fragment_employee_sale_oil_title);
            toolbar.setTitle(R.string.fragment_employee_sale_oil_title);
        } else {
            titleView.setText(R.string.fragment_employee_sale_accessory_title);
            toolbar.setTitle(R.string.fragment_employee_sale_accessory_title);
        }

        TextView nameView = v.findViewById(R.id.fragment_employee_sale_name);
        TextView priceView = v.findViewById(R.id.fragment_employee_sale_price);
        final TextView quantityView = v.findViewById(R.id.fragment_employee_sale_quantity);
        TextView descriptionView = v.findViewById(R.id.fragment_employee_sale_description);
        final TextView totalPriceView = v.findViewById(R.id.fragment_employee_sale_total_price);
        final ImageView paymentMethodView = v.findViewById(R.id.fragment_employee_sale_payment_method);
        HorizontalNumberPicker quantityPickerView = v.findViewById(R.id.fragment_employee_sale_quantity_picker);

        TextView expandedNameView = v.findViewById(R.id.fragment_employee_sale_expanded_name);
        TextView expandedPriceView = v.findViewById(R.id.fragment_employee_sale_expanded_price);
        final TextView expandedQuantityView = v.findViewById(R.id.fragment_employee_sale_expanded_quantity);
        TextView expandedUnitView = v.findViewById(R.id.fragment_employee_sale_quantity_number_of);
        TextView expandedDescriptionView = v.findViewById(R.id.fragment_employee_sale_expanded_description);
        final TextView expandedTotalPriceView = v.findViewById(R.id.fragment_employee_sale_expanded_total_price);
        final FrameLayout expandedConfirmView = v.findViewById(R.id.fragment_employee_sale_confirm_container);

        final String name = TextUtils.capitalizeWords(mItem.getName());
        nameView.setText(name);
        expandedNameView.setText(name);

        if (mItem.getUnit() == Unit.LITERS) {
            priceView.setText(String.format(Locale.ITALIAN, "€%.2f/L", mItem.getPrice()));
        } else {
            priceView.setText(String.format(Locale.ITALIAN, "€%.2f", mItem.getPrice()));
        }
        expandedPriceView.setText(priceView.getText());
        totalPriceView.setText(String.format(Locale.ITALIAN, "€%.2f",
                mItem.getPrice() * mSaleQuantity));
        expandedPriceView.setText(priceView.getText());
        expandedTotalPriceView.setText(totalPriceView.getText());

        quantityView.setText(String.format(Locale.ITALIAN, "%d x", 1));
        expandedQuantityView.setText(quantityView.getText());

        expandedUnitView.setText(getString(R.string.fragment_employee_sale_number_of,
                getString(mItem.getUnit().getNameResource())));

        descriptionView.setText(mItem.getDescription());
        expandedDescriptionView.setText(descriptionView.getText());

        quantityPickerView.setMinimumValue(1);
        quantityPickerView.setMaximumValue(9999);
        quantityPickerView.setOnValueChangedListener((picker, oldVal, newVal) -> {
            mSaleQuantity = newVal;

            quantityView.setText(String.format(Locale.ITALIAN, "%d x", newVal));
            expandedQuantityView.setText(quantityView.getText());

            totalPriceView.setText(String.format(Locale.ITALIAN, "€%.2f",
                    mItem.getPrice() * mSaleQuantity));
            expandedTotalPriceView.setText(totalPriceView.getText());
        });

        TextView optionsView = v.findViewById(R.id.fragment_employee_sale_options);
        TextView confirmView = v.findViewById(R.id.fragment_employee_sale_confirm);

        final Drawable optionsIcon = Conditions.checkNotNull(IconUtils
                .drawable(getContext(), R.drawable.ic_keyboard_arrow_down));
        optionsIcon.setBounds(0, 0, M.dp(8.f).intValue(), M.dp(8.f).intValue());

        final Drawable confirmIcon = Conditions.checkNotNull(IconUtils
                .drawable(getContext(), R.drawable.ic_done));
        optionsIcon.setBounds(0, 0, M.dp(8.f).intValue(), M.dp(8.f).intValue());

        ColorUtils.dyeDrawableWithResolvedColor(getContext(), optionsIcon, R.color.colorPrimaryDark);
        optionsView.setCompoundDrawablesWithIntrinsicBounds(null, null, optionsIcon, null);
        optionsView.setOnClickListener(v16 -> mDialogBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        ColorUtils.dyeDrawableWithResolvedColor(getContext(), confirmIcon, R.color.colorAccent);
        confirmView.setCompoundDrawablesWithIntrinsicBounds(null, null, confirmIcon, null);
        confirmView.setOnClickListener(v15 -> onSaleConfirmed());
        expandedConfirmView.setOnClickListener(v14 -> onSaleConfirmed());

        final View payWithCash = v.findViewById(R.id.fragment_employee_payment_method_cash);
        final View payWithCard = v.findViewById(R.id.fragment_employee_payment_method_credit_card);
        final View payWithCoupon = v.findViewById(R.id.fragment_employee_payment_method_coupon);

        final View cashSelectedView = v.findViewById(R.id.fragment_employee_payment_method_cash_selected);
        final View creditCardSelectedView = v.findViewById(R.id.fragment_employee_payment_method_credit_card_selected);
        final View couponSelectedView = v.findViewById(R.id.fragment_employee_payment_method_coupon_selected);

        payWithCash.setOnClickListener(v13 -> {
            mPaymentMethod = new Cash();
            cashSelectedView.setVisibility(View.VISIBLE);
            creditCardSelectedView.setVisibility(View.INVISIBLE);
            couponSelectedView.setVisibility(View.INVISIBLE);
            paymentMethodView.setImageResource(R.drawable.ic_local_atm);
        });

        payWithCard.setOnClickListener(v12 -> {
            mPaymentMethod = new CreditCard();
            cashSelectedView.setVisibility(View.INVISIBLE);
            creditCardSelectedView.setVisibility(View.VISIBLE);
            couponSelectedView.setVisibility(View.INVISIBLE);
            paymentMethodView.setImageResource(R.drawable.ic_credit_card);
        });

        payWithCoupon.setOnClickListener(v1 -> {
            mPaymentMethod = new Coupon();
            cashSelectedView.setVisibility(View.INVISIBLE);
            creditCardSelectedView.setVisibility(View.INVISIBLE);
            couponSelectedView.setVisibility(View.VISIBLE);
            paymentMethodView.setImageResource(R.drawable.ic_coupon);
        });

        return v;
    }

    private void onSaleConfirmed() {
        final SaleItem item = new SaleItem(mItem.getOid(), mSaleQuantity, new Sale(mPaymentMethod));
        final GasStation station = DeSal.getPersistentSelectedStation(getContext());
        if (station != null) {
            startLoading();
            getInventoryService().sellItem(station.getSid(), item).enqueue(new SaleResponseCallback());
        } else {
            Toast.makeText(getContext(), R.string.error_no_station_selected, Toast.LENGTH_LONG).show();
            dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            final FrameLayout layout = ((BottomSheetDialog) dialog1)
                    .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            mDialogBehavior = BottomSheetBehavior.from(Conditions.checkNotNull(layout));
            mDialogBehavior.setPeekHeight(M.dp(172.f).intValue());
            mDialogBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int state) {
                    if (state == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {
                    mCollapsedLayout.setAlpha(1.f - v);
                    mExpandedLayout.setAlpha(v);
                }
            });
        });
        return dialog;
    }

    private void startLoading() {
        mLoadingLabel.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mLoadingLabel.setVisibility(View.VISIBLE);
    }

    private InventoryService getInventoryService() {
        return RestAPI.getInventoryService();
    }

    public static EmployeeSaleFragment forItem(EmployeeInventoryFragment host, Item item) {
        final EmployeeSaleFragment fragment = new EmployeeSaleFragment();
        final Bundle args = new Bundle();
        args.putParcelable("item", item);
        fragment.setArguments(args);
        fragment.setTargetFragment(host, 0x00);
        return fragment;
    }

    private class SaleResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
            stopLoading();
            if (ViewUtils.isFragmentDead(EmployeeSaleFragment.this)) {
                return;
            }

            final BaseResponse body = response.body();
            if (body == null) {
                Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(getContext(), R.string.fragment_employee_sale_success, Toast.LENGTH_SHORT).show();
                final EmployeeInventoryFragment fragment =
                        (EmployeeInventoryFragment) getTargetFragment();
                if (fragment != null) {
                    fragment.refreshInventoryItems();
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
            if (ViewUtils.isFragmentDead(EmployeeSaleFragment.this)) {
                return;
            }
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }
}
