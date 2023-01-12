package it.stazionidesal.desal.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.ui.fragment.LoginFragment;
import it.stazionidesal.desal.ui.fragment.SignupFragment;
import it.stazionidesal.desal.util.logic.Conditions;

import static it.stazionidesal.desal.util.logic.Conditions.checkNotNull;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String mode = Conditions.checkNotNull(getIntent().getStringExtra("mode"));
        if (mode.equals("login")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, new LoginFragment())
                    .commit();
        } else if (mode.equals("signup")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, new SignupFragment())
                    .commit();
        }
    }

    public void switchToLogin() {
        startActivity(new Intent(this, MainActivity.class).putExtra("mode", "login")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void switchToSignUp() {
        startActivity(new Intent(this, MainActivity.class).putExtra("mode", "signup")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }
}
