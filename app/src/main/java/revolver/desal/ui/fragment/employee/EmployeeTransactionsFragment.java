package revolver.desal.ui.fragment.employee;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.request.MultipleTransactionsRegisterRequest;
import revolver.desal.api.services.TransactionsService;
import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.api.services.transactions.TransactionType;
import revolver.desal.ui.activity.employee.EmployeeMainActivity;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.activity.employee.TransactionsListActivity;
import revolver.desal.ui.adapter.TransactionsQueueAdapter;
import revolver.desal.ui.fragment.RefreshableContent;
import revolver.desal.util.ui.Snacks;
import revolver.desal.view.DeSalProgressDialog;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class EmployeeTransactionsFragment extends Fragment
        implements RefreshableContent, TransactionsQueueAdapter.OnTransactionRemovedFromQueueListener {

    private CardView mQueueContainer;
    private View mEmptyQueueView;
    private FloatingActionButton mCommitQueueButton;
    private CoordinatorLayout mSnackbarContainer;

    private DeSalProgressDialog mProgressDialog;
    private TransactionsQueueAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_employee_transactions, container, false);

        final RecyclerView queue = v.findViewById(R.id.fragment_employee_transactions_queue);
        queue.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        queue.setAdapter(mAdapter = new TransactionsQueueAdapter(new ArrayList<>()));
        mAdapter.setOnTransactionRemovedFromQueueListener(this);

        mEmptyQueueView = v.findViewById(R.id.fragment_employee_transactions_queue_empty);
        mQueueContainer = v.findViewById(R.id.fragment_employee_transactions_queue_container);
        mSnackbarContainer = v.findViewById(R.id.fragment_employee_transactions);

        mCommitQueueButton = v.findViewById(R.id.fragment_employee_transactions_commit_pending);
        mCommitQueueButton.setOnClickListener(v16 -> commitQueuedTransactions());

        final FloatingActionButton transactionsListButton = v.findViewById(R.id.fab);
        transactionsListButton.setOnClickListener(v15 -> startActivity(new Intent(getContext(), TransactionsListActivity.class)
                .putExtra("station", checkNotNull((EmployeeMainActivity) getActivity())
                .getSelectedStation())));

        final CardView newWhatnotView =
                v.findViewById(R.id.fragment_employee_transactions_new_whatnot);
        newWhatnotView.setOnClickListener(v14 -> EmployeeNewTransactionFragment.ofType(EmployeeTransactionsFragment.this,
                TransactionType.WHATNOT).show(requireActivity()
                    .getSupportFragmentManager(), "newTransactionDialog"));

        final CardView newDepositView =
                v.findViewById(R.id.fragment_employee_transactions_new_deposit);
        newDepositView.setOnClickListener(v13 -> EmployeeNewTransactionFragment.ofType(EmployeeTransactionsFragment.this,
                TransactionType.DEPOSIT).show(requireActivity()
                    .getSupportFragmentManager(), "newTransactionDialog"));

        final CardView newCreditCardPaymentView =
                v.findViewById(R.id.fragment_employee_transactions_new_credit_card);
        newCreditCardPaymentView.setOnClickListener(v12 -> EmployeeNewTransactionFragment.ofType(EmployeeTransactionsFragment.this,
                TransactionType.CREDIT_CARD_PAYMENT).show(requireActivity()
                    .getSupportFragmentManager(), "newTransactionDialog"));

        final CardView newCouponPaymentView =
                v.findViewById(R.id.fragment_employee_transactions_new_coupon_payment);
        newCouponPaymentView.setOnClickListener(v1 -> EmployeeNewTransactionFragment.ofType(EmployeeTransactionsFragment.this,
                TransactionType.COUPON_PAYMENT).show(requireActivity()
                    .getSupportFragmentManager(), "newTransactionDialog"));

        return v;
    }

    public void commitQueuedTransactions() {
        startLoading();
        getTransactionsService().registerTransactions(
                checkNotNull((EmployeeMainActivity) getActivity())
                        .getSelectedStation().getSid(),
                    new MultipleTransactionsRegisterRequest(mAdapter.getTransactions())
        ).enqueue(new TransactionsCommitResponseCallback());
    }

    public void enqueueTransaction(final Transaction transaction) {
        boolean wasEmpty = mAdapter.getTransactions().isEmpty();
        mAdapter.addTransaction(transaction);
        if (wasEmpty) {
            mEmptyQueueView.setVisibility(View.GONE);
            mQueueContainer.setVisibility(View.VISIBLE);
            mCommitQueueButton.show();
        }
    }

    @Override
    public void onTransactionRemoved(Transaction transaction) {
        if (mAdapter.getTransactions().isEmpty()) {
            onEmptyQueue();
        }
    }

    private void onEmptyQueue() {
        mQueueContainer.setVisibility(View.GONE);
        mEmptyQueueView.setVisibility(View.VISIBLE);
        mCommitQueueButton.hide();
    }

    private void startLoading() {
        mProgressDialog = DeSalProgressDialog.create(getContext(),
                getString(R.string.fragment_employee_transactions_committing));
        mProgressDialog.show();
    }

    private void stopLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
            mProgressDialog = null;
        }
    }

    @Override
    public void doRefresh() {
    }

    private TransactionsService getTransactionsService() {
        return RestAPI.getTransactionsService();
    }

    private class TransactionsCommitResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Snacks.shorter(mSnackbarContainer, R.string.fragment_employee_transactions_success);
                mAdapter.clearTransactions();
                onEmptyQueue();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
