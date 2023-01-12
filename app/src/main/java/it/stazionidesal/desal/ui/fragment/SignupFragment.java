package it.stazionidesal.desal.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.Status;
import it.stazionidesal.desal.api.model.response.PendingValidationResponse;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.Keyboards;
import it.stazionidesal.desal.util.ui.Snacks;
import it.stazionidesal.desal.util.ui.ViewUtils;
import it.stazionidesal.desal.view.SimpleTextWatcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.users.UserCredentials;
import it.stazionidesal.desal.api.services.UsersService;
import it.stazionidesal.desal.ui.activity.MainActivity;
import it.stazionidesal.desal.ui.activity.employee.PendingValidationActivity;

public class SignupFragment extends Fragment {

    public static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]{4,}$");
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9,.-;:_!=?^*+]{5,}$");
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[a-zA-Z' ]{3,}$");

    private ProgressBar mLoadingWheel;
    private TextView mConfirmView;
    private FrameLayout mConfirmContainer;

    private CoordinatorLayout mSnackbarContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_signup, container, false);

        final EditText usernameField = v.findViewById(R.id.fragment_signup_username);
        final EditText passwordField = v.findViewById(R.id.fragment_signup_password);
        final EditText fullNameField = v.findViewById(R.id.fragment_signup_full_name);

        mConfirmView = v.findViewById(R.id.fragment_signup_confirm);
        mConfirmContainer = v.findViewById(R.id.fragment_signup_confirm_container);
        mLoadingWheel = v.findViewById(R.id.fragment_signup_loading_wheel);
        mSnackbarContainer = v.findViewById(R.id.fragment_signup_snackbar);

        usernameField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (USERNAME_PATTERN.matcher(s).matches() &&
                        PASSWORD_PATTERN.matcher(passwordField.getText()).matches() &&
                            FULL_NAME_PATTERN.matcher(fullNameField.getText()).matches()) {
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
                if (PASSWORD_PATTERN.matcher(s).matches() &&
                        USERNAME_PATTERN.matcher(usernameField.getText()).matches() &&
                            FULL_NAME_PATTERN.matcher(fullNameField.getText()).matches()) {
                    mConfirmView.setEnabled(true);
                    mConfirmContainer.setEnabled(true);
                } else {
                    mConfirmView.setEnabled(false);
                    mConfirmContainer.setEnabled(false);
                }
            }
        });

        fullNameField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (FULL_NAME_PATTERN.matcher(s).matches() &&
                        PASSWORD_PATTERN.matcher(passwordField.getText()).matches() &&
                            USERNAME_PATTERN.matcher(usernameField.getText()).matches()) {
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
            final String fullName = fullNameField.getText().toString();
            signUp(username, password, fullName);
        });

        final TextView switchToLogin = v.findViewById(R.id.fragment_signup_switch);
        switchToLogin.setOnClickListener(v1 -> getMainActivity().switchToLogin());

        return v;
    }

    private void signUp(final String username, final String password, final String fullName) {
        startLoading();
        getUsersService().createUser(new UserCredentials(username, password, fullName))
                .enqueue(new PendingValidationResponseCallback());
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

    private void startLoading() {
        Keyboards.hideOnWindowAttached(mConfirmView);
        mConfirmView.setVisibility(View.GONE);
        mLoadingWheel.setVisibility(View.VISIBLE);
        mConfirmView.setEnabled(false);
    }

    private void stopLoading() {
        mLoadingWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
        mConfirmView.setEnabled(true);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) requireActivity();
    }

    private UsersService getUsersService() {
        return RestAPI.getUsersService();
    }

    private class PendingValidationResponseCallback implements Callback<PendingValidationResponse> {
        @Override
        public void onResponse(@NonNull Call<PendingValidationResponse> call, Response<PendingValidationResponse> response) {
            stopLoading();
            final PendingValidationResponse body = response.body();
            if (body == null) {
                Snacks.shorter(mSnackbarContainer, R.string.error_generic);
                return;
            }
            if (body.isSuccessful()) {
                Toast.makeText(getContext(), R.string.signup_success, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), PendingValidationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("validation", body.getPendingValidation()));
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
            }
        }

        @Override
        public void onFailure(@NonNull Call<PendingValidationResponse> call, @NonNull Throwable t) {
            stopLoading();
            Snacks.shorter(mSnackbarContainer, R.string.error_generic);
        }
    }
}
