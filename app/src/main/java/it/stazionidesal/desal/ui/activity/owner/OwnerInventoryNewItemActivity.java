package it.stazionidesal.desal.ui.activity.owner;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.request.ItemCreationRequest;
import it.stazionidesal.desal.api.model.request.ItemUpdateRequest;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.services.InventoryService;
import it.stazionidesal.desal.api.services.inventory.Item;
import it.stazionidesal.desal.api.services.inventory.Unit;
import it.stazionidesal.desal.ui.fragment.RestrictedColorPickerFragment;
import it.stazionidesal.desal.util.ui.Snacks;
import it.stazionidesal.desal.view.PromptedEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.MainActivity;

public class OwnerInventoryNewItemActivity extends AppCompatActivity {

    private GasStation mStation;

    private TextInputEditText mNameView;
    private PromptedEditText mPriceView;
    private TextInputEditText mDescriptionView;

    private ImageView mOilColorView;
    private TextView mOilColorNameView;

    private ProgressBar mLoadingWheel;
    private TextView mConfirmView;

    private CoordinatorLayout mSnackbarContainer;

    private Item mItem;
    private ItemType mItemType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_inventory_new_item);

        if ("oil".equals(getIntent().getStringExtra("mode"))) {
            mItemType = ItemType.OIL;
        } else {
            mItemType = ItemType.ACCESSORY;
        }

        mStation = getIntent().getParcelableExtra("station");
        if (mStation == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final TextInputLayout nameFieldContainer =
                findViewById(R.id.activity_owner_inventory_new_item_name_field_container);
        final TextInputLayout priceFieldContainer =
                findViewById(R.id.activity_owner_inventory_new_item_price_field_container);
        final View descriptionContainer =
                findViewById(R.id.activity_owner_inventory_new_item_description_container);
        final View colorPickerButton =
                findViewById(R.id.activity_owner_oil_new_item_color_container);

        mNameView = findViewById(R.id.activity_owner_inventory_new_item_name);
        mPriceView = findViewById(R.id.activity_owner_inventory_new_item_price);
        mDescriptionView = findViewById(R.id.activity_owner_inventory_new_item_description);
        mOilColorView = findViewById(R.id.activity_owner_oil_new_item_color_icon);
        mOilColorNameView = findViewById(R.id.activity_owner_oil_new_item_color);

        if (mItemType == ItemType.ACCESSORY) {
            colorPickerButton.setVisibility(View.GONE);
            nameFieldContainer.setHint(getString(R.string.activity_owner_inventory_new_item_name_hint));
            priceFieldContainer.setHint(getString(R.string.activity_owner_inventory_new_item_price_hint));
            descriptionContainer.setVisibility(View.VISIBLE);
        } else if (mItemType == ItemType.OIL) {
            colorPickerButton.setVisibility(View.VISIBLE);
            nameFieldContainer.setHint(getString(R.string.activity_owner_oil_new_item_name_hint));
            priceFieldContainer.setHint(getString(R.string.activity_owner_oil_new_item_price_hint));
            descriptionContainer.setVisibility(View.GONE);
        }

        colorPickerButton.setOnClickListener(v -> RestrictedColorPickerFragment.withListener(
                color -> {
                    mOilColorView.setImageTintList(ColorStateList.valueOf(color));
                    mOilColorNameView.setText(String.format(
                            Locale.ITALIAN, "#%s", Integer.toHexString(color & 0xffffff)));
                }).show(getSupportFragmentManager(), "colorPickerDialog"));

        mLoadingWheel = findViewById(R.id.activity_owner_inventory_new_item_wheel);
        mConfirmView = findViewById(R.id.activity_owner_inventory_new_item_confirm);
        mConfirmView.setOnClickListener(v -> submitDataOrComplain());

        mSnackbarContainer = findViewById(R.id.activity_owner_inventory_new_item_snackbar);

        /* if an item was given as an extra, fill in the fields with all the info we have */
        mItem = getIntent().getParcelableExtra("item");
        if (mItem != null) {
            toolbar.setTitle(R.string.activity_owner_inventory_update_item_title);
            mNameView.setText(mItem.getName());
            mPriceView.setText(String.format(Locale.UK, "%.2f", mItem.getPrice()));
            if (mItem.getUnit() == Unit.LITERS) {
                mOilColorNameView.setText(mItem.getDescription());
                mOilColorView.setImageTintList(ColorStateList.valueOf(Color.parseColor(mItem.getDescription())));
            } else {
                mDescriptionView.setText(mItem.getDescription());
            }
        }
    }

    private void submitDataOrComplain() {
        final String name, description;
        final double price;

        if (mNameView.getText() != null && mNameView.getText().length() > 0) {
            name = mNameView.getText().toString();
        } else {
            Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            mNameView.setError(getString(R.string.error_field_value_invalid));
            return;
        }
        if (mPriceView.getText() != null) {
            try {
                price = Double.parseDouble(mPriceView.getText().toString());
            } catch (NumberFormatException e) {
                Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                mPriceView.setError(getString(R.string.error_field_value_invalid));
                return;
            }
        } else {
            Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            mPriceView.setError(getString(R.string.error_field_value_invalid));
            return;
        }

        if (mItemType == ItemType.OIL) {
            description = mOilColorNameView.getText().toString();
        } else {
            if (mDescriptionView.getText() != null && mDescriptionView.getText().length() > 0) {
                description = mDescriptionView.getText().toString();
            } else {
                Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                mDescriptionView.setError(getString(R.string.error_field_value_invalid));
                return;
            }
        }

        startLoading();
        if (mItem != null) {
            getInventoryService().updateItem(new ItemUpdateRequest(
                    new Item(mItem.getOid(), mItem.getSid(), name, -1,
                            mItemType == ItemType.OIL ? Unit.LITERS : Unit.PIECES, price, description)))
                    .enqueue(new UpdateItemResponseCallback());
        } else {
            getInventoryService().createNewItem(mStation.getSid(), new ItemCreationRequest(
                    new Item(null, null, name, -1, mItemType == ItemType.OIL ?
                            Unit.LITERS : Unit.PIECES, price, description)))
                    .enqueue(new CreateNewItemResponseCallback());
        }
    }

    private void startLoading() {
        mConfirmView.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    private InventoryService getInventoryService() {
        return RestAPI.getInventoryService();
    }

    private class CreateNewItemResponseCallback implements Callback<BaseResponse> {
        @Override
        @EverythingIsNonNull
        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
            if (isDestroyed()) {
                return;
            }

            stopLoading();
            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(OwnerInventoryNewItemActivity.this,
                        R.string.activity_owner_inventory_new_item_success, Toast.LENGTH_SHORT).show();
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status
                                .getErrorDescription(OwnerInventoryNewItemActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(OwnerInventoryNewItemActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(OwnerInventoryNewItemActivity.this, body.getStatus()));
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<BaseResponse> call, Throwable t) {
            if (isDestroyed()) {
                return;
            }
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private class UpdateItemResponseCallback implements Callback<BaseResponse> {
        @Override
        @EverythingIsNonNull
        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
            if (isDestroyed()) {
                return;
            }

            stopLoading();
            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(OwnerInventoryNewItemActivity.this,
                        R.string.activity_owner_inventory_update_item_success, Toast.LENGTH_SHORT).show();
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status
                                .getErrorDescription(OwnerInventoryNewItemActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(OwnerInventoryNewItemActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(OwnerInventoryNewItemActivity.this, body.getStatus()));
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<BaseResponse> call, Throwable t) {
            if (isDestroyed()) {
                return;
            }
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private enum ItemType {
        ACCESSORY, OIL
    }
}
