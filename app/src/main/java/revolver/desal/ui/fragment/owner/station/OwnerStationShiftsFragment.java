package revolver.desal.ui.fragment.owner.station;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.response.ShiftsArchiveResponse;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.DeSalFragmentHostActivity;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.activity.ShiftSummaryActivity;
import revolver.desal.ui.activity.owner.OwnerShiftRevisionActivity;
import revolver.desal.ui.adapter.ShiftsArchiveAdapter;
import revolver.desal.util.ui.Snacks;
import revolver.desal.view.DeSalProgressDialog;

public class OwnerStationShiftsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mRefresher;
    private View mNoShiftsView;

    private ShiftsArchiveAdapter mAdapter;
    private View mSnackbarContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_owner_station_shifts, container, false);

        final RecyclerView list = v.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new ShiftsArchiveAdapter(new ArrayList<>()));

        mAdapter.setOnShiftClickedListener(shift -> {
            if (shift.getRevision() != null) {
                startActivity(new Intent(getContext(), ShiftSummaryActivity.class)
                        .putExtra("station", getStation())
                        .putExtra("shift", shift));
            } else {
                startActivity(new Intent(getContext(), OwnerShiftRevisionActivity.class)
                        .putExtra("station", getStation())
                        .putExtra("shift", shift));
            }
        });

        mAdapter.setOnShiftLongClickedListener(shift -> buildContextMenu(shift).show());

        mRefresher = v.findViewById(R.id.refresh);
        mRefresher.setOnRefreshListener(this);

        mNoShiftsView = v.findViewById(R.id.fragment_owner_station_shifts_no_shifts);
        mSnackbarContainer = getParentActivity().getSnackbarContainer();

        refreshShiftsArchive();

        return v;
    }

    @Override
    public void onRefresh() {
        refreshShiftsArchive();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshShiftsArchive();
    }

    private void refreshShiftsArchive() {
        getParentActivity().startLoading();
        getShiftsService().getEndedShiftsForStation(getStation().getSid())
                .enqueue(new ShiftsArchiveResponseCallback());
    }

    private DeSalFragmentHostActivity getParentActivity() {
        return (DeSalFragmentHostActivity) requireActivity();
    }

    private GasStation getStation() {
        return getParentActivity().getStation();
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private AlertDialog buildContextMenu(final Shift shift) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setItems(R.array.fragment_owner_station_shifts_menu, (dialog, which) -> {
            switch (which) {
                case 0:
                    buildShiftDeletionDialog(shift).show();
                    break;
                case 1:
                    buildShiftRevisionCancellingDialog(shift).show();
                    break;
            }
        });
        return builder.create();
    }

    private AlertDialog buildShiftDeletionDialog(final Shift shift) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.fragment_owner_station_shifts_delete_title);
        builder.setMessage(R.string.fragment_owner_station_shifts_delete_message);
        builder.setPositiveButton(R.string.OK, (dialog, which) -> {
            final DeSalProgressDialog progressDialog =
                    DeSalProgressDialog.create(getContext(), getString(R.string.dialog_progress_loading));
            dialog.dismiss();
            progressDialog.show();
            getShiftsService().removeShift(shift.getRid())
                    .enqueue(new ShiftDeletionResponseCallback(progressDialog));
        });
        builder.setNegativeButton(R.string.CANCEL, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private AlertDialog buildShiftRevisionCancellingDialog(final Shift shift) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.fragment_owner_station_shifts_unrevise_title);
        builder.setMessage(R.string.fragment_owner_station_shifts_unrevise_message);
        builder.setPositiveButton(R.string.OK, (dialog, which) -> {
            final DeSalProgressDialog progressDialog =
                    DeSalProgressDialog.create(getContext(), getString(R.string.dialog_progress_loading));
            dialog.dismiss();
            progressDialog.show();
            getShiftsService().unreviseShift(shift.getRid())
                    .enqueue(new ShiftRevisionCancellingResponseCallback(progressDialog));
        });
        builder.setNegativeButton(R.string.CANCEL, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private class ShiftsArchiveResponseCallback implements Callback<ShiftsArchiveResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftsArchiveResponse> call, @NonNull Response<ShiftsArchiveResponse> response) {
            getParentActivity().stopLoading();
            if (mRefresher.isRefreshing()) {
                mRefresher.setRefreshing(false);
            }

            final ShiftsArchiveResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                mAdapter.updateShifts(body.getShifts());
                if (body.getShifts().isEmpty()) {
                    mNoShiftsView.setVisibility(View.VISIBLE);
                } else {
                    mNoShiftsView.setVisibility(View.GONE);
                }
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status
                                .getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<ShiftsArchiveResponse> call, @NonNull Throwable t) {
            getParentActivity().stopLoading();
            if (mRefresher.isRefreshing()) {
                mRefresher.setRefreshing(false);
            }
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private class ShiftDeletionResponseCallback implements Callback<BaseResponse> {

        private final DeSalProgressDialog mProgressDialog;

        ShiftDeletionResponseCallback(DeSalProgressDialog progressDialog) {
            mProgressDialog = progressDialog;
        }

        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            mProgressDialog.dismiss();

            final BaseResponse body = response.body();
            if (body == null) {
                Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(getContext(), R.string.fragment_owner_station_shifts_delete_success,
                        Toast.LENGTH_SHORT).show();
                refreshShiftsArchive();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Toast.makeText(getContext(),
                        Status.getErrorDescription(getContext(), body.getStatus()),
                            Toast.LENGTH_LONG
                ).show();
                startActivity(new Intent(getContext(),
                        MainActivity.class).putExtra("mode", "login"));
            } else {
                Toast.makeText(getContext(),
                        Status.getErrorDescription(getContext(), body.getStatus()),
                            Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            mProgressDialog.dismiss();
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private class ShiftRevisionCancellingResponseCallback implements Callback<BaseResponse> {

        private final DeSalProgressDialog mProgressDialog;

        ShiftRevisionCancellingResponseCallback(DeSalProgressDialog progressDialog) {
            mProgressDialog = progressDialog;
        }

        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            mProgressDialog.dismiss();

            final BaseResponse body = response.body();
            if (body == null) {
                Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(getContext(), R.string.fragment_owner_station_shifts_unrevise_success,
                        Toast.LENGTH_SHORT).show();
                refreshShiftsArchive();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Toast.makeText(getContext(),
                        Status.getErrorDescription(getContext(), body.getStatus()),
                        Toast.LENGTH_LONG
                ).show();
                startActivity(new Intent(getContext(),
                        MainActivity.class).putExtra("mode", "login"));
            } else {
                Toast.makeText(getContext(),
                        Status.getErrorDescription(getContext(), body.getStatus()),
                        Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            mProgressDialog.dismiss();
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }
}
