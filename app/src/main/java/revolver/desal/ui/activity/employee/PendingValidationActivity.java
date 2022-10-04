package revolver.desal.ui.activity.employee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.DeSal;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.services.QrCodeService;
import revolver.desal.api.services.qr.Size;
import revolver.desal.api.services.users.PendingValidation;
import revolver.desal.util.ui.M;
import revolver.desal.util.ui.Snacks;

public class PendingValidationActivity extends AppCompatActivity {

    private PendingValidation mPendingValidation;
    private ImageView mQrCodeView;

    private TextView mRetryView;
    private ProgressBar mLoadingView;
    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_validation);

        mPendingValidation = getIntent().getParcelableExtra("validation");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finishAndRemoveTask());

        final ImageView copyButton = findViewById(R.id.activity_pending_validation_url_copy);
        copyButton.setOnClickListener(v -> {
            saveToClipboard(buildValidationUrl());
            Toast.makeText(PendingValidationActivity.this,
                    R.string.activity_pending_validation_copy_done, Toast.LENGTH_SHORT).show();
        });

        final ImageView shareButton = findViewById(R.id.activity_pending_validation_url_share);
        shareButton.setOnClickListener(v -> {
            final Intent i = new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.activity_pending_validation_share))
                    .putExtra(Intent.EXTRA_TEXT, buildValidationUrl());
            startActivity(Intent.createChooser(i, getString(R.string.activity_pending_validation_share)));
        });

        final TextView urlView = findViewById(R.id.activity_pending_validation_url);
        urlView.setText(buildSpannedValidationUrl());

        mLoadingView = findViewById(R.id.activity_pending_validation_qr_wheel);
        mRetryView = findViewById(R.id.activity_pending_validation_qr_retry);
        mRetryView.setOnClickListener(v -> requestQrCode());

        mQrCodeView = findViewById(R.id.activity_pending_validation_qr);
        mSnackbarContainer = findViewById(R.id.activity_pending_validation);

        requestQrCode();
    }

    private void requestQrCode() {
        startLoading();
        getQrService().generateQrCodeForUrl(
                new Size(M.dp(192.f).intValue(), M.dp(192.f).intValue()), buildValidationUrl())
                    .enqueue(new QrCodeResponseCallback());
    }

    private void startLoading() {
        mRetryView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingView.setVisibility(View.GONE);
    }

    private Spanned buildSpannedValidationUrl() {
        return Html.fromHtml(String.format(Locale.ITALIAN, "%susers/verify/<b>%s</b>",
                DeSal.API_URL, mPendingValidation.getToken()));
    }

    private String buildValidationUrl() {
        return String.format(Locale.ITALIAN, "%susers/verify/%s",
                DeSal.API_URL, mPendingValidation.getToken());
    }

    private void saveToClipboard(String url) {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(url, url));
    }

    private QrCodeService getQrService() {
        return RestAPI.getQrCodeService();
    }

    private class QrCodeResponseCallback implements Callback<Bitmap> {
        @Override
        public void onResponse(@NonNull Call<Bitmap> call, Response<Bitmap> response) {
            stopLoading();

            final Bitmap qr = response.body();
            if (qr == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                mRetryView.setVisibility(View.VISIBLE);
                return;
            }
            mQrCodeView.setImageBitmap(qr);
        }

        @Override
        public void onFailure(@NonNull Call<Bitmap> call, @NonNull Throwable t) {
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
            mRetryView.setVisibility(View.VISIBLE);
        }
    }
}
