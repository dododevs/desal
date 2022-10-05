package revolver.desal.ui.fragment.employee;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.DeSal;
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
import revolver.desal.ui.activity.employee.EmployeeMainActivity;
import revolver.desal.ui.adapter.InventoryItemsAdapter;
import revolver.desal.ui.fragment.RefreshableContent;
import revolver.desal.util.ui.Snacks;
import revolver.desal.util.ui.ViewUtils;
import revolver.desal.view.DeSalProgressDialog;

public class EmployeeInventoryFragment extends Fragment implements RefreshableContent {

    private View mSnackbarContainer;
    private FrameLayout mLoadingView;

    private TextView mAvailableItemsHeader;
    private FrameLayout mNoItemsView;

    private InventoryItemsAdapter mAdapter;
    private TextView mFilterLabelView;
    private ImageView mFilterIconView;
    private Filter mFilter = FILTER_ALL;

    private static final Filter FILTER_ALL = item -> true;
    private static final Filter FILTER_ONLY_ACCESSORIES = item -> item.getUnit() == Unit.PIECES;
    private static final Filter FILTER_ONLY_OIL = item -> item.getUnit() == Unit.LITERS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_employee_inventory, container, false);

        final RecyclerView recyclerView = v.findViewById(R.id.list);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter = new InventoryItemsAdapter(new ArrayList<>()));

        final CardView filterCard = v.findViewById(R.id.fragment_employee_inventory_filter);
        filterCard.setOnClickListener(v13 -> buildFilterDialog().show());

        mAdapter.setOnItemClickListener((v12, item) -> {
            if (item.getAvailableQuantity() != 0) {
                EmployeeSaleFragment.forItem(EmployeeInventoryFragment.this, item)
                        .show(getParentFragmentManager(), "saleDialog");
            } else {
                buildUselessItemDialog(item).show();
            }
        });

        mAdapter.setOnItemLongClickListener((v1, item) -> {

        });

        mSnackbarContainer = v.findViewById(R.id.fragment_employee_inventory_snackbar);
        mLoadingView = v.findViewById(R.id.dim);
        mNoItemsView = v.findViewById(R.id.fragment_employee_inventory_no_items);
        mAvailableItemsHeader = v.findViewById(R.id.fragment_employee_inventory_available_items);

        mFilterLabelView = v.findViewById(R.id.fragment_employee_inventory_filter_label);
        mFilterIconView = v.findViewById(R.id.fragment_employee_inventory_filter_icon);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshInventoryItems();
    }

    @Override
    public void doRefresh() {
        refreshInventoryItems();
    }

    public void refreshInventoryItems() {
        startLoading();
        getInventoryService().getInventory(ensureSelectedStation().getSid())
                .enqueue(new InventoryResponseCallback());
    }

    private void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingView.animate().alpha(0.0f).setDuration(200L).setStartDelay(100L)
                .withEndAction(() -> {
                    mLoadingView.setVisibility(View.GONE);
                    mLoadingView.setAlpha(1.f);
                }).start();
    }

    private GasStation ensureSelectedStation() {
        final GasStation station = DeSal.getPersistentSelectedStation(getContext());
        return station != null ?
                station : ((EmployeeMainActivity) requireActivity()).getSelectedStation();
    }

    private AlertDialog buildUselessItemDialog(final Item item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.dialog_useless_item_message);
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

    private AlertDialog buildFilterDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.fragment_employee_inventory_filter_title);

        final int index = mFilter == FILTER_ALL ? 0 :
                (mFilter == FILTER_ONLY_ACCESSORIES ? 1 : (mFilter == FILTER_ONLY_OIL ? 2 : 0));
        builder.setSingleChoiceItems(R.array.fragment_employee_inventory_filters,
                index, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mFilter = FILTER_ALL;
                            mFilterLabelView.setText(R.string.fragment_employee_inventory_filter_all);
                            mFilterIconView.setImageResource(R.drawable.ic_store_mall_directory);
                            break;
                        case 1:
                            mFilter = FILTER_ONLY_ACCESSORIES;
                            mFilterLabelView.setText(R.string.fragment_employee_inventory_filter_only_accessories);
                            mFilterIconView.setImageResource(R.drawable.ic_accessories);
                            break;
                        case 2:
                            mFilter = FILTER_ONLY_OIL;
                            mFilterLabelView.setText(R.string.fragment_employee_inventory_filter_only_oil);
                            mFilterIconView.setImageResource(R.drawable.ic_oil);
                            break;
                    }
                    applyFilter();
                    dialog.dismiss();
                });

        return builder.create();
    }

    private void applyFilter() {
        if (mAdapter != null) {
            mAdapter.applyAndStoreFilter(mFilter);
        }
    }

    private InventoryService getInventoryService() {
        return RestAPI.getInventoryService();
    }

    private class InventoryResponseCallback implements Callback<InventoryResponse> {
        @Override
        public void onResponse(@NonNull Call<InventoryResponse> call, @NonNull Response<InventoryResponse> response) {
            stopLoading();
            if (ViewUtils.isFragmentDead(EmployeeInventoryFragment.this)) {
                return;
            }

            final InventoryResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
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
            stopLoading();
            if (ViewUtils.isFragmentDead(EmployeeInventoryFragment.this)) {
                return;
            }
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
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
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    public interface Filter {
        boolean include(Item item);
    }
}
