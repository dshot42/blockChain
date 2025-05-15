package blockChain.models;

import java.util.List;

public class PrivateWallet {

    public PrivateWallet() {
    }

    public PrivateWallet(String address, String uniqueWalletId) {
        this.address = address;
        this.uniqueWalletId = uniqueWalletId;
    }
    public String address;

    public String uniqueWalletId;


    byte[] key;

    String cryptedContent;

    List<Transaction> transactions;

    public float amount;



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

    public String getUniqueWalletId() {
        return uniqueWalletId;
    }

    public void setUniqueWalletId(String uniqueWalletId) {
        this.uniqueWalletId = uniqueWalletId;
    }
}
