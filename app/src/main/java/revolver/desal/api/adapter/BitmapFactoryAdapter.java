package revolver.desal.api.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class BitmapFactoryAdapter extends Converter.Factory {

    @Override
    public Converter<ResponseBody, Bitmap> responseBodyConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        if (type != Bitmap.class) {
            return null;
        }
        return value -> BitmapFactory.decodeStream(value.byteStream());
    }
}
