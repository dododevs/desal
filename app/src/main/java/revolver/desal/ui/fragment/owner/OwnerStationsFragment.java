package revolver.desal.ui.fragment.owner;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.StationsResponse;
import revolver.desal.api.services.StationsService;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.activity.owner.OwnerNewStationActivity;
import revolver.desal.ui.adapter.ExpandedStationsListAdapter;
import revolver.desal.ui.fragment.RefreshableContent;
import revolver.desal.util.ui.Snacks;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class OwnerStationsFragment extends Fragment implements RefreshableContent {

    public static final int CREATE_NEW_STATION_REQUEST_ID = "newStation!".hashCode() & 0xffff;

    private List<GasStation> mStations;
    private ExpandedStationsListAdapter mAdapter;

    private CoordinatorLayout mSnackbarContainer;
    private FrameLayout mLoadingView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_owner_stations, container, false);

        mStations = checkNotNull(requireArguments().getParcelableArrayList("stations"));

        final RecyclerView list = v.findViewById(R.id.list);
        list.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new ExpandedStationsListAdapter(getActivity(), mStations));

        final FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(v1 -> startActivityForResult(new Intent(getContext(),
                OwnerNewStationActivity.class), CREATE_NEW_STATION_REQUEST_ID));

        mSnackbarContainer = v.findViewById(R.id.fragment_owner_stations_snackbar);
        mLoadingView = v.findViewById(R.id.dim);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        doRefresh();
    }

    @Override
    public void doRefresh() {
        startLoading();
        getStationsService().getStations().enqueue(new StationsListResponseCallback());
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

    public static OwnerStationsFragment withStations(List<GasStation> stations) {
        final OwnerStationsFragment fragment = new OwnerStationsFragment();
        final Bundle args = new Bundle();
        args.putParcelableArrayList("stations", new ArrayList<>(stations));
        fragment.setArguments(args);
        return fragment;
    }

    private StationsService getStationsService() {
        return RestAPI.getStationsService();
    }

    private class StationsListResponseCallback implements Callback<StationsResponse> {
        @Override
        public void onResponse(@NonNull Call<StationsResponse> call, Response<StationsResponse> response) {
            stopLoading();

            final StationsResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                mStations = body.getStations();
                mAdapter.updateStationsList(mStations);
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<StationsResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
