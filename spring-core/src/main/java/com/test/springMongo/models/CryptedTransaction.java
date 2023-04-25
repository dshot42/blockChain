package com.test.springMongo.models;


public class CryptedTransaction {


    PublicWallet receiverAddress;

    PublicWallet senderAddress;

    String cryptedTransaction; // transaction
    String hash;

    String dateTime;

    byte[] key; // uniquement sur les retours !

    public PublicWallet getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(PublicWallet receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getCryptedTransaction() {
        return cryptedTransaction;
    }

    public void setCryptedTransaction(String cryptedTransaction) {
        this.cryptedTransaction = cryptedTransaction;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public PublicWallet getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(PublicWallet senderAddress) {
        this.senderAddress = senderAddress;
    }

}
