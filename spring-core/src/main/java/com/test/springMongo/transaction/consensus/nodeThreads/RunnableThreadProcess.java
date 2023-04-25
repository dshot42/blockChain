package com.test.springMongo.transaction.consensus.nodeThreads;

import com.test.springMongo.models.CryptedTransaction;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.GenericObjectConvert;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.NodeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RunnableThreadProcess implements Runnable { // membre du jury


    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;


    public String ip;

    public String nextMember;

    public NodeUtils nodeUtils;

    public boolean isReady = false;

    private static int cptMember = 0;

    @Autowired
    public RunnableThreadProcess(String ip, String nextMember, NodeUtils consensusSocketUtils) {
        this.ip = ip;
        this.nextMember = nextMember;
        this.nodeUtils = consensusSocketUtils;
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

        CryptedTransaction cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());
        String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());

        if (nodeUtils.checkValidation(cryptedTransaction.getHash(), thisHash)) {

            System.out.println(ip + " a validé l'intégrité avec succes {" + thisHash + "}");
            if (cptMember < NodeUtils.nodeValidatorLvl) {
                nodeUtils.startNextNodeMemberThread(nextMember); // start next Thread
                Thread.sleep(100); // cela serait mieux avec un ack, verifier que les socket member sont ready !

                nodeUtils.socketEmitToNextThread(nextMember, GenericObjectConvert.objectToString(cryptedTransaction));
            }
            else
                sendToFinalDestinator(cryptedTransaction, GenericObjectConvert.objectToString(cryptedTransaction));
        } else {
            System.out.println("Violation de l'intégrité de la transaction ! ");
            // transaction aborted
        }
        cptMember++;
    }

    private void sendToFinalDestinator(CryptedTransaction cryptedTransaction, String cryptedTransationToString) throws Exception {
        nodeUtils.socketEmitToNextThread(cryptedTransaction.getReceiverAddress().getAddress(), cryptedTransationToString);
    }

}
