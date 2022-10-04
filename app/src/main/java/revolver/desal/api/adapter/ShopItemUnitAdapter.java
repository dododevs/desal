package revolver.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import revolver.desal.api.services.inventory.Unit;

public class ShopItemUnitAdapter extends TypeAdapter<Unit> {

    @Override
    public Unit read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return Unit.fromString(in.nextString());
    }

    @Override
    public void write(JsonWriter out, Unit value) throws IOException {
        out.value(value.getSlug());
    }
}
