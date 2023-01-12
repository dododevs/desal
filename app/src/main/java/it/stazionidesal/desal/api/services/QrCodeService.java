package it.stazionidesal.desal.api.services;

import android.graphics.Bitmap;

import it.stazionidesal.desal.api.services.qr.Size;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QrCodeService {

    @GET("create-qr-code")
    Call<Bitmap> generateQrCodeForUrl(@Query("size") Size size, @Query("data") String data);

}
