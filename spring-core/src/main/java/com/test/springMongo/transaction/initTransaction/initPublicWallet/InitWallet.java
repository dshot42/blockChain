package com.test.springMongo.transaction.initTransaction.initPublicWallet;

import com.chiffrement.ChiffrementUtils;
import com.test.springMongo.models.PublicWallet;

public class InitWallet {
    public static PublicWallet sellerWallet;

    static {
        try {
            sellerWallet = new PublicWallet("127.0.0.1:4998", ChiffrementUtils.generateHashKey("TOTO"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicWallet buyerWallet;

    static {
        try {
            buyerWallet = new PublicWallet("127.0.0.1:4999",ChiffrementUtils.generateHashKey("KIKI"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
