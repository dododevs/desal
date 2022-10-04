package revolver.desal.ui.activity.employee;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.TransactionsResponse;
import revolver.desal.api.services.TransactionsService;
import revolver.desal.api.services.shifts.GplClockData;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.shifts.ShiftData;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.adapter.TransactionsListAdapter;
import revolver.desal.util.ui.Snacks;

public class ShiftActivity extends AppCompatActivity {

    private static final int END_SHIFT_REQUEST_ID = "shutIt!".hashCode() & 0xffff;

    private Shift mShift;

    private TextView mGplClockRefView;
    private TextView mGplClockValueView;

    private List<GplClockData> mGplClocks;
    private int mGplClockIndex;

    private CardView mTransactionsContainer;
    private TextView mNoTransactionsView;

    private FrameLayout mLoadingView;
    private CoordinatorLayout mSnackbarContainer;

    private TransactionsListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift);

        mShift = getIntent().getParcelableExtra("shift");
        mGplClocks = mShift.getInitialData().getGplClock();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final TextView fundView = findViewById(R.id.activity_shift_fund);
        final ShiftData initialData = mShift.getInitialData();
        fundView.setText(String.format(Locale.ITALIAN, "%.2f", initialData.getFund()));

        mGplClockRefView = findViewById(R.id.activity_shift_gpl_clock_ref);
        mGplClockValueView = findViewById(R.id.activity_shift_gpl_clock_value);

        final LinearLayout gplClockContainer = findViewById(R.id.activity_shift_gpl_clock_container);
        gplClockContainer.setOnClickListener(v -> switchToNextGplClock());

        gplClockContainer.setClickable(mShift.getInitialData().getGplClock().size() > 1);

        final RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        });
        list.setAdapter(mAdapter = new TransactionsListAdapter(new ArrayList<>(), false));

        final TextView endShiftView = findViewById(R.id.activity_shift_end);
        endShiftView.setOnClickListener(v -> startActivityForResult(new Intent(ShiftActivity.this, BeginOrEndShiftActivity.class)
                .putExtra("mode", "end").putExtra("shift", mShift), END_SHIFT_REQUEST_ID));

        mTransactionsContainer = findViewById(R.id.activity_shift_transactions_container);
        mNoTransactionsView = findViewById(R.id.activity_shift_no_transactions);

        mLoadingView = findViewById(R.id.dim);
        mSnackbarContainer = findViewById(R.id.activity_shift);

        switchToNextGplClock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTransactions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == END_SHIFT_REQUEST_ID && resultCode == RESULT_OK) {
            finish();
        }
    }

    private void switchToNextGplClock() {
        final GplClockData clock = mGplClocks.get(mGplClockIndex);
        mGplClockRefView.setText(getString(R.string.activity_shift_gpl_clock, clock.getRef()));
        mGplClockValueView.setText(String.format(Locale.ITALIAN, "%.2f", clock.getValue()));

        mGplClockIndex++;
        if (mGplClockIndex >= mGplClocks.size()) {
            mGplClockIndex = 0;
        }
    }

    private void refreshTransactions() {
        startLoading();
        getTransactionsService().getTransactionsForShift(mShift.getRid())
                .enqueue(new TransactionsListResponseCallback());
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

    private TransactionsService getTransactionsService() {
        return RestAPI.getTransactionsService();
    }

    private class TransactionsListResponseCallback implements Callback<TransactionsResponse> {
        @Override
        public void onResponse(@NonNull Call<TransactionsResponse> call, Response<TransactionsResponse> response) {
            stopLoading();

            final TransactionsResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                mAdapter.updateTransactions(body.getTransactions());
                if (body.getTransactions().size() > 0) {
                    mNoTransactionsView.setVisibility(View.GONE);
                    mTransactionsContainer.setVisibility(View.VISIBLE);
                } else {
                    mTransactionsContainer.setVisibility(View.GONE);
                    mNoTransactionsView.setVisibility(View.VISIBLE);
                }
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(ShiftActivity.this,
                            body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(ShiftActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(ShiftActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
