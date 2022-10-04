package revolver.desal.view;

import android.content.Context;
import android.print.PdfPrinter;
import android.util.AttributeSet;
import android.webkit.WebView;

import java.io.File;

public class PrintableWebView extends WebView {

    private File mOutputPath;
    private String mOutputName;
    private File mOutput;

    public PrintableWebView(Context context) {
        super(context);
    }

    public PrintableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrintableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void renderToPdf(final File path, final String name, PdfPrinter.PrintResultCallback callback) {
        PdfPrinter.print(createPrintDocumentAdapter(name), path.getAbsolutePath(), name, callback);
    }
}
