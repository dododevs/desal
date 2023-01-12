package it.stazionidesal.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import it.stazionidesal.desal.api.services.transactions.TransactionDirection;

public class TransactionDirectionAdapter extends TypeAdapter<TransactionDirection> {

    @Override
    public TransactionDirection read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return TransactionDirection.fromString(in.nextString());
    }

    @Override
    public void write(JsonWriter out, TransactionDirection value) throws IOException {
        out.value(value.toString());
    }
}
