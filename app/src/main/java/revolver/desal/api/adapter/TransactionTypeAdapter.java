package revolver.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import revolver.desal.api.services.transactions.TransactionType;

public class TransactionTypeAdapter extends TypeAdapter<TransactionType> {

    @Override
    public TransactionType read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return TransactionType.fromString(in.nextString());
    }

    @Override
    public void write(JsonWriter out, TransactionType value) throws IOException {
        out.value(value.toString());
    }
}
