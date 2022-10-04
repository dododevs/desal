package revolver.desal.api.services.models;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import revolver.desal.api.services.inventory.Item;
import revolver.desal.api.services.inventory.Unit;
import revolver.desal.api.services.shifts.GplClockData;
import revolver.desal.api.services.shifts.ShiftPumpData;
import revolver.desal.api.services.shifts.revision.ActualIncomes;
import revolver.desal.api.services.shifts.revision.EstimatedIncomes;
import revolver.desal.api.services.shifts.revision.FortechTotal;
import revolver.desal.api.services.shifts.revision.Revision;
import revolver.desal.api.services.stations.Fuel;
import revolver.desal.api.services.stations.GasPrice;
import revolver.desal.api.services.stations.GasPump;
import revolver.desal.api.services.stations.PumpType;
import revolver.desal.api.services.transactions.Transaction;
import revolver.desal.api.services.transactions.model.CouponPayment;
import revolver.desal.api.services.transactions.model.CreditCardPayment;
import revolver.desal.api.services.transactions.model.Deposit;
import revolver.desal.api.services.transactions.model.Sale;
import revolver.desal.api.services.transactions.model.Whatnot;
import revolver.desal.util.ui.TextUtils;

public class PdfModelCompiler {

    public static final SimpleDateFormat sDateFieldFormatter =
            new SimpleDateFormat("dd/MM/yy", Locale.ITALIAN);

    private final Context context;
    private final PdfModel pdfModel;

    private final Document page1;
    private final Document page2;

    private final Elements fuelPumps;

    private final Element stationNameField;
    private final Element employeeNameField;
    private final Element dateField;
    private final Element shiftField;
    private final Element workingHoursField;

    private final Elements oilFields;
    private final Elements accessoriesFields;
    private final Elements depositFields;
    private final Elements whatnotFields;

    private final Elements creditCardTransactionFields;
    private final Elements couponPaymentTransactionFields;

    private final Element gplClockInitial;
    private final Element gplClockEnd;

    public PdfModelCompiler(Context context, PdfModel model) {
        this.context = context;
        this.pdfModel = model;
        this.page1 = Jsoup.parse(model.getPage1());
        this.page2 = Jsoup.parse(model.getPage2());

        this.stationNameField = page1.selectFirst("#station-name");
        this.employeeNameField = page1.selectFirst("#employee");
        this.dateField = page1.selectFirst("#date");
        this.shiftField = page1.selectFirst("#shift");
        this.workingHoursField = page1.selectFirst("td#working-hours");

        this.fuelPumps = page1.select("tr.pump");

        this.oilFields = page1.select("#oil td");
        this.accessoriesFields = page1.select("#accessories td");
        this.depositFields = page1.select("#deposits td");
        this.whatnotFields = page1.select("#whatnot td");

        this.creditCardTransactionFields = page2.select(".credit-card");
        this.couponPaymentTransactionFields = page2.select(".coupon-payment");

        this.gplClockInitial = page1.selectFirst("#gpl-clocks-initial");
        this.gplClockEnd = page1.selectFirst("#gpl-clocks-end");
    }

    public void setStationName(String name) {
        this.stationNameField.text(name);
    }

    public void setEmployeeName(String name) {
        this.employeeNameField.appendText(TextUtils.capitalizeWords(name));
    }

    public void setDate(Date date) {
        this.dateField.appendText(sDateFieldFormatter.format(date));
    }

    public void setShiftLabel(String label) {
        this.shiftField.appendText(TextUtils.capitalizeFirst(label));
    }

    public void setWorkingHours(long start, long end) {
        int hours = ((int) (end - start)) / 60 / 60;
        int minutes = (((int) (end - start)) - (hours * 60 * 60)) / 60;
        this.workingHoursField.text(
                String.format(Locale.ITALIAN, "%02d:%02d", hours, minutes));
    }

    public void setPumpInitialFinalAndDifferenceValue(final GasPump pump,
                                                      final ShiftPumpData initialData,
                                                      final ShiftPumpData endData,
                                                      final ShiftPumpData differenceData) {
        if (pump.getAvailableFuel() == Fuel.GPL) {
            setGplPumpInitialFinalAndDifferenceValue(pump, initialData, endData, differenceData);
            return;
        }
        this.fuelPumps.select(String.format(
                Locale.ITALIAN, "[fuel=%s][type=%s][display=%s] #initial",
                    pump.getAvailableFuel().toString(),
                        pump.getType().toString(),
                            pump.getDisplay())).first()
                .text(String.format(Locale.ITALIAN, "%.2f", initialData.getValue()));
        this.fuelPumps.select(String.format(
                Locale.ITALIAN, "[fuel=%s][type=%s][display=%s] #end",
                    pump.getAvailableFuel().toString(),
                        pump.getType().toString(),
                            pump.getDisplay())).first()
                .text(String.format(Locale.ITALIAN, "%.2f", endData.getValue()));
        this.fuelPumps.select(String.format(
                Locale.ITALIAN, "[fuel=%s][type=%s][display=%s] #difference",
                    pump.getAvailableFuel().toString(),
                        pump.getType().toString(),
                            pump.getDisplay())).first()
                .text(String.format(Locale.ITALIAN, "%.2f", differenceData.getValue()));
    }

    private void setGplPumpInitialFinalAndDifferenceValue(final GasPump pump,
                                                          final ShiftPumpData initialData,
                                                          final ShiftPumpData endData,
                                                          final ShiftPumpData differenceData) {
        this.page1.selectFirst(String.format(Locale.ITALIAN, "#initial.gpl-%s", pump.getDisplay()))
                .text(formatTwoFractionDigitsNumber(initialData.getValue()));
        this.page1.selectFirst(String.format(Locale.ITALIAN, "#end.gpl-%s", pump.getDisplay()))
                .text(formatTwoFractionDigitsNumber(endData.getValue()));
        this.page1.selectFirst(String.format(Locale.ITALIAN, "#difference.gpl-%s", pump.getDisplay()))
                .text(formatTwoFractionDigitsNumber(differenceData.getValue()));
    }

    public void setGplClocks(final List<GplClockData> initialData, final List<GplClockData> endData) {
        for (final GplClockData data : initialData) {
            this.gplClockInitial.appendText(formatTwoFractionDigitsNumber(data.getValue()));
            this.gplClockInitial.appendText(" ");
        }
        for (final GplClockData data : endData) {
            this.gplClockEnd.appendText(formatTwoFractionDigitsNumber(data.getValue()));
            this.gplClockEnd.appendText(" ");
        }
    }

    public void setPrices(final List<GasPrice> prices, boolean selfOnly) {
        for (final GasPrice price : prices) {
            if (price.getType() == PumpType.PATP && price.getFuel() != Fuel.GPL && selfOnly) {
                continue;
            }
            this.page1.selectFirst(String.format(Locale.ITALIAN,
                    "#price-%s", price.getFuel().toString()))
                        .appendText(String.format(Locale.ITALIAN, " %.3f", price.getPrice()))
                    .appendText(price.getFuel() != Fuel.GPL && selfOnly ? "+ 0.10€" : "");
        }
    }

    public void setRevision(final Revision revision, List<Transaction> rejectedTransactions) {
        if (rejectedTransactions == null) {
            rejectedTransactions = new ArrayList<>();
        }
        for (final Transaction transaction : revision.getTransactions()) {
            if (!addTransaction(transaction)) rejectedTransactions.add(transaction);
        }

        final EstimatedIncomes estimatedIncomes = revision.getEstimatedIncomes();

        this.page1.selectFirst("#total-fund-initial")
                .text(formatEuroPrice(estimatedIncomes.getInitialFund()));
        for (final FortechTotal total : estimatedIncomes.getFortechTotals()) {
            this.page1.selectFirst(String.format(Locale.ITALIAN, ".total-fuel-litres[fuel=%s][type=%s]",
                    total.getFuel().toString(), total.getType().toString()))
                        .text(formatTwoFractionDigitsNumber(total.getTotalLitres()));
            this.page1.selectFirst(String.format(Locale.ITALIAN, ".total-fuel-profit[fuel=%s][type=%s]",
                    total.getFuel().toString(), total.getType().toString()))
                        .text(formatTwoFractionDigitsNumber(total.getTotalProfit()));
        }

        this.page1.selectFirst("#total-oil-profit")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getOilTotal()));
        this.page1.selectFirst("#grand-total-oil-profit")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getOilTotal()));
        this.page1.selectFirst("#total-accessories")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getAccessoriesTotal()));
        this.page1.selectFirst("#grand-total-accessories-profit")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getAccessoriesTotal()));
        this.page1.selectFirst("#total-whatnot")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getWhatnotTotal()));
        this.page1.selectFirst("#total-unsupplied")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getOptUnsupplied()));
        this.page1.selectFirst("#total-oil-profit")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getOilTotal()));
        this.page1.selectFirst("#estimated-incomes #grand-total")
                .text(formatTwoFractionDigitsNumber(estimatedIncomes.getGrandTotal()));

        final ActualIncomes actualIncomes = revision.getActualIncomes();
        this.page1.selectFirst("#total-fund-end")
                .text(formatEuroPrice(actualIncomes.getEndFund()));
        this.page1.selectFirst("#total-deposits")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getDepositTotal()));
        this.page1.selectFirst("#total-coupons")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getCouponTotal()));
        this.page1.selectFirst("#total-credit-cards")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getCreditCardsTotal()));
        this.page1.selectFirst("#total-opt-cash")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getOptCashTotal()));
        this.page1.selectFirst("#total-opt-credit-cards")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getOptCreditCardsTotal()));
        this.page1.selectFirst("#total-private-cards")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getCardsTotal()));
        this.page1.selectFirst("#total-opt-refunds")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getOptRefundsTotal()));
        this.page1.selectFirst("#actual-incomes #grand-total")
                .text(formatTwoFractionDigitsNumber(actualIncomes.getGrandTotal()));

        setDifference(revision.getActualIncomes().getGrandTotal() -
                revision.getEstimatedIncomes().getGrandTotal());
    }

    public PdfModel getCompiledModel() {
        return new PdfModel(page1.html(), page2.html(), this.pdfModel.getAttributes());
    }

    private boolean addTransaction(final Transaction transaction) {
        switch (transaction.getType()) {
            case SALE:
                return addTransaction((Sale) transaction);
            case DEPOSIT:
                return addTransaction((Deposit) transaction);
            case WHATNOT:
                return addTransaction((Whatnot) transaction);
            case CREDIT_CARD_PAYMENT:
                return addTransaction((CreditCardPayment) transaction);
            case COUPON_PAYMENT:
                return addTransaction((CouponPayment) transaction);
            default:
                return false;
        }
    }

    public void setGrandTotalForFuel(final Fuel fuel, double litres, double profit) {
        if (fuel == Fuel.GPL) {
            this.page1.selectFirst("#total-gpl-litres")
                    .text(formatTwoFractionDigitsNumber(litres));
            this.page1.selectFirst("#total-gpl-profit")
                    .text(formatTwoFractionDigitsNumber(profit));
        }
        this.page1.selectFirst(String.format(Locale.ITALIAN,
                "#grand-total-%s-profit", fuel.toString()))
                    .text(formatTwoFractionDigitsNumber(profit));
        this.page1.selectFirst(String.format(Locale.ITALIAN,
                "#grand-total-%s-litres", fuel))
                    .text(formatTwoFractionDigitsNumber(litres));
    }

    public void setGrandTotal(double litres, double profit) {
        this.page1.selectFirst("#grand-total-litres")
                .text(formatTwoFractionDigitsNumber(litres));
        this.page1.selectFirst("#grand-total-profit")
                .text(formatTwoFractionDigitsNumber(profit));
    }

    private void setDifference(double amount) {
        this.page1.selectFirst(".stuff-totals #difference").text(formatTwoFractionDigitsNumber(amount));
    }

    private boolean addTransaction(final Sale sale) {
        final Item item = sale.getItem();
        try {
            if (item.getUnit() == Unit.LITERS) {
                findFirstEmptyField(oilFields).text(String.format(
                        Locale.ITALIAN, "%dx %s", item.getAvailableQuantity(), item.getName().toUpperCase()));
            } else {
                findFirstEmptyField(accessoriesFields).text(String.format(
                        Locale.ITALIAN, "%dx %s", item.getAvailableQuantity(), item.getName().toUpperCase()));
            }
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private boolean addTransaction(final Deposit deposit) {
        try {
            findFirstEmptyField(depositFields).text(formatEuroPrice(deposit.getAmount()));
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private boolean addTransaction(final Whatnot whatnot) {
        try {
            findFirstEmptyField(whatnotFields).text(String.format(Locale.ITALIAN, "%s € %s",
                    whatnot.getWhat(), whatnot.getAmount()));
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private boolean addTransaction(final CreditCardPayment creditCardPayment) {
        try {
            findFirstEmptyField(creditCardTransactionFields)
                    .text(formatEuroPrice(creditCardPayment.getAmount()));
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private boolean addTransaction(final CouponPayment couponPayment) {
        try {
            final Element e = findFirstEmptyField(couponPaymentTransactionFields);
            e.text(formatEuroPrice(couponPayment.getAmount()));

            String customer = couponPayment.getCustomer();
            if (customer.isEmpty() || customer.matches("^\\s+$")) {
                customer = "-";
            }
            e.parent().selectFirst(".customer").text(TextUtils.capitalizeWords(customer));
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private static String formatEuroPrice(double amount) {
        return String.format(Locale.ITALIAN, "€ %.2f", amount);
    }

    private static String formatTwoFractionDigitsNumber(double value) {
        return String.format(Locale.ITALIAN, "%.2f", value);
    }

    @NonNull
    private static Element findFirstEmptyField(Elements fields) {
        for (final Element e : fields) {
            if (isEmpty(e)) return e;
        }
        throw new IllegalStateException("Not enough room to fit in everything. Did it pass the checks?");
    }

    private static boolean isEmpty(Element field) {
        return field.text() == null || field.text().isEmpty() || field.html().matches("^(&nbsp;)*$");
    }

    private String getString(@StringRes int res) {
        return context.getString(res);
    }

}
