package com.test.springMongo.models;


public class TransactionContainerToEmit {

    String state; // enum => ack, sendTransaction, feedBack (retour donnée)
    PublicWallet receiverAddress;

    PublicWallet senderAddress;

    String cryptedTransaction; // transaction
    String cryptedTransactionHash; // hash de cryptedTransaction

    String immutableHash; // hash de la transaction retourné par la blockchain

    String blockHash; // hash du block // le immutable

    String dateTime;

    byte[] key; // uniquement sur les retours !

    float amount;


    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

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

    public String getCryptedTransactionHash() {
        return cryptedTransactionHash;
    }

    public void setCryptedTransactionHash(String cryptedTransactionHash) {
        this.cryptedTransactionHash = cryptedTransactionHash;
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

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getImmutableHash() {
        return immutableHash;
    }

    public void setImmutableHash(String immutableHash) {
        this.immutableHash = immutableHash;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
