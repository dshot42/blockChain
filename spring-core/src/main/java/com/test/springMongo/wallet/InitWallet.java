package com.test.springMongo.wallet;

import com.chiffrement.ChiffrementUtils;
import com.test.springMongo.models.PrivateWallet;
import com.test.springMongo.models.PublicWallet;

public class InitWallet {
    public static PublicWallet sellerWallet;

    static {
        try {
            sellerWallet = new PublicWallet("127.0.0.1:9999", "Seller");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicWallet buyerWallet;

    static {
        try {
                buyerWallet = new PublicWallet("127.0.0.1:9998","Buyer");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
