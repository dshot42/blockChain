package client.wallet.handler.personalWalletHandler;


import vendor.models.PublicWallet;

public class InitWallet {
    public static PublicWallet remoteWallet;

    static {
        try {
            remoteWallet = new PublicWallet("127.0.0.1:8889", "Seller");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicWallet personnalWallet;

    static {
        try {
            personnalWallet = new PublicWallet("127.0.0.1:8888", "Personnal");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
