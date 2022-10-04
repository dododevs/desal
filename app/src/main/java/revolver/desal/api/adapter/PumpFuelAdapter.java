package revolver.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import revolver.desal.api.services.stations.Fuel;

public class PumpFuelAdapter extends TypeAdapter<Fuel> {

    @Override
    public Fuel read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return Fuel.fromString(in.nextString());
    }

    @Override
    public void write(JsonWriter out, Fuel value) throws IOException {
        out.value(value.toString());
    }
}
