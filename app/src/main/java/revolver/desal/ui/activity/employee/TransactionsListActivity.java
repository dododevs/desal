package revolver.desal.ui.activity.employee;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.response.ShiftResponse;
import revolver.desal.api.model.response.TransactionsResponse;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.TransactionsService;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.adapter.TransactionsListAdapter;
import revolver.desal.ui.fragment.RefreshableContent;
import revolver.desal.util.ui.Snacks;
import revolver.desal.view.DeSalProgressDialog;

public class TransactionsListActivity extends AppCompatActivity implements RefreshableContent {

    private GasStation mStation;
    private TransactionsListAdapter mAdapter;

    private SwipeRefreshLayout mRefresher;
    private FrameLayout mLoadingView;
    private DeSalProgressDialog mDeletionDialog;
    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_list);

        if ((mStation = getIntent().getParcelableExtra("station")) == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(mAdapter = new TransactionsListAdapter(new ArrayList<>(), true));
        mAdapter.setOnTransactionClickedListener(transaction -> buildRemoveTransactionDialog(transaction).show());

        mRefresher = findViewById(R.id.refresh);
        mRefresher.setOnRefreshListener(this::doRefresh);

        mLoadingView = findViewById(R.id.dim);
        mSnackbarContainer = findViewById(R.id.activity_transactions_list);

        refreshTransactionsList();
    }

    @Override
    public void doRefresh() {
        refreshTransactionsList();
    }

    private void refreshTransactionsList() {
        startLoading();
        getShiftsService().getActiveShiftForStation(mStation.getSid())
                .enqueue(new ActiveShiftResponseCallback());
    }

    private AlertDialog buildRemoveTransactionDialog(final Transaction transaction) {
        return new AlertDialog.Builder(this)
                .setTitle(R.string.activity_transactions_list_remove_title)
                .setMessage(R.string.activity_transactions_list_remove_message)
                .setPositiveButton(R.string.OK, (dialog, which) -> removeTransaction(transaction)).setNegativeButton(R.string.CANCEL, (dialog, which) -> dialog.dismiss()).create();
    }

    private void removeTransaction(final Transaction transaction) {
        mDeletionDialog = DeSalProgressDialog.create(this,
                getString(R.string.activity_transactions_list_remove_loading));
        mDeletionDialog.show();
        getTransactionsService().removeTransaction(transaction.getTid())
                .enqueue(new TransactionDeletionResponseCallback(transaction));
    }

    private void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mRefresher.setRefreshing(false);
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
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
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(TransactionsListActivity.this,
                        body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(TransactionsListActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(TransactionsListActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private class ActiveShiftResponseCallback implements Callback<ShiftResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftResponse> call, Response<ShiftResponse> response) {
            final ShiftResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                final Shift shift = body.getShift();
                getTransactionsService().getTransactionsForShift(shift.getRid())
                        .enqueue(new TransactionsListResponseCallback());
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                stopLoading();
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(TransactionsListActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(TransactionsListActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                stopLoading();
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(TransactionsListActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<ShiftResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }

    private class TransactionDeletionResponseCallback implements Callback<BaseResponse> {
        final Transaction mTransaction;

        TransactionDeletionResponseCallback(Transaction transaction) {
            mTransaction = transaction;
        }

        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            mDeletionDialog.dismiss();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            } if (body.isSuccessful()) {
                Snacks.shorter(mSnackbarContainer, R.string.activity_transactions_list_remove_success);
                mAdapter.removeTransaction(mTransaction);
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(TransactionsListActivity.this, body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(TransactionsListActivity.this,
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(TransactionsListActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            mDeletionDialog.dismiss();
        }
    }
}
