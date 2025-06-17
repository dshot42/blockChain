package vendor.models;

import java.util.List;

public class PrivateWallet {
    public String address;

    public String walletId;

    byte[] key;

    String cryptedContent;

    List<Transaction> transactions;

    public float amount;

    public PrivateWallet() {
    }

    public PrivateWallet(String address, String walletId) {
        this.address = address;
        this.walletId = walletId;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public String getCryptedContent() {
        return cryptedContent;
    }

    public void setCryptedContent(String cryptedContent) {
        this.cryptedContent = cryptedContent;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
}
