package it.stazionidesal.desal.api.adapter;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Locale;

import it.stazionidesal.desal.api.services.qr.Size;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class QrCodeSizeFactoryAdapter extends Converter.Factory {

    @Override
    public Converter<Size, String> stringConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        if (type != Size.class) {
            return null;
        }
        return value -> String.format(Locale.getDefault(),
                "%dx%d", value.getWidth(), value.getHeight());
    }
}
