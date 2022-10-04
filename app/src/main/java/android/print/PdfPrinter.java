package android.print;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PdfPrinter {

    public static void print(final PrintDocumentAdapter adapter, final String path,
                             final String name, final PrintResultCallback callback) {
        final File output = new File(path + "/" + name);
        final ParcelFileDescriptor fd = buildParcelFileDescriptor(output);
        if (fd == null) {
            if (callback != null) {
                callback.onPrintFailed("fd == null");
            }
            return;
        }
        adapter.onLayout(null, new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                        .setMinMargins(new PrintAttributes.Margins(1000, 1000, 1000, 1000)).build(),
                new CancellationSignal(), new PrintDocumentAdapter.LayoutResultCallback() {
                    @Override
                    public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                        adapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, fd, new CancellationSignal(),
                                new PrintDocumentAdapter.WriteResultCallback() {
                                    @Override
                                    public void onWriteFinished(PageRange[] pages) {
                                        if (callback != null) {
                                            callback.onPrintSucceeded(output);
                                        }
                                    }

                                    @Override
                                    public void onWriteFailed(CharSequence error) {
                                        if (callback != null) {
                                            callback.onPrintFailed(error);
                                        }
                                    }
                                });
                    }
                }, null
        );
    }

    private static ParcelFileDescriptor buildParcelFileDescriptor(File output) {
        if (!output.exists()) {
            try {
                if (!output.createNewFile()) return null;
            } catch (IOException e) {
                return null;
            }
        }
        try {
            return ParcelFileDescriptor.open(output, ParcelFileDescriptor.MODE_WRITE_ONLY);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public interface PrintResultCallback {
        void onPrintSucceeded(final File output);
        void onPrintFailed(CharSequence error);
    }

}
