package it.stazionidesal.desal.api.model.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.stazionidesal.desal.api.services.transactions.Transaction;

public class MultipleTransactionsRegisterRequest {

    @SerializedName("transactions")
    private final List<Transaction> transactions;

    public MultipleTransactionsRegisterRequest(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
