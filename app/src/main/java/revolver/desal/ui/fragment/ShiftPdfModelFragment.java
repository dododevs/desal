package revolver.desal.ui.fragment;

import static android.os.Build.VERSION_CODES.Q;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PdfPrinter;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import revolver.desal.BuildConfig;
import revolver.desal.R;
import revolver.desal.api.RestAPI;
import revolver.desal.api.Status;
import revolver.desal.api.model.response.ShiftsArchiveResponse;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.models.PdfModel;
import revolver.desal.api.services.models.PdfModelCompiler;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.api.services.shifts.ShiftPumpData;
import revolver.desal.api.services.shifts.revision.EstimatedIncomes;
import revolver.desal.api.services.stations.Fuel;
import revolver.desal.api.services.stations.GasPump;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.ui.activity.MainActivity;
import revolver.desal.ui.adapter.ShiftsArchiveAdapter;
import revolver.desal.ui.adapter.TransactionsListAdapter;
import revolver.desal.util.ui.PdfUtils;
import revolver.desal.util.ui.Snacks;

public class ShiftPdfModelFragment extends Fragment
        implements ActivityResultCallback<Boolean> {

    private GasStation mStation;
    private Shift mShift;

    private PdfModel mEmptyPdfModel;
    private PdfModel mPdfModel;

    private CoordinatorLayout mSnackbarContainer;

    private FrameLayout mLoadingView;
    private TextView mConfirmView;
    private FrameLayout mConfirmContainer;
    private ProgressBar mWheel;

    private CardView mOkContainer;
    private CardView mProblemContainer;
    private CardView mDisasterContainer;
    private TextView mSeeRejectedView;

    private CheckBox mDayGrandTotalCheckView;

    private boolean mIncludeDayGrandTotal = false;
    private List<Shift> mSameDayEndedShifts;
    private List<Transaction> mRejectedTransactions;

    private ActivityResultLauncher<String> mPermissionsRequestLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mPermissionsRequestLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                this
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        WebView.enableSlowWholeDocumentDraw();
        final View v = inflater.inflate(R.layout.fragment_shift_pdf_model, container, false);

        if (getArguments() == null || (mStation = getArguments().getParcelable("station")) == null) {
            Toast.makeText(getContext(), R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return null;
        }

        if ((mShift = getArguments().getParcelable("shift")) == null) {
            Toast.makeText(getContext(), R.string.error_shift_invalid, Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return null;
        }

        if ((mEmptyPdfModel = getArguments().getParcelable("model")) == null) {
            Toast.makeText(getContext(), R.string.error_model_invalid, Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return null;
        }

        final Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v13 -> requireActivity().onBackPressed());

        final WebView previewView = v.findViewById(R.id.fragment_shift_pdf_model_preview);
        previewView.getSettings().setUseWideViewPort(true);
        previewView.getSettings().setLoadWithOverviewMode(true);
        previewView.loadDataWithBaseURL(null, mEmptyPdfModel.getPage1(), "text/html", null, null);

        mConfirmView = v.findViewById(R.id.fragment_shift_pdf_model_create_confirm);
        mConfirmView.setOnClickListener(v12 -> startPdfWrite());

        mConfirmContainer = v.findViewById(R.id.fragment_shift_pdf_model_create_confirm_container);
        mOkContainer = v.findViewById(R.id.fragment_shift_pdf_model_ok);
        mProblemContainer = v.findViewById(R.id.fragment_shift_pdf_model_problem_container);
        mDisasterContainer = v.findViewById(R.id.fragment_shift_pdf_model_disaster_container);
        mSeeRejectedView = v.findViewById(R.id.fragment_shift_pdf_model_rejected);
        mDayGrandTotalCheckView = v.findViewById(R.id.fragment_shift_pdf_model_include_grand_total);

        final LinearLayout dayGrandTotalView =
                v.findViewById(R.id.fragment_shift_pdf_model_include_grand_total_container);
        dayGrandTotalView.setOnClickListener(v1 -> {
            mIncludeDayGrandTotal = !mIncludeDayGrandTotal;
            mDayGrandTotalCheckView.setChecked(mIncludeDayGrandTotal);
            if (mIncludeDayGrandTotal && mSameDayEndedShifts == null) {
                startGrandTotalLoading();
            } else if (mIncludeDayGrandTotal && mSameDayEndedShifts.isEmpty()) {
                onNoShiftsAvailableForGrandTotal();
                mIncludeDayGrandTotal = false;
                mDayGrandTotalCheckView.setChecked(false);
            } else {
                startPdfBuild();
            }
        });

        mLoadingView = v.findViewById(R.id.dim);
        mWheel = v.findViewById(R.id.fragment_shift_pdf_model_create_wheel);
        mSnackbarContainer = v.findViewById(R.id.fragment_shift_pdf_model_snackbar);

        startGrandTotalLoading();

        return v;
    }

    private void startLoading() {
        mConfirmView.setVisibility(View.GONE);
        mWheel.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mWheel.setVisibility(View.GONE);
        mConfirmView.setVisibility(View.VISIBLE);
    }

    private void startGrandTotalLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        mSameDayEndedShifts = null;
        getShiftsService().getEndedShiftsForStation(mShift.getSid())
                .enqueue(new DayGrandTotalResponseCallback());
    }

    private void stopGrandTotalLoading() {
        mLoadingView.animate().alpha(0.0f).setDuration(200L).withEndAction(() -> {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1.f);
        }).setStartDelay(100L).start();
    }

    private void onFailedGrandTotalLoading() {
        mIncludeDayGrandTotal = false;
        mDayGrandTotalCheckView.setChecked(false);
    }

    private void onNoShiftsAvailableForGrandTotal() {
        Snacks.normal(mSnackbarContainer,
                getString(R.string.fragment_shift_pdf_model_include_grand_total_empty),
                    getString(R.string.fragment_shift_pdf_model_include_grand_total_empty_why),
                v -> buildWhyThereAreNoShiftsToIncludeDialog().show()
        );
    }

    private AlertDialog buildWhyThereAreNoShiftsToIncludeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                requireContext(), R.style.RoundedAdaptiveDialog);
        final View rootView = View.inflate(getContext(), R.layout.dialog_no_shifts_to_include, null);
        builder.setView(rootView);

        final AlertDialog dialog = builder.create();
        rootView.findViewById(R.id.dialog_no_shifts_to_include_close)
                .setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    private AlertDialog buildRejectedTransactionsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                requireContext(), R.style.RoundedAdaptiveDialog);
        final View rootView = View.inflate(getContext(), R.layout.dialog_rejected_transactions, null);
        builder.setView(rootView);

        final RecyclerView list = rootView.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(new TransactionsListAdapter(mRejectedTransactions, false));

        final AlertDialog dialog = builder.create();
        rootView.findViewById(R.id.dialog_rejected_transactions_close)
                .setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private boolean generateCompiledModel() {
        final PdfModelCompiler compiler = new PdfModelCompiler(getContext(), mEmptyPdfModel);
        try {
            compiler.setStationName(mStation.getName());
            compiler.setDate(new Date(mShift.getStart() * 1000));
            compiler.setEmployeeName(mShift.getFullName());
            compiler.setShiftLabel(String.format(Locale.ITALIAN, "%s - %s",
                    ShiftsArchiveAdapter.sHourFormatter.format(new Date(mShift.getStart() * 1000)),
                    ShiftsArchiveAdapter.sHourFormatter.format(new Date(mShift.getEnd() * 1000))));
            compiler.setPrices(mStation.getPrices(), true);

            for (final GasPump pump : mStation.getPumps()) {
                compiler.setPumpInitialFinalAndDifferenceValue(pump,
                        getPumpDataForPump(pump, mShift.getInitialData().getPumpsData()),
                        getPumpDataForPump(pump, mShift.getEndData().getPumpsData()),
                        getPumpDataForPump(pump, mShift.getDifferenceData().getPumpsData())
                );
            }
            mRejectedTransactions = new ArrayList<>();
            compiler.setRevision(mShift.getRevision(), mRejectedTransactions);
            compiler.setGplClocks(mShift.getInitialData().getGplClock(), mShift.getEndData().getGplClock());
            compiler.setWorkingHours(mShift.getStart(), mShift.getEnd());

            List<Shift> endedTodayShifts = new ArrayList<>(mSameDayEndedShifts);
            if (!mIncludeDayGrandTotal) {
                endedTodayShifts = new ArrayList<>();
            }
            endedTodayShifts.add(mShift);

            final double[] fuelGrandTotal = getGrandTotalForFuels(endedTodayShifts);
            final double[] gplGrandTotal = getGrandTotalForGpl(endedTodayShifts);
            compiler.setGrandTotalForFuel(fuelGrandTotal[0], fuelGrandTotal[1]);
            compiler.setGrandTotalForGpl(gplGrandTotal[0], gplGrandTotal[1]);
            compiler.setGrandTotal(
                    fuelGrandTotal[0] + gplGrandTotal[0],
                    getGrandTotalProfit(endedTodayShifts) + gplGrandTotal[1]
            );

            compiler.setGrandTotalForAccessories(getGrandTotalForAccessories(endedTodayShifts));
            compiler.setGrandTotalForOil(getGrandTotalForOil(endedTodayShifts));
            mPdfModel = compiler.getCompiledModel();
        } catch (Exception e) {
            Log.e("ShiftPdfModelFragment", "failed compiling model", e);
            return false;
        }
        return true;
    }

    private void startPdfWrite() {
        if (Build.VERSION.SDK_INT >= Q || hasWritePermission()) {
            startLoading();
            writePage1ToFile();
        } else {
            requestWritePermission();
        }
    }

    private void startPdfBuild() {
        if (generateCompiledModel()) {
            mDisasterContainer.setVisibility(View.GONE);
            mConfirmView.setEnabled(true);
            mConfirmContainer.setEnabled(true);

            if (mRejectedTransactions.isEmpty()) {
                mOkContainer.setVisibility(View.VISIBLE);
                mProblemContainer.setVisibility(View.GONE);
                mSeeRejectedView.setOnClickListener(null);
            } else {
                mOkContainer.setVisibility(View.GONE);
                mProblemContainer.setVisibility(View.VISIBLE);
                mSeeRejectedView.setOnClickListener(v -> buildRejectedTransactionsDialog().show());
            }
        } else {
            mOkContainer.setVisibility(View.GONE);
            mProblemContainer.setVisibility(View.GONE);
            mSeeRejectedView.setOnClickListener(null);
            mDisasterContainer.setVisibility(View.VISIBLE);
            mConfirmView.setEnabled(false);
            mConfirmContainer.setEnabled(false);
        }
    }

    private void writePage1ToFile() {
        final PdfPrinter.PrintResultCallback callback = new PdfPrinter.PrintResultCallback() {
            @Override
            public void onPrintSucceeded(File output) {
                writePage2ToFile();
            }

            @Override
            public void onPrintFailed(CharSequence error) {
                ShiftPdfModelFragment.this.onPrintFailed();
            }
        };
        if (!PdfUtils.printHtmlToPdf(requireContext(), mPdfModel.getPage1(),
                requireContext().getCacheDir(), "page1.pdf", callback)) {
            onPrintFailed();
        }
    }

    private void writePage2ToFile() {
        final PdfPrinter.PrintResultCallback callback = new PdfPrinter.PrintResultCallback() {
            @Override
            public void onPrintSucceeded(File output) {
                mergePagesAndWriteToFile();
            }

            @Override
            public void onPrintFailed(CharSequence error) {
                Log.d("onPrintFailed", String.valueOf(error));
                ShiftPdfModelFragment.this.onPrintFailed();
            }
        };
        if (!PdfUtils.printHtmlToPdf(requireContext(), mPdfModel.getPage2(),
                requireContext().getCacheDir(), "page2.pdf", callback)) {
            onPrintFailed();
        }
    }

    @RequiresApi(Q)
    private Uri mergePagesAndWriteToFileUsingMediaStore(String filename) {
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        final ContentResolver resolver = requireContext().getContentResolver();
        final Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        try {
            final OutputStream outfile = resolver.openOutputStream(uri);
            if (uri != null) {
                PdfUtils.mergePdfFiles(outfile,
                        new File(requireContext()
                                .getCacheDir().getAbsolutePath() +
                                "/page1.pdf"),
                        new File(requireContext()
                                .getCacheDir().getAbsolutePath() +
                                "/page2.pdf")
                );
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        return uri;
    }

    private void mergePagesAndWriteToFile() {
        final String filename = generateFilename();

        final Uri merged;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            merged = mergePagesAndWriteToFileUsingMediaStore(filename);
            if (merged == null) {
                onPrintFailed();
                return;
            }
        } else {
            final File output = PdfUtils.mergePdfFiles(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), generateFilename(),
                        new File(requireContext().getCacheDir().getAbsolutePath() + "/page1.pdf"),
                            new File(requireContext().getCacheDir().getAbsolutePath() + "/page2.pdf"));
            if (output == null) {
                onPrintFailed();
                return;
            }
            merged = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider", output);
            stopLoading();
        }

        final Intent i = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(merged, "application/pdf")
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (i.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(i);
        } else {
            Toast.makeText(getContext(),
                    getString(R.string.fragment_shift_pdf_model_success), Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
        }
    }

    private void onPrintFailed() {
        stopLoading();
        Snacks.shorter(mSnackbarContainer, R.string.fragment_shift_pdf_model_print_failed);
    }

    private String generateFilename() {
        return getString(R.string.fragment_shift_pdf_model_name,
                mStation.getName().replace(" ", "_"),
                    PdfModelCompiler.sDateFieldFormatter.format(
                            new Date(mShift.getStart() * 1000)).replace("/", "_"));
    }

    private boolean hasWritePermission() {
        return ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWritePermission() {
        mPermissionsRequestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onActivityResult(Boolean result) {
        if (result) {
            writePage1ToFile();
        } else {
            stopLoading();
            Snacks.shorter(mSnackbarContainer,
                    R.string.fragment_shift_pdf_model_no_write_permission);
        }
    }

    private double[] getGrandTotalForFuels(final List<Shift> endedShifts) {
        double[] totals = new double[] { 0.0, 0.0 };
        for (final Shift endedShift : endedShifts) {
            totals[0] += endedShift.getRevision()
                    .getEstimatedIncomes().getFortechTotal().getTotalLitres();
            totals[1] += endedShift.getRevision()
                    .getEstimatedIncomes().getFortechTotal().getTotalProfit();
        }
        return totals;
    }

    private double[] getGrandTotalForGpl(final List<Shift> endedShifts) {
        double totalInLitres = 0.0;
        double totalProfit = 0.0;
        for (final Shift endedShift : endedShifts) {
            for (final ShiftPumpData shiftPumpData : endedShift.getDifferenceData().getPumpsData()) {
                if (isGplPump(shiftPumpData)) {
                    totalInLitres += shiftPumpData.getValue();
                }
            }
            totalProfit += endedShift.getRevision().getEstimatedIncomes().getGplTotal();
        }
        return new double[] { totalInLitres, totalProfit };
    }

    private double getGrandTotalForAccessories(final List<Shift> endedShifts) {
        double total = 0.0;
        for (final Shift endedShift : endedShifts) {
            total += endedShift.getRevision().getEstimatedIncomes().getAccessoriesTotal();
        }
        return total;
    }

    private double getGrandTotalForOil(final List<Shift> endedShifts) {
        double total = 0.0;
        for (final Shift endedShift : endedShifts) {
            total += endedShift.getRevision().getEstimatedIncomes().getOilTotal();
        }
        return total;
    }

    private double getGrandTotalProfit(final List<Shift> endedShifts) {
        double total = 0.0;
        for (final Shift endedShift : endedShifts) {
            final EstimatedIncomes estimatedIncomes = endedShift.getRevision().getEstimatedIncomes();
            total += estimatedIncomes.getFortechTotal().getTotalProfit();
            total += estimatedIncomes.getGplTotal();
            total += estimatedIncomes.getOilTotal();
            total += estimatedIncomes.getAccessoriesTotal();
        }
        return total;
    }

    private static ShiftPumpData getPumpDataForPump(GasPump pump, List<ShiftPumpData> data) {
        for (final ShiftPumpData pumpData : data) {
            if (pump.getPid().equals(pumpData.getPid())) return pumpData;
        }
        return null;
    }

    private boolean isGplPump(ShiftPumpData data) {
        for (final GasPump pump : mStation.getPumps()) {
            if (pump.getPid().equals(data.getPid())) {
                return pump.getAvailableFuel() == Fuel.GPL;
            }
        }
        return false;
    }

    private List<Shift> getTodayEndedShifts(List<Shift> allShifts) {
        final Calendar midnight = new GregorianCalendar();
        midnight.setTimeInMillis(mShift.getStart() * 1000);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        for (int i = allShifts.size() - 1; i >= 0; i--) {
            final Shift shift = allShifts.get(i);
            if (!shift.isDone() || shift.getStart() * 1000 < midnight.getTimeInMillis() ||
                    shift.getStart() > mShift.getStart() || shift.getRid().equals(mShift.getRid()) ||
                        shift.getRevision() == null) {
                allShifts.remove(i);
            }
        }
        return allShifts;
    }

    private ShiftsService getShiftsService() {
        return RestAPI.getShiftsService();
    }

    private class DayGrandTotalResponseCallback implements Callback<ShiftsArchiveResponse> {
        @Override
        public void onResponse(@NonNull Call<ShiftsArchiveResponse> call, Response<ShiftsArchiveResponse> response) {
            stopGrandTotalLoading();

            final ShiftsArchiveResponse body = response.body();
            if (body == null) {
                Snacks.normal(mSnackbarContainer,
                        R.string.fragment_shift_pdf_model_include_grand_total_failed);
                onFailedGrandTotalLoading();
                return;
            }

            if (body.isSuccessful()) {
                mSameDayEndedShifts = getTodayEndedShifts(body.getShifts());
            } else if (body.getStatus() == Status.SESSION_TOKEN_INVALID) {
                Snacks.normal(mSnackbarContainer, Status.getErrorDescription(getContext(), body.getStatus()),
                        getString(R.string.action_login), v -> startActivity(new Intent(getContext(),
                                MainActivity.class).putExtra("mode", "login")));
                onFailedGrandTotalLoading();
            } else {
                Snacks.normal(mSnackbarContainer,
                        Status.getErrorDescription(getContext(), body.getStatus()));
                onFailedGrandTotalLoading();
            }

            startPdfBuild();
        }

        @Override
        public void onFailure(@NonNull Call<ShiftsArchiveResponse> call, @NonNull Throwable t) {
            stopGrandTotalLoading();
            Snacks.normal(mSnackbarContainer,
                    R.string.fragment_shift_pdf_model_include_grand_total_failed);
            onFailedGrandTotalLoading();

            startPdfBuild();
        }
    }
}