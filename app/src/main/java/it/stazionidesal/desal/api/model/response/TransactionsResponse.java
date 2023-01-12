package it.stazionidesal.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.transactions.Transaction;

public class TransactionsResponse extends BaseResponse {

    @SerializedName("transactions")
    private List<Transaction> transactions;

    public TransactionsResponse() {
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
