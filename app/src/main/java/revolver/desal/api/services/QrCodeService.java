package revolver.desal.api.services;

import android.graphics.Bitmap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import revolver.desal.api.services.qr.Size;

public interface QrCodeService {

    @GET("create-qr-code")
    Call<Bitmap> generateQrCodeForUrl(@Query("size") Size size, @Query("data") String data);

}
