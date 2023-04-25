package com.test.springMongo.models;

import java.util.List;

public class PublicWallet  {
    public PublicWallet() {
    }

    public PublicWallet(String address, String uniqueWalletId) {
        this.address = address;
        this.uniqueWalletId = uniqueWalletId;
    }

    public String address;

    public String uniqueWalletId;

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
