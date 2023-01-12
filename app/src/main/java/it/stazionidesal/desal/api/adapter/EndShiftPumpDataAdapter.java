package it.stazionidesal.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.services.shifts.ShiftPumpData;

public class EndShiftPumpDataAdapter extends TypeAdapter<List<ShiftPumpData>> {

    @Override
    public List<ShiftPumpData> read(JsonReader in) throws IOException {
        final List<ShiftPumpData> pumpsData = new ArrayList<>();
        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            in.nextName(); // 'gpl' or 'diesel' or 'unleaded'
            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                in.nextName(); // 'self' or 'patp'
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();
                    String pid = null;
                    Double value = null;
                    while (in.peek() != JsonToken.END_OBJECT) {
                        final String key = in.nextName();
                        if ("pid".equals(key)) {
                            pid = in.nextString();
                        } else if ("value".equals(key)) {
                            value = in.nextDouble();
                        }
                    }
                    if (pid == null || value == null) {
                        throw new IOException("No pid or value found.");
                    } else {
                        pumpsData.add(new ShiftPumpData(pid, value));
                    }
                    in.endObject();
                }
                in.endArray();
            }
            in.endObject();
        }
        in.endObject();

        return pumpsData;
    }

    @Override
    public void write(JsonWriter out, List<ShiftPumpData> value) {
        RestAPI.getDefaultGson().toJson(value, ShiftPumpData.class, out);
    }
}
