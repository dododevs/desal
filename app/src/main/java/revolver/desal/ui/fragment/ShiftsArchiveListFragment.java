package revolver.desal.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.DeSal;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.ShiftsArchiveResponse;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.activity.ShiftSummaryActivity;
import revolver.desal.ui.adapter.ShiftsArchiveAdapter;
import revolver.desal.util.ui.Snacks;

public class ShiftsArchiveListFragment extends Fragment {

    private GasStation mStation;

    private SwipeRefreshLayout mRefresher;
    private LinearLayout mNoShiftsView;
    private CoordinatorLayout mSnackbarContainer;
    private FrameLayout mLoadingView;

    private ShiftsArchiveAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_shifts_archive_list, container, false);

        mStation = DeSal.getPersistentSelectedStation(getContext());
        if (mStation == null) {
            Toast.makeText(getContext(),
                    R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }

        final Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v1 -> requireActivity().onBackPressed());

        final RecyclerView list = v.findViewById(R.id.list);
        list.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new ShiftsArchiveAdapter(new ArrayList<>()));
        mAdapter.setOnShiftClickedListener(shift -> startActivity(new Intent(getContext(), ShiftSummaryActivity.class)
                .putExtra("station", mStation)
                .putExtra("shift", shift)));

        mRefresher = v.findViewById(R.id.refresh);
        mRefresher.setOnRefreshListener(this::refreshShiftsArchive);

        mNoShiftsView = v.findViewById(R.id.activity_shifts_archive_no_shifts);
        mSnackbarContainer = v.findViewById(R.id.activity_shifts_archive);
        mLoadingView = v.findViewById(R.id.dim);

        refreshShiftsArchive();

        return v;
    }

    private void refreshShiftsArchive() {
        startLoading();
        getShiftsService().getEndedShiftsForStation(mStation.getSid())
                .enqueue(new ShiftsArchiveResponseCallback());
    }

    private void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private class ShiftsArchiveResponseCallback implements Callback<ShiftsArchiveResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftsArchiveResponse> call, @NonNull Response<ShiftsArchiveResponse> response) {
            stopLoading();
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
            stopLoading();
            if (mRefresher.isRefreshing()) {
                mRefresher.setRefreshing(false);
            }
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
