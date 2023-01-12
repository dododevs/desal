package it.stazionidesal.desal.util.ui;

import android.content.Context;
import android.print.PdfPrinter;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import it.stazionidesal.desal.view.PrintableWebView;

public final class PdfUtils {

    public static boolean printHtmlToPdf(final Context context, String data,
                                         final File path, final String name,
                                         final PdfPrinter.PrintResultCallback callback) {
        if (context == null || path == null || name == null) {
            return false;
        }
        final File output = new File(path.getAbsolutePath() + "/" + name);
        try {
            if (!output.exists() && !output.createNewFile()) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        final PrintableWebView renderer = new PrintableWebView(context);
        renderer.getSettings().setUseWideViewPort(true);
        renderer.getSettings().setLoadWithOverviewMode(true);
        renderer.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                renderer.renderToPdf(path, name, callback);
            }
        });
        renderer.loadDataWithBaseURL(null, data, "text/html", null, null);
        return true;
    }

    public static File mergePdfFiles(final File outPath, String outName, File... pdfs) {
        final PDFMergerUtility merger = new PDFMergerUtility();
        for (File pdf : pdfs) {
            try {
                merger.addSource(pdf);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        final File output = new File(outPath.getAbsolutePath() + "/" + outName);
        merger.setDestinationFileName(output.getAbsolutePath());
        try {
            merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
        } catch (IOException e) {
            return null;
        }
        return output;
    }

    public static boolean mergePdfFiles(final OutputStream outStream, File... pdfs) {
        final PDFMergerUtility merger = new PDFMergerUtility();
        for (File pdf : pdfs) {
            try {
                merger.addSource(pdf);
            } catch (FileNotFoundException e) {
                return false;
            }
        }
        merger.setDestinationStream(outStream);
        try {
            merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
