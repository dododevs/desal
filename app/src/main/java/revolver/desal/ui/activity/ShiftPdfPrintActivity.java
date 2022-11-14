package revolver.desal.ui.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import revolver.desal.R;
import revolver.desal.api.services.models.PdfModel;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.fragment.ShiftPdfModelFragment;

public class ShiftPdfPrintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_pdf_print);

        final GasStation station = getIntent().getParcelableExtra("station");
        if (station == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Shift shift = getIntent().getParcelableExtra("shift");
        if (shift == null) {
            Toast.makeText(this, R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final PdfModel model = getIntent().getParcelableExtra("model");
        if (model == null) {
            Toast.makeText(this, R.string.error_model_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Bundle args = new Bundle();
        args.putParcelable("station", station);
        args.putParcelable("shift", shift);
        args.putParcelable("model", model);

        final ShiftPdfModelFragment fragment = new ShiftPdfModelFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment)
                .commit();
    }
}
