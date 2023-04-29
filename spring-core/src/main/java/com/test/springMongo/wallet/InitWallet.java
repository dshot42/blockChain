package com.test.springMongo.wallet;

import com.test.springMongo.models.PublicWallet;

public class InitWallet {
    public static PublicWallet sellerWallet;

    static {
        try {
            sellerWallet = new PublicWallet("127.0.0.1:8889", "Seller");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicWallet buyerWallet;

    static {
        try {
            buyerWallet = new PublicWallet("127.0.0.1:8888", "Buyer");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
