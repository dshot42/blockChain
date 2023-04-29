package com.test.springMongo.transaction.consensus;

import com.test.springMongo.models.TransactionContainerToEmit;
import com.test.springMongo.transaction.nodeThreads.utils.GenericObjectConvert;
import com.test.springMongo.transaction.nodeThreads.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ConsensusThreadProcess implements Runnable { // membre du jury

    public static String systemSocketAddress = "127.0.0.1:6666";
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;


    public String ip;

    public String nextMember;

    @Autowired
     TransactionUtils nodeUtils;

    public boolean isReady = false;

    private static int cptMember = 0;

    @Autowired
    public ConsensusThreadProcess(String ip, TransactionUtils nodeUtils) {
        this.ip = ip;
        this.nodeUtils = nodeUtils;
    }

    public void run() {
        // code in the other thread, can reference "var" variable
        try {
            isReady = true;
            socketClientStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(ip.split(":")[1]));
        clientSocket = serverSocket.accept();
        triggerRecipeEvent();
    }

    private void triggerRecipeEvent() throws Exception {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        TransactionContainerToEmit transactionContainerToEmit = nodeUtils.jsonToCryptedTransaction(in.readLine());
        String thisHash = nodeUtils.hashTransaction(transactionContainerToEmit.getCryptedTransaction());
        transactionContainerToEmit.setCryptedTransactionHash(thisHash);
        Thread.sleep(100); // cela serait mieux avec un ack, verifier que les socket member sont ready !
        ConsensusUtils.sendTransactionToBlockChainSystem(GenericObjectConvert.objectToString(transactionContainerToEmit));
    }


}
