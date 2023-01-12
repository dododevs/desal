package it.stazionidesal.desal.api.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import it.stazionidesal.desal.api.services.transactions.Transaction;
import it.stazionidesal.desal.api.services.transactions.model.CouponPayment;
import it.stazionidesal.desal.api.services.transactions.model.CreditCardPayment;
import it.stazionidesal.desal.api.services.transactions.model.Deposit;
import it.stazionidesal.desal.api.services.transactions.model.Sale;
import it.stazionidesal.desal.api.services.transactions.model.Whatnot;

public class TransactionAdapter extends TypeAdapter<Transaction> {

    private static final Gson gson = new Gson();

    @Override
    public Transaction read(JsonReader in) {
        final JsonElement object = JsonParser.parseReader(in);
        final Transaction transaction = gson.fromJson(object, Transaction.class);
        switch (transaction.getType()) {
            case DEPOSIT:
                return gson.fromJson(object, Deposit.class);
            case WHATNOT:
                return gson.fromJson(object, Whatnot.class);
            case SALE:
                return gson.fromJson(object, Sale.class);
            case CREDIT_CARD_PAYMENT:
                return gson.fromJson(object, CreditCardPayment.class);
            case COUPON_PAYMENT:
                return gson.fromJson(object, CouponPayment.class);
            default:
                return transaction;
        }
    }

    @Override
    public void write(JsonWriter out, Transaction value) throws IOException {
        out.jsonValue(gson.toJson(value, value.getClass()));
    }
}
