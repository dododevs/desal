package revolver.desal.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import revolver.desal.R;
import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.api.services.transactions.model.CouponPayment;
import revolver.desal.api.services.transactions.model.Sale;
import revolver.desal.api.services.transactions.model.Whatnot;
import revolver.desal.util.ui.TextUtils;
import revolver.desal.util.ui.ViewUtils;

public class TransactionsQueueAdapter extends RecyclerView.Adapter<TransactionsQueueAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView mIconView;
        final TextView mTypeView;
        final TextView mDetailsView;
        final TextView mAmountView;
        final ImageView mRemoveView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().verticalMargin(8.f).get());

            mIconView = v.findViewById(R.id.item_transaction_queue_icon);
            mTypeView = v.findViewById(R.id.item_transaction_queue_type);
            mDetailsView = v.findViewById(R.id.item_transaction_queue_details);
            mAmountView = v.findViewById(R.id.item_transaction_queue_amount);
            mRemoveView = v.findViewById(R.id.item_transaction_queue_remove);
        }
    }

    Context mContext;
    List<Transaction> mTransactions;
    private OnTransactionRemovedFromQueueListener mOnRemovedListener;

    public TransactionsQueueAdapter(List<Transaction> transactions) {
        mTransactions = transactions;
    }

    @NonNull
    @Override
    public TransactionsQueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_transaction_queue, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final TransactionsQueueAdapter.ViewHolder viewHolder, final int i) {
        final Transaction transaction = mTransactions.get(i);
        viewHolder.mIconView.setImageResource(transaction.getType().getIconResource());
        viewHolder.mTypeView.setText(TextUtils.capitalizeFirst(
                mContext.getString(transaction.getType().getStringResource())));
        viewHolder.mAmountView.setText(String.format(Locale.ITALIAN, "€%.2f", transaction.getAmount()));
        switch (transaction.getType()) {
            case DEPOSIT:
            case CREDIT_CARD_PAYMENT:
                viewHolder.mDetailsView.setVisibility(View.GONE);
                break;
            case WHATNOT:
                final Whatnot whatnot = (Whatnot) transaction;
                final String title = viewHolder.mTypeView.getText() + " " +
                        mContext.getString(whatnot.getDirection().getStringResource()).toLowerCase();
                viewHolder.mTypeView.setText(title);
                viewHolder.mDetailsView.setText(whatnot.getWhat());
                break;
            case COUPON_PAYMENT:
                final CouponPayment payment = (CouponPayment) transaction;
                viewHolder.mDetailsView.setText(payment.getCustomer());
                break;
            case SALE:
                final Sale sale = (Sale) transaction;
                viewHolder.mDetailsView.setText(TextUtils.capitalizeFirst(sale.getItem().getName()));
                break;
        }

        viewHolder.mRemoveView.setOnClickListener(v -> {
            final int index = viewHolder.getAdapterPosition();
            final Transaction removed = mTransactions.get(index);
            mTransactions.remove(index);
            notifyItemRemoved(index);

            if (mOnRemovedListener != null) {
                mOnRemovedListener.onTransactionRemoved(removed);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    public void addTransaction(Transaction transaction) {
        int length = mTransactions.size();
        mTransactions.add(transaction);
        notifyItemInserted(length);
    }

    public void clearTransactions() {
        int length = getItemCount();
        mTransactions.clear();
        notifyItemRangeRemoved(0, length);
    }

    public List<Transaction> getTransactions() {
        return mTransactions;
    }

    public void setOnTransactionRemovedFromQueueListener(OnTransactionRemovedFromQueueListener l) {
        mOnRemovedListener = l;
    }

    public interface OnTransactionRemovedFromQueueListener {
        void onTransactionRemoved(Transaction transaction);
    }
}
