package it.stazionidesal.desal.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.StationsResponse;
import it.stazionidesal.desal.api.services.users.Session;
import it.stazionidesal.desal.ui.activity.employee.EmployeeMainActivity;
import it.stazionidesal.desal.ui.activity.employee.PendingValidationActivity;
import it.stazionidesal.desal.ui.activity.owner.OwnerMainActivity;
import it.stazionidesal.desal.util.ui.IconUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.users.UserType;
import it.stazionidesal.desal.api.services.stations.GasStation;

public class LoadingActivity extends AppCompatActivity {

    private UserType mUserType;
    public static boolean sAlreadySeen = false;

    private LinearLayout mNoStationsContainer;
    private LinearLayout mLogoutContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        final ProgressBar wheel = findViewById(R.id.wheel);
        wheel.postDelayed(() -> {
            wheel.setVisibility(View.VISIBLE);
            startBootRoutine();
        }, 750);

        final TextView retryView = findViewById(R.id.activity_loading_retry);
        retryView.setOnClickListener(v -> startBootRoutine());

        final Drawable retryIcon = IconUtils.drawableWithResolvedColor(
                this, R.drawable.ic_refresh, R.color.colorPrimaryDark);
        retryView.setCompoundDrawablesWithIntrinsicBounds(retryIcon, null, null, null);

        mNoStationsContainer = findViewById(R.id.activity_loading_no_stations_container);

        mLogoutContainer = findViewById(R.id.activity_loading_logout_container);
        mLogoutContainer.setOnClickListener(v -> onNewLoginNeeded());

        sAlreadySeen = false;
    }

    private void startBootRoutine() {
        mNoStationsContainer.setVisibility(View.GONE);
        mLogoutContainer.setVisibility(View.GONE);
        if (DeSal.isPersistentSessionAvailable(this)) {
            final Session session = DeSal.getPersistentSession(this);
            if (session != null) {
                mUserType = session.getUserType();
                RestAPI.setCurrentSession(session);
                RestAPI.getSessionService().checkSession().enqueue(new CheckSessionCallback());
            } else {
                onNewLoginNeeded();
            }
        } else {
            onNewLoginNeeded();
        }
    }

    private void onNewLoginNeeded() {
        startActivity(new Intent(this, MainActivity.class).putExtra("mode", "login")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void onSignupNeeded() {
        startActivity(new Intent(this, MainActivity.class).putExtra("mode", "signup")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void onSuccessfulAuthenticatedRequest(List<GasStation> stations) {
        if (mUserType == UserType.EMPLOYEE) {
            if (stations.isEmpty()) {
                onEmptyStationsList();
            } else {
                startActivity(new Intent(this, EmployeeMainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putParcelableArrayListExtra("stations", new ArrayList<>(stations)));
            }
        } else if (mUserType == UserType.OWNER) {
            startActivity(new Intent(this, OwnerMainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putParcelableArrayListExtra("stations", new ArrayList<>(stations)));
        }
    }

    private void onFailedAuthenticatedRequest() {
        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show();
        finishAndRemoveTask();
    }

    private void onEmptyStationsList() {
        mNoStationsContainer.setVisibility(View.VISIBLE);
        mLogoutContainer.setVisibility(View.VISIBLE);
    }

    private class CheckSessionCallback implements Callback<Session> {
        @Override
        public void onResponse(@NonNull Call<Session> call, Response<Session> response) {
            final Session body = response.body();
            if (body == null) {
                onFailedAuthenticatedRequest();
                return;
            }
            if (body.isSuccessful()) {
                if (body.isVerified()) {
                    RestAPI.getStationsService().getStations().enqueue(new InitialStationsListCallback());
                } else {
                    startActivity(new Intent(LoadingActivity.this, PendingValidationActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("validation", body.getValidation()));
                }
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Toast.makeText(LoadingActivity.this, Status.getErrorDescription(
                        LoadingActivity.this, body.getStatus()), Toast.LENGTH_SHORT).show();
                onNewLoginNeeded();
            } else if (body.getStatus() == Status.VALIDATION_TOKEN_INVALID ||
                    body.getStatus() == Status.VALIDATION_TOKEN_EXPIRED) {
                Toast.makeText(LoadingActivity.this, Status.getErrorDescription(
                        LoadingActivity.this, body.getStatus()), Toast.LENGTH_SHORT).show();
                onSignupNeeded();
            } else {
                Toast.makeText(LoadingActivity.this, Status.getErrorDescription(
                        LoadingActivity.this, body.getStatus()), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<Session> call, Throwable t) {
            onFailedAuthenticatedRequest();
        }
    }

    private class InitialStationsListCallback implements Callback<StationsResponse> {
        @Override
        @EverythingIsNonNull
        public void onResponse(Call<StationsResponse> call, Response<StationsResponse> response) {
            final StationsResponse body = response.body();
            if (body == null) {
                onFailedAuthenticatedRequest();
            } else {
                if (body.isSuccessful()) {
                    onSuccessfulAuthenticatedRequest(body.getStations());
                } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                    Toast.makeText(LoadingActivity.this,
                            R.string.error_session_invalid, Toast.LENGTH_LONG).show();
                    onNewLoginNeeded();
                } else {
                    onFailedAuthenticatedRequest();
                }
            }
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<StationsResponse> call, Throwable t) {
            onFailedAuthenticatedRequest();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sAlreadySeen) {
            // Toast.makeText(this, "Already been here, closing...", Toast.LENGTH_SHORT).show();
            finishAndRemoveTask();
            sAlreadySeen = false;
        } else {
            sAlreadySeen = true;
        }
    }
}
