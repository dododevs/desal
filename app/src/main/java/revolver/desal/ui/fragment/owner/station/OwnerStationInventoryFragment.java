package revolver.desal.ui.fragment.owner.station;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.response.InventoryResponse;
import revolver.desal.api.services.InventoryService;
import revolver.desal.api.services.inventory.Item;
import revolver.desal.api.services.inventory.Unit;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.activity.owner.OwnerInventoryNewItemActivity;
import revolver.desal.ui.activity.owner.OwnerStationActivity;
import revolver.desal.ui.adapter.InventoryItemsAdapter;
import revolver.desal.util.ui.Snacks;
import revolver.desal.util.ui.ViewUtils;
import revolver.desal.view.DeSalProgressDialog;

public class OwnerStationInventoryFragment extends Fragment {

    private TextView mAvailableItemsHeader;
    private FrameLayout mNoItemsView;

    private InventoryItemsAdapter mAdapter;
    private SwipeRefreshLayout mRefresher;
    private CoordinatorLayout mSnackbarContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_owner_station_inventory, container, false);

        final RecyclerView list = v.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new InventoryItemsAdapter(new ArrayList<>()));

        mAdapter.setOnItemClickListener((v13, item) -> startActivity(new Intent(getContext(), OwnerInventoryNewItemActivity.class)
                .putExtra("station", getStation())
                .putExtra("item", item)));

        mAdapter.setOnItemLongClickListener((v12, item) -> buildDeleteItemDialog(item).show());

        final FloatingActionButton newItemButton = v.findViewById(R.id.fab);
        newItemButton.setOnClickListener(v1 -> startActivity(new Intent(getContext(), OwnerInventoryNewItemActivity.class)
                .putExtra("station", getStation())));

        mRefresher = v.findViewById(R.id.refresh);
        mRefresher.setOnRefreshListener(() -> refreshInventoryItems());

        mNoItemsView = v.findViewById(R.id.fragment_owner_station_inventory_no_items);
        mAvailableItemsHeader = v.findViewById(R.id.fragment_owner_station_inventory_available_items);
        mSnackbarContainer = getOwnerStationActivity().getSnackbarContainer();

        refreshInventoryItems();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshInventoryItems();
    }

    public void refreshInventoryItems() {
        getOwnerStationActivity().startLoading();
        getInventoryService().getInventory(getStation().getSid()).enqueue(new InventoryResponseCallback());
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

    private class InventoryResponseCallback implements Callback<InventoryResponse> {
        @Override
        public void onResponse(@NonNull Call<InventoryResponse> call, @NonNull Response<InventoryResponse> response) {
            getOwnerStationActivity().stopLoading();
            if (ViewUtils.isFragmentDead(OwnerStationInventoryFragment.this)) {
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
                onlyAccessories(body.getItems());
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
            if (ViewUtils.isFragmentDead(OwnerStationInventoryFragment.this)) {
                return;
            }
            if (mRefresher.isRefreshing()) {
                mRefresher.setRefreshing(false);
            }
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }

        private List<Item> onlyAccessories(List<Item> items) {
            for (int i = items.size() - 1; i >= 0; i--) {
                if (items.get(i).getUnit() != Unit.PIECES) {
                    items.remove(i);
                }
            }
            return items;
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
                Log.d("ItemDeletionRCallback", "null body");
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Snacks.shorter(mSnackbarContainer,
                        R.string.fragment_employee_inventory_item_deletion_success);
                refreshInventoryItems();
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
            Log.d("ItemDeletionRCallback", "", t);
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}