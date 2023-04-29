package blockChain.system.mongoDb.webSocket;


import blockChain.chiffrement.ChiffrementUtils;
import blockChain.system.mongoDb.service.BlockChainService;
import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import com.fasterxml.jackson.databind.ObjectMapper;
import blockChain.models.Transaction;
import blockChain.models.TransactionContainerToEmit;
import blockChain.system.mongoDb.repository.ElementRepository;
import blockChain.transaction.nodeThreads.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class MongoWebConsensusListener implements Runnable {

    @Autowired
    ElementRepository elementService;


    @Autowired
    BlockChainService blockChainService;

    @Autowired
    TransactionUtils nodeUtils;

    public static String systemSocketAddress = "127.0.0.1:6666";

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    private static Map<String, List<String>> consensusReceipeHash = new HashMap<>();

    private int numberReceipeNeeded = 6; // member + le sender


    @Override
    public void run() {
        try {
            socketClientStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(systemSocketAddress.split(":")[1]));
        while (true) {
            clientSocket = serverSocket.accept();
            System.out.println("System receive transaction from consensus member ! ");
            triggerRecipeEvent();
        }
    }

    private void triggerRecipeEvent() throws Exception {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        TransactionContainerToEmit cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());

        String transaction2string = ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), ChiffrementUtils.systemKey);
        ObjectMapper objectMapper = new ObjectMapper();
        Transaction transaction = objectMapper.readValue(transaction2string, Transaction.class);

        List<String> listHash = new LinkedList<>();
        if (consensusReceipeHash.containsKey(transaction2string)) {
             listHash = consensusReceipeHash.get(transaction2string);
        }
        listHash.add(cryptedTransaction.getCryptedTransactionHash());
        consensusReceipeHash.put(transaction2string, listHash);

        if (consensusReceipeHash.get(transaction2string).size() == numberReceipeNeeded) {
            boolean isValidated = checkConsensusValidity(transaction, consensusReceipeHash.get(transaction2string));
            if (isValidated) {
                System.out.println("Consensus System : validation de la transaction par le consensus ! ");
                sendReceipeToActorOfTransation(cryptedTransaction, ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction)));
            } else {
                System.out.println("Consensus System : l'integrite de la transaction n'a pas ete valide par les membre du consensus ! ");
            }
        }
    }

    private void sendReceipeToActorOfTransation(TransactionContainerToEmit cryptedTransaction, String transaction2string) throws Exception {
        TransactionContainerToEmit cryptedToEmitTransaction = blockChainService.registryTransactionOnBlockChain(cryptedTransaction.getCryptedTransaction());

        if (cryptedToEmitTransaction != null) {
            // send feedback
            nodeUtils.socketEmitToNextThread(cryptedToEmitTransaction.getReceiverAddress().getAddress(), GenericObjectConvert.objectToString(cryptedToEmitTransaction));
            nodeUtils.socketEmitToNextThread(cryptedToEmitTransaction.getSenderAddress().getAddress(), GenericObjectConvert.objectToString(cryptedToEmitTransaction));
        }
        consensusReceipeHash.remove(transaction2string);
    }

    public boolean checkConsensusValidity(Transaction transaction, List<String> listHash) {
        boolean integrity = true;
        for (int i = 0; i < listHash.size() - 1; i++) {
            if (!listHash.get(i).equals(listHash.get(i + 1)))
                integrity = false;
        }
        return integrity;
    }
}