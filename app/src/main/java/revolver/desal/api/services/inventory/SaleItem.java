package revolver.desal.api.services.inventory;

import com.google.gson.annotations.SerializedName;

import revolver.desal.api.services.transactions.Transaction;

public class SaleItem {

    @SerializedName("oid")
    private final String oid;

    @SerializedName("quantity")
    private final int quantity;

    @SerializedName("transaction")
    private final Transaction transaction;

    public SaleItem(String oid, int quantity, Transaction transaction) {
        this.oid = oid;
        this.quantity = quantity;
        this.transaction = transaction;
    }

    public String getOid() {
        return oid;
    }

    public int getQuantity() {
        return quantity;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
