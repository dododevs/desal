package revolver.desal.ui.activity.owner;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.services.StationsService;
import revolver.desal.api.services.stations.GasPump;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.adapter.OwnerNewStationPumpsAdapter;
import revolver.desal.ui.fragment.owner.OwnerAddPumpFragment;
import revolver.desal.util.ui.IconUtils;
import revolver.desal.util.ui.Keyboards;
import revolver.desal.util.ui.M;
import revolver.desal.util.ui.Snacks;

public class OwnerNewStationActivity extends AppCompatActivity
        implements OwnerNewStationPumpsAdapter.OnItemStartDragListener, OwnerNewStationPumpsAdapter.OnItemRemovedListener {

    private TextView mNoPumpsView;
    private RecyclerView mPumpsList;
    private OwnerNewStationPumpsAdapter mAdapter;
    private ItemTouchHelper mItemMoveHelper;

    private TextInputEditText mNameView;
    private TextInputEditText mGplClockCountView;
    private CoordinatorLayout mSnackbarContainer;

    private TextView mConfirmView;
    private ProgressBar mLoadingWheel;

    private final List<GasPump> mPumps = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_new_station);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final TextView addPumpButton = findViewById(R.id.activity_owner_new_station_add_pump);
        final Drawable addPumpButtonIcon =  IconUtils.drawableWithResolvedColor(
                this, R.drawable.ic_add, R.color.colorPrimaryDark);
        addPumpButtonIcon.setBounds(0, 0, M.dp(16.f).intValue(), M.dp(16.f).intValue());
        addPumpButton.setCompoundDrawables(addPumpButtonIcon, null, null, null);
        addPumpButton.setCompoundDrawablePadding(M.dp(4.f).intValue());
        addPumpButton.setOnClickListener(v -> {
            Keyboards.hideOnWindowAttached(v);
            new OwnerAddPumpFragment().show(getSupportFragmentManager(), "newPumpFragment");
        });

        mLoadingWheel = findViewById(R.id.activity_owner_new_station_wheel);
        mConfirmView = findViewById(R.id.activity_owner_new_station_confirm);
        mConfirmView.setOnClickListener(v -> {
            Keyboards.hideOnWindowAttached(v);
            submitDataOrComplain();
        });

        mPumpsList = findViewById(R.id.activity_owner_new_station_pumps);
        mPumpsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mPumpsList.setAdapter(mAdapter = new OwnerNewStationPumpsAdapter(mPumps, this, this));

        final ItemTouchHelper.Callback moveCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder moving,
                                  @NonNull RecyclerView.ViewHolder target) {
                final int startPosition = moving.getAdapterPosition();
                final int endPosition = target.getAdapterPosition();
                mAdapter.notifyItemMoved(startPosition, endPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            }
        };
        mItemMoveHelper = new ItemTouchHelper(moveCallback);
        mItemMoveHelper.attachToRecyclerView(mPumpsList);

        mNoPumpsView = findViewById(R.id.activity_owner_new_station_no_pumps);
        mNameView = findViewById(R.id.activity_owner_new_station_name);
        mGplClockCountView = findViewById(R.id.activity_owner_new_station_gpl_clock_count);
        mSnackbarContainer = findViewById(R.id.activity_owner_new_station_snackbar);

        mGplClockCountView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Keyboards.hideOnWindowAttached(v);
            }
            return true;
        });
    }

    public void onNewPumpAdded(final GasPump pump) {
        boolean wasEmpty = mPumps.isEmpty();
        mPumps.add(pump);
        if (wasEmpty && pump != null) {
            mNoPumpsView.setVisibility(View.GONE);
            mPumpsList.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onPumpRemoved(GasPump pump) {
        mPumps.remove(pump);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemMoveHelper.startDrag(viewHolder);
    }

    private void submitDataOrComplain() {
        final String name;
        final int gplClockCount;
        if (mNameView.getText() != null && mNameView.getText().length() > 0) {
            name = mNameView.getText().toString();
        } else {
            mNameView.setError(getString(R.string.error_field_value_invalid));
            Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            return;
        }
        if (mGplClockCountView.getText() != null && mNameView.getText().length() > 0) {
            try {
                gplClockCount = Integer.parseInt(mGplClockCountView.getText().toString());
            } catch (NumberFormatException e) {
                mGplClockCountView.setError(getString(R.string.error_field_value_invalid));
                Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
                return;
            }
        } else {
            mGplClockCountView.setError(getString(R.string.error_field_value_invalid));
            Snacks.normal(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            return;
        }

        if (mPumps.isEmpty()) {
            Snacks.normal(mSnackbarContainer, R.string.error_no_pumps_added);
            return;
        }

        startLoading();

        final GasStation station = new GasStation(name, gplClockCount, mPumps, null);
        getStationsService().createStation(station).enqueue(new CreateNewStationCallback());
    }

    private void startLoading() {
        mConfirmView.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    private StationsService getStationsService() {
        return RestAPI.getStationsService();
    }

    private class CreateNewStationCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(OwnerNewStationActivity.this,
                        R.string.activity_owner_new_station_success, Toast.LENGTH_SHORT).show();
                finish();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(OwnerNewStationActivity.this, body.getStatus()),
                            getString(R.string.action_login), v -> startActivity(new Intent(OwnerNewStationActivity.this,
                                    MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(
                        OwnerNewStationActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
