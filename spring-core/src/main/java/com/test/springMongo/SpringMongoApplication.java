package com.test.springMongo;

import com.test.springMongo.transaction.initTransaction.initBlockChain.CreateBlockChain;
import com.test.springMongo.transaction.personalWalletHandler.PersonnalWalletHandler;
import com.test.springMongo.transaction.seller.AskAndReceiveTransactionProcess;
import com.test.springMongo.transaction.buyer.SendTransactionProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class SpringMongoApplication {

    static CreateBlockChain createInitBlockChain;

    static SendTransactionProcess sendTransactionController;

    static AskAndReceiveTransactionProcess askTransactionController;



    @Autowired
    public SpringMongoApplication(CreateBlockChain createInitBlockChain, SendTransactionProcess sendTransaction, AskAndReceiveTransactionProcess askTransactionController) {
        this.createInitBlockChain = createInitBlockChain;
        this.sendTransactionController = sendTransaction;
        this.askTransactionController = askTransactionController;

    }

    /***
     *
     * @param args
     * @throws Exception

     * ***************    SCENARIO    **************

     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur renvoir un ackTransaction avec son address sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré avec la clef !

     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringMongoApplication.class, args);
        createInitBlockChain.initBlockChain();
        initWalletHandler();
        launchTransaction();
    }

    public static void initWalletHandler() {
        Thread buyerWallet = new Thread( new PersonnalWalletHandler("Buyer","127.0.0.1:4444")); // acheteur
        buyerWallet.start();

        Thread sellerWallet = new Thread(new PersonnalWalletHandler("Seller","127.0.0.1:4443")); // acheteur
        sellerWallet.start();
    }

    private static void launchTransaction() throws InterruptedException {
        Thread tSend = new Thread(sendTransactionController); // acheteur
        tSend.start();
        Thread.sleep(1000);

        Thread tAsk = new Thread(askTransactionController); // vendeur
        tAsk.start();
    }


}
