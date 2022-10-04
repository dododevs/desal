package revolver.desal.ui.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import revolver.desal.R;
import revolver.desal.ui.fragment.ShiftsArchiveListFragment;

public class ShiftsArchiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts_archive);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new ShiftsArchiveListFragment())
                .commit();
    }

}
