package revolver.desal.ui.activity.owner;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.DeSal;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.services.ValidationService;
import revolver.desal.api.services.users.Session;
import revolver.desal.api.services.users.UserType;
import revolver.desal.ui.activity.LoadingActivity;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.util.ui.Snacks;

public class OwnerValidationActivity extends AppCompatActivity {

    private TextInputEditText mUrlField;
    private ProgressBar mLoadingWheel;
    private TextView mConfirmView;
    private CoordinatorLayout mSnackbarContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_validation);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        final Session session;
        if (!DeSal.isPersistentSessionAvailable(this) || (session = DeSal.getPersistentSession(this)) == null) {
            Toast.makeText(this, R.string.error_no_session_available, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OwnerValidationActivity.this, MainActivity.class)
                    .putExtra("mode", "login")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }

        if (session.getUserType() != UserType.OWNER) {
            Toast.makeText(this, R.string.error_wrong_user_type, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OwnerValidationActivity.this, MainActivity.class)
                    .putExtra("mode", "login")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }

        final CheckBox disclaimerBox = findViewById(R.id.activity_owner_validation_checkbox);

        mUrlField = findViewById(R.id.activity_owner_validation_url);
        if (getIntent().getDataString() != null) {
            mUrlField.setText(getIntent().getDataString());
        }

        mLoadingWheel = findViewById(R.id.activity_owner_validation_confirm_wheel);
        mConfirmView = findViewById(R.id.activity_owner_validation_confirm);
        mConfirmView.setOnClickListener(v -> {
            if (disclaimerBox.isChecked()) {
                validateUserOrComplain();
            } else {
                Snacks.shorter(mSnackbarContainer, R.string.activity_owner_validation_not_checked);
            }
        });

        mSnackbarContainer = findViewById(R.id.activity_owner_validation_snackbar);
    }

    private void validateUserOrComplain() {
        final String url;
        if (mUrlField.getText() != null && mUrlField.getText().length() > 0) {
            url = mUrlField.getText().toString();
        } else {
            mUrlField.setError(getString(R.string.activity_owner_validation_url_invalid));
            Snacks.shorter(mSnackbarContainer, R.string.error_not_all_fields_are_filled);
            return;
        }

        final String validationToken = getValidationToken(url);
        if (validationToken == null || validationToken.length() != 128) {
            Snacks.normal(mSnackbarContainer, R.string.error_validation_invalid);
            return;
        }
        startLoading();
        getValidationService().validateUserWithToken(validationToken)
                .enqueue(new UserVerificationResponseCallback());
    }

    private void startLoading() {
        mConfirmView.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    private String getValidationToken(String url) {
        return url != null ? url.length() > 49 ? url.substring(49) : null : null;
    }

    private ValidationService getValidationService() {
        return RestAPI.getValidationService();
    }

    private class UserVerificationResponseCallback implements Callback<BaseResponse> {
        @Override
        public void onResponse(@NonNull Call<BaseResponse> call, Response<BaseResponse> response) {
            stopLoading();

            final BaseResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(OwnerValidationActivity.this,
                        R.string.activity_owner_validation_success, Toast.LENGTH_SHORT).show();
                LoadingActivity.sAlreadySeen = false;
                startActivity(new Intent(OwnerValidationActivity.this, LoadingActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            } else {
                Snacks.normal(mSnackbarContainer, Status
                        .getErrorDescription(OwnerValidationActivity.this, body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
