package revolver.desal.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import revolver.desal.R;

public class DeSalProgressDialog extends Dialog {

    private final TextView mTextView;

    public DeSalProgressDialog(Context context) {
        super(context);

        final View v = View.inflate(context, R.layout.dialog_progress, null);
        mTextView = v.findViewById(R.id.progress_dialog_label);
        setContentView(v);
    }

    public DeSalProgressDialog label(String label) {
        mTextView.setText(label);
        return this;
    }

    public static DeSalProgressDialog create(Context context, String text) {
        return new DeSalProgressDialog(context).label(text);
    }

}
