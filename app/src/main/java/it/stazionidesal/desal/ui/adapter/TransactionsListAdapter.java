package it.stazionidesal.desal.ui.adapter;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class TransactionsListAdapter extends TransactionsQueueAdapter {

    class ViewHolder extends TransactionsQueueAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
            if (mClickableItems) {
                v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                        .matchParentInWidth().wrapContentInHeight().verticalMargin(8.f).get());
            } else {
                v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                        .matchParentInWidth().wrapContentInHeight().verticalMargin(16.f).get());
            }
            mRemoveView.setVisibility(View.GONE);
        }
    }

    private final boolean mClickableItems;
    private OnTransactionClickListener mClickListener;

    public TransactionsListAdapter(List<Transaction> transactions, boolean clickable) {
        super(transactions);
        mClickableItems = clickable;
    }

    @NonNull
    @Override
    public TransactionsQueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_transaction_queue, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final TransactionsQueueAdapter.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        if (mClickableItems) {
            viewHolder.itemView.setBackground(ViewUtils.getSelectableBackgroundDrawable(mContext));
            viewHolder.itemView.setFocusable(true);
            viewHolder.itemView.setClickable(true);
            viewHolder.itemView.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onTransactionClicked(mTransactions.get(viewHolder.getAdapterPosition()));
                }
            });
        }
    }

    public void updateTransactions(List<Transaction> transactions) {
        mTransactions = transactions;
        notifyDataSetChanged();
    }

    public void removeTransaction(final Transaction transaction) {
        final List<Transaction> current = new ArrayList<>(mTransactions);
        current.remove(transaction);
        mTransactions = new ArrayList<>(current);
        notifyDataSetChanged();
    }

    public void setOnTransactionClickedListener(OnTransactionClickListener listener) {
        mClickListener = listener;
    }

    public interface OnTransactionClickListener {
        void onTransactionClicked(final Transaction transaction);
    }
}
