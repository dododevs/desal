package revolver.desal.api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import revolver.desal.api.services.users.UserType;

public class UserTypeAdapter extends TypeAdapter<UserType> {

    @Override
    public UserType read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            return null;
        }
        return UserType.fromString(in.nextString());
    }

    @Override
    public void write(JsonWriter out, UserType value) throws IOException {
        out.value(value.toString());
    }
}
