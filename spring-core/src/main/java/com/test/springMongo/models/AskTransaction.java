package com.test.springMongo.models;

import java.time.LocalDateTime;


public class AskTransaction {

    PublicWallet sellerWalletAddress;
    PublicWallet buyerWalletAddress;

    float amount;

    String dateTime;

    byte[] key;


    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public PublicWallet getSellerWalletAddress() {
        return sellerWalletAddress;
    }

    public void setSellerWalletAddress(PublicWallet sellerWalletAddress) {
        this.sellerWalletAddress = sellerWalletAddress;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public PublicWallet getBuyerWalletAddress() {
        return buyerWalletAddress;
    }

    public void setBuyerWalletAddress(PublicWallet buyerWalletAddress) {
        this.buyerWalletAddress = buyerWalletAddress;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


}
