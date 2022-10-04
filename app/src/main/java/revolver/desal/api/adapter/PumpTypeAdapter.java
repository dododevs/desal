package revolver.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import revolver.desal.api.services.stations.PumpType;

public class PumpTypeAdapter extends TypeAdapter<PumpType> {

    @Override
    public PumpType read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return PumpType.fromString(in.nextString());
    }

    @Override
    public void write(JsonWriter out, PumpType value) throws IOException {
        out.value(value.toString());
    }
}
