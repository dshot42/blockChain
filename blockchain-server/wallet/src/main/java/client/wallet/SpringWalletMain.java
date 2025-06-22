package client.wallet;



import client.wallet.handler.personalWalletHandler.InitTransactionDetails;
import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class SpringWalletMain{


    /***
     *
     * @param args
     * @throws Exception

     * ***************    SCENARIO    **************
     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur (tiers de confiace) renvoir un ackTransaction avec son address sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré avec la clef  au vendeur par le bien d'un noeud


     * ***************    SCENARIO 2   **************
     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur (tiers de confiace) renvoir un ackTransaction avec son address sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré au systeme et la transaction est validé par
     *    un consensus !
     */

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Wallet Application...");
        SpringApplication.run(SpringWalletMain.class, args);

        System.out.println("Send PrivateKey to Block chain System");
        PrivateWalletHandler.sendPrivateKey();
        System.out.println("Check wallet : ");
        checkWallet();
        System.out.println("Wallet is ready, launching transaction");
    }


    // todo isoler code
    private static void checkWallet() throws Exception {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();

        privateWalletHandler.testWallet();
    }


}
