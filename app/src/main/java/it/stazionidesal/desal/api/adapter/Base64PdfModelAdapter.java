package it.stazionidesal.desal.api.adapter;

import android.util.Base64;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Base64PdfModelAdapter extends TypeAdapter<String> {

    @Override
    public String read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return new String(Base64.decode(in.nextString(), Base64.DEFAULT), StandardCharsets.UTF_8);
    }

    @Override
    public void write(JsonWriter out, String value) throws IOException {
        out.value(Base64.encodeToString(value.getBytes(), Base64.DEFAULT));
    }
}
