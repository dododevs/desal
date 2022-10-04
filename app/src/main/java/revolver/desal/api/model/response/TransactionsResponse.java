package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import revolver.desal.api.services.transactions.Transaction;

public class TransactionsResponse extends BaseResponse {

    @SerializedName("transactions")
    private List<Transaction> transactions;

    public TransactionsResponse() {
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
