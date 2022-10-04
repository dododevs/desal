package revolver.desal.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import revolver.desal.DeSal;
import revolver.desal.R;
import revolver.desal.api.services.users.Session;
import revolver.desal.ui.activity.MainActivity;

public class UserDetailFragment extends DialogFragment {

    private TextView mCloseButton;

    private TextView mLogoutButton;
    private LinearLayout mLogoutContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_user_detail, container, false);

        final Session session;
        if (!DeSal.isPersistentSessionAvailable(getContext()) ||
                (session = DeSal.getPersistentSession(getContext())) == null) {
            dismiss();
            return null;
        }

        final TextView usernameView = v.findViewById(R.id.fragment_user_detail_username);
        usernameView.setText(String.format(Locale.ITALIAN, "@%s", session.getUsername()));

        final TextView userTypeView = v.findViewById(R.id.fragment_user_detail_type);
        userTypeView.setText(session.getUserType().getStringResource());

        mLogoutButton = v.findViewById(R.id.fragment_user_detail_logout);
        mLogoutButton.setOnClickListener(new OnLogoutButtonClickedListener());

        mCloseButton = v.findViewById(R.id.fragment_user_detail_close);
        mCloseButton.setOnClickListener(v1 -> dismiss());

        mLogoutContainer = v.findViewById(R.id.fragment_user_detail_logout_container);

        return v;
    }

    private void logout() {
        mCloseButton.setVisibility(View.GONE);
        mLogoutContainer.setVisibility(View.VISIBLE);

        mLogoutButton.setText(R.string.fragment_user_detail_logout_confirmation_question);
        mLogoutButton.setOnClickListener(null);

        final TextView yesButton = mLogoutContainer.findViewById(R.id.fragment_user_detail_logout_confirm);
        yesButton.setOnClickListener(v -> onLogoutConfirmed());

        final TextView noButton = mLogoutContainer.findViewById(R.id.fragment_user_detail_logout_cancel);
        noButton.setOnClickListener(v -> onLogoutAborted());
    }

    private void onLogoutConfirmed() {
        DeSal.removePersistentSession(getContext());
        startActivity(new Intent(getContext(), MainActivity.class).putExtra("mode", "login")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void onLogoutAborted() {
        mLogoutContainer.setVisibility(View.GONE);
        mCloseButton.setVisibility(View.VISIBLE);
        mLogoutButton.setText(R.string.fragment_user_detail_logout);
        mLogoutButton.setOnClickListener(new OnLogoutButtonClickedListener());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(requireContext(), R.style.RoundedAdaptiveDialog);
    }

    private class OnLogoutButtonClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            logout();
        }
    }
}
