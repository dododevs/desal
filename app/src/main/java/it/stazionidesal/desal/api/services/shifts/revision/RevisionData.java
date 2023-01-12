package it.stazionidesal.desal.api.services.shifts.revision;

import com.google.gson.annotations.SerializedName;

public class RevisionData {

    @SerializedName("total")
    private final FortechTotal fortechTotal;

    @SerializedName("incomes")
    private final Incomes incomes;

    public RevisionData(FortechTotal totals, Incomes incomes) {
        this.fortechTotal = totals;
        this.incomes = incomes;
    }

    public static class Incomes {

        @SerializedName("cards")
        private final double privateCards;

        @SerializedName("opt")
        private final Opt optData;

        public static class Opt {

            @SerializedName("optCashTotal")
            private final double optCashTotal;

            @SerializedName("optCreditCardsTotal")
            private final double optCreditCardsTotal;

            @SerializedName("optRefunds")
            private final double optRefunds;

            @SerializedName("optUnsupplied")
            private final double optUnsupplied;

            public Opt(double optCashTotal, double optCreditCardsTotal,
                       double optRefunds, double optUnsupplied) {
                this.optCashTotal = optCashTotal;
                this.optCreditCardsTotal = optCreditCardsTotal;
                this.optRefunds = optRefunds;
                this.optUnsupplied = optUnsupplied;
            }
        }

        public Incomes(double privateCards, Opt optData) {
            this.privateCards = privateCards;
            this.optData = optData;
        }
    }

}