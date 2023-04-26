package com.test.springMongo.transaction.buyer;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springMongo.models.CryptedTransaction;
import com.test.springMongo.models.PrivateWallet;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.GenericObjectConvert;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.NodeUtils;
import com.test.springMongo.wallet.InitWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

@Component
public class SendTransactionProcess implements Runnable { // processus de l'acheteur

    @Autowired
    NodeUtils nodeUtils;

    private static PrintWriter out;
    private static BufferedReader in;

    private PrivateWallet privateWallet;

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


    public String generateCryptedTransaction(CryptedTransaction ackTransaction) throws Exception {

        Transaction transaction = MapperTransaction(ackTransaction);

        CryptedTransaction sendTransaction = new CryptedTransaction();
        sendTransaction.setReceiverAddress(ackTransaction.getSenderAddress());
        // laddresse de m'émeteur devient laddresse du recepteur
        String transactionJson = GenericObjectConvert.objectToString(transaction);
        privateKeyCache = ackTransaction.getKey();
        sendTransaction.setCryptedTransaction(ChiffrementUtils.cryptAES(transactionJson, privateKeyCache));
        sendTransaction.setHash(ChiffrementUtils.generateHashKey(sendTransaction.getCryptedTransaction()));
        sendTransaction.setState("SENDTRANSACTION");
        return GenericObjectConvert.objectToString(sendTransaction);
    }

    private static Transaction MapperTransaction(CryptedTransaction askTransaction) {
        Transaction transaction = new Transaction();
        transaction.setReceiverAddress(askTransaction.getSenderAddress()); // address communiqué
        transaction.setSenderAddress(InitWallet.buyerWallet); // this thread
        Random random = new Random();
        transaction.setAmount(random.nextInt(1000));
        transaction.setDateTime(askTransaction.getDateTime());
        return transaction;
    }


    public void socketClientStart() throws Exception {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(InitWallet.buyerWallet.getAddress().split(":")[1]));
        Socket clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String datas = in.readLine();
        // if accepte => send
        CryptedTransaction cryptedTransaction = getAckTransaction(datas);

        if (cryptedTransaction.getState().equals("ACK")) {
            System.out.println("ACK receive");
            nodeUtils.sendCryptedTransactionOnNode(generateCryptedTransaction(cryptedTransaction));
        } else if (cryptedTransaction.getState().equals("FEEDBACK")) { //  state = feedback
            System.out.println("FEEDBACK receive");
            nodeUtils.persistTransactionOnWallet(cryptedTransaction, privateKeyCache, "Buyer");
            System.out.println("Fin de la transaction ! ");
        }
        serverSocket.close();
        socketClientStart();
    }


    public CryptedTransaction getAckTransaction(String datas) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CryptedTransaction transaction = objectMapper.readValue(datas, CryptedTransaction.class);
        return transaction;
    }

}
