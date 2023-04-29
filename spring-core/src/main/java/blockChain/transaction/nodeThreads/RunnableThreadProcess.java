package blockChain.transaction.nodeThreads;

import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import blockChain.transaction.nodeThreads.utils.TransactionUtils;
import blockChain.models.TransactionContainerToEmit;
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

    public TransactionUtils nodeUtils;

    public boolean isReady = false;

    private static int cptMember = 0;

    @Autowired
    public RunnableThreadProcess(String ip, String nextMember, TransactionUtils consensusSocketUtils) {
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

        TransactionContainerToEmit cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());
        String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());

        if (nodeUtils.checkValidation(cryptedTransaction.getCryptedTransactionHash(), thisHash)) {

            System.out.println(ip + " a valide l'intégrite de la transaction {" + thisHash + "} avec succes ");
            if (cptMember < TransactionUtils.nodeValidatorLvl) {
                nodeUtils.startNextNodeMemberThread(nextMember); // start next Thread
                Thread.sleep(100); // cela serait mieux avec un ack, verifier que les socket member sont ready !

                nodeUtils.socketEmitToNextThread(nextMember, GenericObjectConvert.objectToString(cryptedTransaction));
            } else {
                sendToFinalDestinator(cryptedTransaction, GenericObjectConvert.objectToString(cryptedTransaction));
                cptMember = 0;
            }
        } else {
            System.out.println("Violation de l'intégrité de la transaction ! ");
            // transaction aborted
        }
        cptMember++;
    }

    private void sendToFinalDestinator(TransactionContainerToEmit cryptedTransaction, String cryptedTransationToString) throws Exception {
        nodeUtils.socketEmitToNextThread(cryptedTransaction.getReceiverAddress().getAddress(), cryptedTransationToString);
    }

}
