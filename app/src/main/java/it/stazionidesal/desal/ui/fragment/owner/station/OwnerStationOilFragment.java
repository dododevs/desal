package it.stazionidesal.desal.ui.fragment.owner.station;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.model.response.InventoryResponse;
import it.stazionidesal.desal.api.services.InventoryService;
import it.stazionidesal.desal.api.services.inventory.Unit;
import it.stazionidesal.desal.util.ui.Snacks;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.view.DeSalProgressDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.inventory.Item;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.MainActivity;
import it.stazionidesal.desal.ui.activity.owner.OwnerInventoryNewItemActivity;
import it.stazionidesal.desal.ui.activity.owner.OwnerStationActivity;
import it.stazionidesal.desal.ui.adapter.OilItemsAdapter;

public class OwnerStationOilFragment extends Fragment {

    private TextView mAvailableItemsHeader;
    private FrameLayout mNoItemsView;

    private OilItemsAdapter mAdapter;
    private SwipeRefreshLayout mRefresher;
    private CoordinatorLayout mSnackbarContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_owner_station_oil, container, false);

        final RecyclerView list = v.findViewById(R.id.list);
        list.setLayoutManager(new GridLayoutManager(
                getContext(), 2, GridLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new OilItemsAdapter(new ArrayList<>()));

        mAdapter.setOnItemClickListener((v13, item) -> startActivity(new Intent(getContext(), OwnerInventoryNewItemActivity.class)
                .putExtra("mode", "oil")
                .putExtra("station", getStation())
                .putExtra("item", item)));

        mAdapter.setOnItemLongClickListener((v12, item) -> buildDeleteItemDialog(item).show());

        final FloatingActionButton newItemButton = v.findViewById(R.id.fab);
        newItemButton.setOnClickListener(v1 -> startActivity(new Intent(getContext(), OwnerInventoryNewItemActivity.class)
                .putExtra("mode", "oil")
                .putExtra("station", getStation())));

        mRefresher = v.findViewById(R.id.refresh);
        mRefresher.setOnRefreshListener(() -> refreshOilItems());

        mNoItemsView = v.findViewById(R.id.fragment_owner_station_oil_no_items);
        mAvailableItemsHeader = v.findViewById(R.id.fragment_owner_station_oil_available_items);
        mSnackbarContainer = getOwnerStationActivity().getSnackbarContainer();

        refreshOilItems();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshOilItems();
    }

    public void refreshOilItems() {
        getOwnerStationActivity().startLoading();
        getInventoryService().getInventory(getStation().getSid()).enqueue(new OilResponseCallback());
    }

    private InventoryService getInventoryService() {
        return RestAPI.getInventoryService();
    }

    private OwnerStationActivity getOwnerStationActivity() {
        return (OwnerStationActivity) requireActivity();
    }

    private AlertDialog buildDeleteItemDialog(final Item item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.dialog_delete_item_message);
        builder.setPositiveButton(R.string.OK, (dialog, which) -> {
            final DeSalProgressDialog progressDialog =
                    DeSalProgressDialog.create(getContext(),
                            getString(R.string.dialog_progress_loading));
            getInventoryService().removeItem(item.getOid())
                    .enqueue(new ItemDeletionResponseCallback(progressDialog));
        });
        builder.setNegativeButton(R.string.CANCEL, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private GasStation getStation() {
        return getOwnerStationActivity().getStation();
    }

    private class OilResponseCallback implements Callback<InventoryResponse> {
        @Override
        public void onResponse(@NonNull Call<InventoryResponse> call, @NonNull Response<InventoryResponse> response) {
            getOwnerStationActivity().stopLoading();
            if (ViewUtils.isFragmentDead(OwnerStationOilFragment.this)) {
                return;
            }
            if (mRefresher.isRefreshing()) {
                mRefresher.setRefreshing(false);
            }

            final InventoryResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                onlyOil(body.getItems());
                mAdapter.updateItems(body.getItems());
                if (body.getItems().size() > 0) {
                    mNoItemsView.setVisibility(View.GONE);
                    mAvailableItemsHeader.setVisibility(View.VISIBLE);
                } else {
                    mNoItemsView.setVisibility(View.VISIBLE);
                    mAvailableItemsHeader.setVisibility(View.GONE);
                }
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<InventoryResponse> call, @NonNull Throwable t) {
            getOwnerStationActivity().stopLoading();
            if (ViewUtils.isFragmentDead(OwnerStationOilFragment.this)) {
                return;
            }
            if (mRefresher.isRefreshing()) {
                mRefresher.setRefreshing(false);
            }
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }

        private void onlyOil(List<Item> items) {
            for (int i = items.size() - 1; i >= 0; i--) {
                if (items.get(i).getUnit() != Unit.LITERS) {
                    items.remove(i);
                }
            }
        }
    }

    private class ItemDeletionResponseCallback implements Callback<BaseResponse> {
        private final DeSalProgressDialog mProgressDialog;

        ItemDeletionResponseCallback(DeSalProgressDialog progressDialog) {
            mProgressDialog = progressDialog;
        }

        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            mProgressDialog.dismiss();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Snacks.shorter(mSnackbarContainer,
                        R.string.fragment_employee_inventory_item_deletion_success);
                refreshOilItems();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(getView(), Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            mProgressDialog.dismiss();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}