package com.test.springMongo.transaction.buyer;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springMongo.models.CryptedTransaction;
import com.test.springMongo.models.PublicWallet;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.GenericObjectConvert;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.NodeUtils;
import com.test.springMongo.transaction.initTransaction.initPublicWallet.InitWallet;
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

    private PublicWallet publicWallet;

    public void run() {
        try {
            publicWallet = InitWallet.buyerWallet;
            socketClientStart();
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
        sendTransaction.setCryptedTransaction(ChiffrementUtils.cryptAES(transactionJson, ackTransaction.getKey()));
        sendTransaction.setHash(ChiffrementUtils.generateHashKey(sendTransaction.getCryptedTransaction()));

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

    public void sendCryptedTransactionOnNode(CryptedTransaction askTransaction) throws Exception {
        String firstMember = nodeUtils.getRandomNextNodeMember();
        nodeUtils.startNextNodeMemberThread(firstMember);
        String cryptedTransactiondata = generateCryptedTransaction(askTransaction);
        nodeUtils.socketEmitToNextThread(firstMember, cryptedTransactiondata);
    }

    public void socketClientStart() throws Exception {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(InitWallet.buyerWallet.getAddress().split(":")[1]));
        Socket clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String datas = in.readLine();

        // if accepte => send
        sendCryptedTransactionOnNode(getAckTransaction(datas));
    }

    public CryptedTransaction getAckTransaction(String datas) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CryptedTransaction transaction = objectMapper.readValue(datas, CryptedTransaction.class);
        return transaction;
    }

}
