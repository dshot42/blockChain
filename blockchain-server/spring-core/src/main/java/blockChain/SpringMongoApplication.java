package blockChain;

import blockChain.system.mongoDb.webSocket.MongoWebConsensusListener;
import blockChain.transaction.initTransaction.initBlockChain.CreateBlockChain;
import blockChain.wallet.personalWalletHandler.PrivateWalletHandler;
import blockChain.transaction.buyer.SendTransactionProcess;
import blockChain.transaction.seller.AckAndReceiveTransactionProcess;
import blockChain.wallet.InitWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class SpringMongoApplication {

    static CreateBlockChain createInitBlockChain;

    static SendTransactionProcess sendTransactionController;

    static AckAndReceiveTransactionProcess askTransactionController;

    static MongoWebConsensusListener mongoWebConsensusListener;


    @Autowired
    public SpringMongoApplication(CreateBlockChain createInitBlockChain, SendTransactionProcess sendTransaction, AckAndReceiveTransactionProcess askTransactionController, MongoWebConsensusListener mongoWebConsensusListener) {
        this.createInitBlockChain = createInitBlockChain;
        this.sendTransactionController = sendTransaction;
        this.askTransactionController = askTransactionController;
        this.mongoWebConsensusListener = mongoWebConsensusListener;

    }

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
        SpringApplication.run(SpringMongoApplication.class, args);

        createInitBlockChain.initBlockChain();

        Thread systemSocketListener = new Thread(mongoWebConsensusListener); // acheteur
        systemSocketListener.start();
        Thread.sleep(1000);

        checkWallet();
        launchTransaction();
    }

    private static void checkWallet() throws Exception {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitWallet.buyerWallet.getAddress(), InitWallet.buyerWallet.getUniqueWalletId());
        privateWalletHandler.testWallet();
    }

    private static void launchTransaction() throws InterruptedException {
        Thread tSend = new Thread(sendTransactionController); // acheteur
        tSend.start();
        Thread.sleep(1000);

        Thread tAsk = new Thread(askTransactionController); // vendeur
        tAsk.start();
    }


}
