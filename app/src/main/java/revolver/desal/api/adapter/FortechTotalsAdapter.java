package revolver.desal.api.adapter;

import android.util.ArrayMap;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import revolver.desal.api.services.shifts.revision.FortechTotal;
import revolver.desal.api.services.stations.Fuel;
import revolver.desal.api.services.stations.PumpType;

import static revolver.desal.util.logic.Conditions.checkNotNull;

public class FortechTotalsAdapter extends TypeAdapter<List<FortechTotal>> {

    @Override
    public List<FortechTotal> read(JsonReader in) throws IOException {
        final List<FortechTotal> fortechTotals = new ArrayList<>();
        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            final String fuel = in.nextName(); // 'gpl' or 'diesel' or 'unleaded'
            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                final String type = in.nextName(); // 'self' or 'patp'
                in.beginObject();

                Double totalLitres = null;
                Double totalProfit = null;
                while (in.peek() != JsonToken.END_OBJECT) {
                    final String key = in.nextName();
                    if ("litres".equals(key)) {
                        totalLitres = in.nextDouble();
                    } else if ("profit".equals(key)) {
                        totalProfit = in.nextDouble();
                    }
                }
                if (totalLitres == null || totalProfit == null) {
                    throw new IOException("No litres or profit found.");
                } else {
                    fortechTotals.add(new FortechTotal(Fuel.fromString(fuel),
                            PumpType.fromString(type), totalLitres, totalProfit));
                }
                in.endObject();
            }
            in.endObject();
        }
        in.endObject();

        return fortechTotals;
    }

    @Override
    public void write(JsonWriter out, List<FortechTotal> totals) throws IOException {
        final Map<Fuel, Map<PumpType, FortechTotal>> mapOfMaps = new ArrayMap<>();
        for (FortechTotal total : totals) {
            if (!mapOfMaps.containsKey(total.getFuel())) {
                mapOfMaps.put(total.getFuel(), new ArrayMap<>());
            }
            final Map<PumpType, FortechTotal> map = checkNotNull(mapOfMaps.get(total.getFuel()));
            if (map.containsKey(total.getType())) {
                throw new IOException("Found duplicate for fuel " +
                        total.getFuel() + " and type " + total.getType());
            }
            map.put(total.getType(), total);
        }

        out.beginObject();
        for (Map.Entry<Fuel, Map<PumpType, FortechTotal>> entry : mapOfMaps.entrySet()) {
            out.name(entry.getKey().toString());
            out.beginObject();
            for (Map.Entry<PumpType, FortechTotal> entry1 : entry.getValue().entrySet()) {
                out.name(entry1.getKey().toString());
                out.beginObject();
                out.name("litres");
                out.value(entry1.getValue().getTotalLitres());
                out.name("profit");
                out.value(entry1.getValue().getTotalProfit());
                out.endObject();
            }
            out.endObject();
        }
        out.endObject();
    }
}
