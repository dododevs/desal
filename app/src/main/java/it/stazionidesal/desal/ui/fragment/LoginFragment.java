package it.stazionidesal.desal.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.StationsResponse;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.Snacks;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.view.SimpleTextWatcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.users.Session;
import it.stazionidesal.desal.api.services.users.UserType;
import it.stazionidesal.desal.api.services.StationsService;
import it.stazionidesal.desal.api.services.UsersService;
import it.stazionidesal.desal.ui.activity.MainActivity;
import it.stazionidesal.desal.ui.activity.employee.EmployeeMainActivity;
import it.stazionidesal.desal.ui.activity.employee.PendingValidationActivity;
import it.stazionidesal.desal.ui.activity.owner.OwnerMainActivity;

public class LoginFragment extends Fragment implements Callback<Session> {

    private TextView mConfirmView;
    private FrameLayout mConfirmContainer;
    private ProgressBar mLoadingWheel;

    private CoordinatorLayout mSnackbarContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_login, container, false);

        final EditText usernameField = v.findViewById(R.id.fragment_login_username);
        final EditText passwordField = v.findViewById(R.id.fragment_login_password);

        mConfirmView = v.findViewById(R.id.fragment_login_confirm);
        mConfirmContainer = v.findViewById(R.id.fragment_login_confirm_container);
        mLoadingWheel = v.findViewById(R.id.fragment_login_loading_wheel);
        mSnackbarContainer = v.findViewById(R.id.fragment_login_snackbar);

        usernameField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (SignupFragment.USERNAME_PATTERN.matcher(s).matches() &&
                        SignupFragment.PASSWORD_PATTERN.matcher(passwordField.getText()).matches()) {
                    mConfirmView.setEnabled(true);
                    mConfirmContainer.setEnabled(true);
                } else {
                    mConfirmView.setEnabled(false);
                    mConfirmContainer.setEnabled(false);
                }
            }
        });

        passwordField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (SignupFragment.PASSWORD_PATTERN.matcher(s).matches() &&
                        SignupFragment.USERNAME_PATTERN.matcher(usernameField.getText()).matches()) {
                    mConfirmView.setEnabled(true);
                    mConfirmContainer.setEnabled(true);
                } else {
                    mConfirmView.setEnabled(false);
                    mConfirmContainer.setEnabled(false);
                }
            }
        });

        mConfirmView.setOnClickListener(v12 -> {
            final String username = usernameField.getText().toString();
            final String password = passwordField.getText().toString();
            login(username, password);
        });

        final TextView switchToSignUp = v.findViewById(R.id.fragment_login_switch);
        switchToSignUp.setOnClickListener(v1 -> getMainActivity().switchToSignUp());

        return v;
    }

    private void login(final String username, final String password) {
        beginLoading();
        getUsersService().login(username, password).enqueue(this);
    }

    private void beginLoading() {
        mConfirmView.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(@NonNull Call<Session> call, Response<Session> response) {
        stopLoading();

        final Session session = response.body();
        if (session == null) {
            Snacks.normal(mSnackbarContainer, R.string.error_generic);
            return;
        }
        if (session.isSuccessful()) {
            Log.d("session", "successful");
            if (!session.isVerified()) {
                Log.d("session", "not verified");
                startActivity(new Intent(getContext(), PendingValidationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("validation", session.getValidation()));
                return;
            }
            Log.d("session", "verified");
            RestAPI.setCurrentSession(session);
            DeSal.persistSession(getContext(), session);
            getStationsService().getStations().enqueue(new Callback<StationsResponse>() {
                @Override
                public void onResponse(@NonNull Call<StationsResponse> call, @NonNull Response<StationsResponse> response) {
                    final StationsResponse body = response.body();
                    if (body != null && body.isSuccessful()) {
                        if (session.getUserType() == UserType.EMPLOYEE) {
                            startActivity(new Intent(getContext(), EmployeeMainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putParcelableArrayListExtra("stations", new ArrayList<>(body.getStations())));
                        } else if (session.getUserType() == UserType.OWNER) {
                            startActivity(new Intent(getContext(), OwnerMainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putParcelableArrayListExtra("stations", new ArrayList<>(body.getStations())));
                        }
                    } else {
                        DeSal.removePersistentSession(getContext());
                        Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StationsResponse> call, @NonNull Throwable t) {
                    Log.w("getStations", t);
                    DeSal.removePersistentSession(getContext());
                    Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                }
            });
        } else {
            Snacks.shorter(mSnackbarContainer,
                    Status.getErrorDescription(getContext(), session.getStatus()));
        }
    }

    @Override
    public void onFailure(@NonNull Call<Session> call, @NonNull Throwable t) {
        stopLoading();
        Log.w("login", t);
        Snacks.shorter(mSnackbarContainer, R.string.error_generic);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Activity activity = getActivity();
        if (activity != null) {
            ViewUtils.setStatusBarColor(activity,
                    ColorUtils.get(getContext(), R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final Activity activity = getActivity();
        if (activity != null) {
            ViewUtils.setStatusBarColor(activity, ColorUtils.get(getContext(), R.color.midGray));
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) requireActivity();
    }

    private UsersService getUsersService() {
        return RestAPI.getUsersService();
    }

    private StationsService getStationsService() {
        return RestAPI.getStationsService();
    }
}
