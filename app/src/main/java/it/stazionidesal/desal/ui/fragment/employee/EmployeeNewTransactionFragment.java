package it.stazionidesal.desal.ui.fragment.employee;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.request.MultipleTransactionsRegisterRequest;
import it.stazionidesal.desal.api.model.response.BaseResponse;
import it.stazionidesal.desal.api.services.TransactionsService;
import it.stazionidesal.desal.util.logic.Conditions;
import it.stazionidesal.desal.util.ui.M;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.view.SimpleTextWatcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.transactions.model.CouponPayment;
import it.stazionidesal.desal.api.services.transactions.model.CreditCardPayment;
import it.stazionidesal.desal.api.services.transactions.model.Deposit;
import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.api.services.transactions.TransactionDirection;
import it.stazionidesal.desal.api.services.transactions.TransactionType;
import it.stazionidesal.desal.api.services.transactions.model.Whatnot;
import it.stazionidesal.desal.ui.activity.employee.EmployeeMainActivity;
import it.stazionidesal.desal.ui.activity.MainActivity;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class EmployeeNewTransactionFragment extends BottomSheetDialogFragment {

    private Fragment mHost;
    private BottomSheetBehavior<FrameLayout> mDialogBehavior;

    private TextInputEditText mDetailsView;
    private TextInputEditText mValueView;

    private TextView mEnqueueView;
    private TextView mCommitView;
    private ProgressBar mCommitWheel;

    private TransactionType mTransactionType;
    private TransactionDirection mTransactionDirection = TransactionDirection.INBOUND;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_employee_new_transaction, container, false);

        mHost = getTargetFragment();
        mTransactionType = TransactionType.fromString(
                requireArguments().getString("transactionType"));

        final TextView titleView = v.findViewById(R.id.fragment_employee_new_transaction_title);
        titleView.setText(mTransactionType.getStringResource());

        final FrameLayout enqueueViewContainer =
                v.findViewById(R.id.fragment_employee_new_transaction_enqueue_container);
        enqueueViewContainer.setEnabled(false);
        final FrameLayout commitViewContainer =
                v.findViewById(R.id.fragment_employee_new_transaction_commit_container);
        commitViewContainer.setEnabled(false);

        mEnqueueView = v.findViewById(R.id.fragment_employee_new_transaction_enqueue);
        mEnqueueView.setOnClickListener(v13 -> {
            enqueueTransaction();
            dismiss();
        });

        mCommitView = v.findViewById(R.id.fragment_employee_new_transaction_commit);
        mCommitView.setOnClickListener(v12 -> commitTransaction());

        mDetailsView = v.findViewById(R.id.fragment_employee_new_transaction_details);
        mValueView = v.findViewById(R.id.fragment_employee_new_transaction_value);

        mCommitWheel = v.findViewById(R.id.fragment_employee_new_transaction_commit_wheel);

        final LinearLayout directionView =
                v.findViewById(R.id.fragment_employee_new_transaction_direction_container);
        final TextView directionLabelView =
                v.findViewById(R.id.fragment_employee_new_transaction_direction_label);
        final ImageView directionIconView =
                v.findViewById(R.id.fragment_employee_new_transaction_direction_icon);
        directionView.setOnClickListener(v1 -> {
            if (mTransactionDirection == TransactionDirection.OUTBOUND) {
                directionLabelView.setText(R.string.transaction_direction_inbound);
                directionIconView.setImageResource(R.drawable.arrow_left);
                mTransactionDirection = TransactionDirection.INBOUND;
            } else {
                directionLabelView.setText(R.string.transaction_direction_outbound);
                directionIconView.setImageResource(R.drawable.arrow_right);
                mTransactionDirection = TransactionDirection.OUTBOUND;
            }
        });


        mDetailsView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && checkValueViewNotEmpty()) {
                    enqueueViewContainer.setEnabled(true);
                    commitViewContainer.setEnabled(true);
                    mEnqueueView.setEnabled(true);
                    mCommitView.setEnabled(true);
                } else {
                    enqueueViewContainer.setEnabled(false);
                    commitViewContainer.setEnabled(false);
                    mEnqueueView.setEnabled(false);
                    mCommitView.setEnabled(false);
                }
            }
        });

        mValueView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (!shouldHaveNonEmptyDetails() || checkDetailsViewNotEmpty())) {
                    enqueueViewContainer.setEnabled(true);
                    commitViewContainer.setEnabled(true);
                    mEnqueueView.setEnabled(true);
                    mCommitView.setEnabled(true);
                } else {
                    enqueueViewContainer.setEnabled(false);
                    commitViewContainer.setEnabled(false);
                    mEnqueueView.setEnabled(false);
                    mCommitView.setEnabled(false);
                }
            }
        });

        final LinearLayout layout =
                v.findViewById(R.id.fragment_employee_new_transaction_container);
        final LinearLayout detailsViewContainer =
                v.findViewById(R.id.fragment_employee_new_transaction_details_container);

        switch (mTransactionType) {
            case WHATNOT:
                layout.setLayoutParams(ViewUtils
                        .newLayoutParams(CoordinatorLayout.LayoutParams.class)
                        .matchParentInWidth().height(330.f).get());
                detailsViewContainer.setVisibility(View.VISIBLE);

                mDetailsView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                mDetailsView.setHint(R.string.fragment_employee_new_transaction_whatnot_hint);

                directionView.setVisibility(View.VISIBLE);
                break;
            case COUPON_PAYMENT:
                layout.setLayoutParams(ViewUtils
                        .newLayoutParams(CoordinatorLayout.LayoutParams.class)
                        .matchParentInWidth().height(330.f).get());
                detailsViewContainer.setVisibility(View.VISIBLE);

                mDetailsView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                mDetailsView.setHint(R.string.fragment_employee_new_transaction_coupon_issue_hint);
                break;
            default:
                layout.setLayoutParams(ViewUtils.newLayoutParams(CoordinatorLayout.LayoutParams.class)
                        .matchParentInWidth().height(180.f).get());
                break;
        }

        return v;
    }

    private void startLoading() {
        mCommitView.setVisibility(View.GONE);
        mCommitWheel.setVisibility(View.VISIBLE);
        mEnqueueView.setEnabled(false);
    }

    private void stopLoading() {
        mCommitWheel.setVisibility(View.GONE);
        mCommitView.setVisibility(View.VISIBLE);
        mEnqueueView.setEnabled(true);
    }

    private boolean checkValueViewNotEmpty() {
        return mValueView.getText() != null && mValueView.getText().length() > 0;
    }

    private boolean checkDetailsViewNotEmpty() {
        return mDetailsView.getText() != null && mDetailsView.getText().length() > 0;
    }

    private boolean shouldHaveNonEmptyDetails() {
        return mTransactionType == TransactionType.WHATNOT ||
                mTransactionType == TransactionType.COUPON_PAYMENT;
    }

    private Transaction buildTransaction() {
        String details = null;
        if (shouldHaveNonEmptyDetails()) {
            if (mDetailsView.getText() != null && mDetailsView.getText().length() > 0) {
                details = mDetailsView.getText().toString();
            } else {
                mDetailsView.setError(getString(R.string.error_field_value_invalid));
                Toast.makeText(getContext(),
                        R.string.error_not_all_fields_are_filled, Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        final double amount;
        try {
            if (mValueView.getText() != null) {
                amount = Double.parseDouble(mValueView.getText().toString());
            } else{
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.fragment_employee_new_transaction_value_invalid,
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        switch (mTransactionType) {
            case WHATNOT:
                final TransactionDirection direction = mTransactionDirection;
                return new Whatnot(amount, details, direction);
            case DEPOSIT:
                return new Deposit(amount);
            case COUPON_PAYMENT:
                return new CouponPayment(amount, details);
            case CREDIT_CARD_PAYMENT:
                return new CreditCardPayment(amount);
        }
        return null;
    }

    private void enqueueTransaction() {
        final Transaction transaction = buildTransaction();
        if (transaction != null && mHost instanceof EmployeeTransactionsFragment) {
            ((EmployeeTransactionsFragment) mHost).enqueueTransaction(transaction);
        }
    }

    private void commitTransaction() {
        final Transaction transaction = buildTransaction();
        if (transaction != null) {
            startLoading();
            getTransactionsService().registerTransactions(
                    Conditions.checkNotNull((EmployeeMainActivity) getActivity())
                            .getSelectedStation().getSid(), new MultipleTransactionsRegisterRequest(
                                    Collections.singletonList(transaction)
                    )).enqueue(new TransactionCommitResponseCallback());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            final FrameLayout layout = ((BottomSheetDialog) dialog1)
                    .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            mDialogBehavior = BottomSheetBehavior.from(Conditions.checkNotNull(layout));

            if (shouldHaveNonEmptyDetails()) {
                mDialogBehavior.setPeekHeight(M.dp(360.f).intValue());
            } else {
                mDialogBehavior.setPeekHeight(M.dp(256.f).intValue());
            }

            mDialogBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int state) {
                    if (state == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {
                }
            });
        });
        return dialog;
    }

    private TransactionsService getTransactionsService() {
        return RestAPI.getTransactionsService();
    }

    public static EmployeeNewTransactionFragment ofType(EmployeeTransactionsFragment host,
                                                        TransactionType transactionType) {
        final EmployeeNewTransactionFragment fragment = new EmployeeNewTransactionFragment();
        fragment.setTargetFragment(host, 0);
        final Bundle args = new Bundle();
        args.putString("transactionType", transactionType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    private class TransactionCommitResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(getContext(), R.string.fragment_employee_new_transaction_success,
                        Toast.LENGTH_SHORT).show();
                dismiss();
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Toast.makeText(getContext(),
                        Status.getErrorDescription(getContext(), body.getStatus()),
                        Toast.LENGTH_LONG
                ).show();
                startActivity(new Intent(getContext(),
                        MainActivity.class).putExtra("mode", "login"));
            } else {
                Toast.makeText(
                        getContext(),
                            Status.getErrorDescription(getContext(), body.getStatus()),
                                Toast.LENGTH_SHORT
                ).show();
                dismiss();
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            stopLoading();
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
}
