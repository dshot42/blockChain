package vendor.transaction.service;


import vendor.models.PublicWallet;

import java.util.Random;

public class InitTransactionDetails {
    static Random random = new Random();
    public static Long transacAmount =0L;
    public static PublicWallet remoteWallet= new PublicWallet("127.0.0.1:8889", "Remote");
    public static PublicWallet personnalWallet =new PublicWallet("127.0.0.1:8888", "Personnal");

    public static void setTransationDetails(String personnalWalletId, String remoteWalletNameId, Long amount) {
        personnalWallet = new PublicWallet("127.0.0.1:8888", personnalWalletId);
        remoteWallet = new PublicWallet("127.0.0.1:8889", remoteWalletNameId);
        transacAmount = amount;
    }




}
