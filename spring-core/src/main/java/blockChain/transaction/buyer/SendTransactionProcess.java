package blockChain.transaction.buyer;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.PrivateWallet;
import blockChain.transaction.consensus.ConsensusUtils;
import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import blockChain.transaction.nodeThreads.utils.TransactionUtils;
import blockChain.wallet.InitWallet;
import blockChain.wallet.personalWalletHandler.PrivateWalletHandler;
import blockChain.models.Transaction;
import blockChain.models.TransactionContainerToEmit;
import blockChain.transaction.consensus.ConsensusThreadProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class SendTransactionProcess implements Runnable { // processus de l'acheteur

    @Autowired
    TransactionUtils nodeUtils;

    private static PrintWriter out;
    private static BufferedReader in;

    private PrivateWallet privateWallet;

    private byte[] walletKey = {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
            -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
            -8, 60, 69, 38, -33, 78, 55, -65, 104};
    public static byte[] privateKeyCache;

    public void run() {
        try {

            new Thread(new Runnable() {
                public void run() {
                    try {
                        socketClientStart();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String generateCryptedTransaction(TransactionContainerToEmit ackTransaction, byte[] privateKeyCache) throws Exception {

        Transaction transaction = MapperTransaction(ackTransaction);
        String transactionJson = GenericObjectConvert.objectToString(transaction);
   //     transaction.setHash(ChiffrementUtils.cryptAES(transactionJson));

        TransactionContainerToEmit sendTransaction = new TransactionContainerToEmit();
        sendTransaction.setReceiverAddress(ackTransaction.getSenderAddress());
        // laddresse de m'émeteur devient laddresse du recepteur

        sendTransaction.setCryptedTransaction(ChiffrementUtils.cryptAES(transactionJson, privateKeyCache));
        sendTransaction.setCryptedTransactionHash(ChiffrementUtils.generateHashKey(sendTransaction.getCryptedTransaction()));
        sendTransaction.setState("SYNACK");
        return GenericObjectConvert.objectToString(sendTransaction);
    }

    private static Transaction MapperTransaction(TransactionContainerToEmit askTransaction) {
        Transaction transaction = new Transaction();
        transaction.setReceiverAddress(askTransaction.getSenderAddress()); // address communiqué
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitWallet.buyerWallet.getAddress(), InitWallet.buyerWallet.getUniqueWalletId());

        PrivateWallet myPrivateWallet = privateWalletHandler.getWallet();

        transaction.setSenderAddress(privateWalletHandler.mapPrivateToPublicWaller(myPrivateWallet)); // this thread
        transaction.setAmount(askTransaction.getAmount());
        transaction.setDateTime(askTransaction.getDateTime());
        return transaction;
    }


    public void socketClientStart() throws Exception {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(InitWallet.buyerWallet.getAddress().split(":")[1]));
        while (true) {
            Socket clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String datas = in.readLine();

            TransactionContainerToEmit cryptedTransaction = nodeUtils.jsonToCryptedTransaction(datas);
            if (cryptedTransaction.getState().equals("SYN")) { //  demande de transaction
                System.out.println("SYNC receive");
                // todo important ! mes 2 scenario !!!!
                nodeUtils.emitCryptedTransactionOnNode(generateCryptedTransaction(cryptedTransaction, cryptedTransaction.getKey()));
               Thread.sleep(10000); // on wait 10 sec avant pour pas avoir de souci d'asynchrone
                emitBroadcastCryptedTransactionOnConsensus(generateCryptedTransaction(cryptedTransaction, ChiffrementUtils.systemKey)); // systeme key
            } else if (cryptedTransaction.getState().equals("ACK")) { //  retour apres persistance block chaine
                ConsensusUtils.systemConsensusAckFeedBackTransactionPersisted(InitWallet.buyerWallet, walletKey, nodeUtils, cryptedTransaction);
                System.out.println("Fin de la transaction par Consensus ! (SENDER)");
            }
        }
    }


    /////////////////////////  SYSTEME PAR CONSENSUS  /////////////////////////

    public void emitBroadcastCryptedTransactionOnConsensus(String cryptedTransactiondata) throws Exception {

        ConsensusUtils.sendTransactionToBlockChainSystem(cryptedTransactiondata);

        int i = 0;
        int numberConsensusMember = 5;

        while (i != 5) {
            String consensusMember = nodeUtils.getRandomNextNodeMember();
            startNextConsensusMemberThread(consensusMember);
            Thread.sleep(100); // le temps de demarrer la socket d'écoute
            nodeUtils.socketEmitToNextThread(consensusMember, cryptedTransactiondata);
            i++;
        }
    }

    public void startNextConsensusMemberThread(String thisMember) throws Exception {
        ConsensusThreadProcess nodeMember = new ConsensusThreadProcess(thisMember, nodeUtils);
        Thread t = new Thread(nodeMember);
        t.start();
    }


}
