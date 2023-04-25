package com.test.springMongo.models;

import java.util.List;

public class PersonnalWallet extends PublicWallet {

    public PersonnalWallet() {
    }

    byte[] key;

    String cryptedContent;

    List<Transaction> transactions;

    public float amount;

    public PersonnalWallet(String address, String uniqueWalletId) {
        super(address, uniqueWalletId);
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

    public String getUniqueWalletId() {
        return uniqueWalletId;
    }

    public void setUniqueWalletId(String uniqueWalletId) {
        this.uniqueWalletId = uniqueWalletId;
    }


}
