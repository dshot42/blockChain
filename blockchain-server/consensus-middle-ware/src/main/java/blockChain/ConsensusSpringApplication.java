package blockChain;

import blockChain.transaction.consensus.webSocket.ThreadPoolHandler;
import blockChain.transaction.buyer.SendTransactionProcess;
import blockChain.transaction.seller.AckAndReceiveTransactionProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

    @Configuration
    @SpringBootApplication
    public class ConsensusSpringApplication {

        static SendTransactionProcess sendTransactionController;
        static AckAndReceiveTransactionProcess askTransactionController;
        static ThreadPoolHandler threadPoolHandler;

        @Autowired
        public ConsensusSpringApplication( SendTransactionProcess sendTransaction, AckAndReceiveTransactionProcess askTransactionController, ThreadPoolHandler threadPoolHandler) {
            this.sendTransactionController = sendTransaction;
            this.askTransactionController = askTransactionController;
            this.threadPoolHandler = threadPoolHandler;

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

        public static void main(String[] args) throws InterruptedException {
            System .out.println("Consensus System socket listener is instantiated and currently running");
            SpringApplication.run(ConsensusSpringApplication.class, args);
            Thread systemSocketListener = new Thread(threadPoolHandler); // acheteur
            systemSocketListener.start();

             initTransactionController(); // pour test
        }


        private static void initTransactionController() throws InterruptedException {
            try {
                Thread tSend = new Thread(sendTransactionController); // buyer
                tSend.start();
                Thread.sleep(1000);

                Thread tAsk = new Thread(askTransactionController); // seller
                tAsk.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
