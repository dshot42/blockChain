package blockChain.system.mongoDb.webSocket;


import blockChain.chiffrement.ChiffrementUtils;


import blockChain.nodeThreads.utils.TransactionUtils;
import blockChain.system.mongoDb.service.BlockChainService;
import blockChain.transaction.consensus.ConsensusUtils;

import blockChain.transaction.seller.AckAndReceiveTransactionProcess;
import org.springframework.stereotype.Service;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import vendor.models.Transaction;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class MongoWebConsensusListener implements Runnable {
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
            // fixme ici on recoit un msg sans addresse , trouver le coupable
        }
    }

    private void triggerRecipeEvent() throws Exception {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("triggerRecipeEvent() ThreadPoolHandler");
        TransitTransaction containerToEmit = nodeUtils.jsonToCryptedTransaction(in.readLine());

        ObjectMapper objectMapper = new ObjectMapper();

        String transaction2string = ChiffrementUtils.decryptAES(containerToEmit.getCryptedTransaction(), ChiffrementUtils.systemKey);

        Transaction transaction = objectMapper.readValue(transaction2string, Transaction.class);

        List<String> listHash = new LinkedList<>();
        if (consensusReceipeHash.containsKey(transaction2string)) {
             listHash = consensusReceipeHash.get(transaction2string);
        }
        listHash.add(containerToEmit.getCryptedTransactionHash());
        consensusReceipeHash.put(transaction2string, listHash);

        if (consensusReceipeHash.get(transaction2string).size() == ConsensusUtils.numberConsensusMember) {
            boolean isValidated = checkConsensusValidity(transaction, consensusReceipeHash.get(transaction2string));
            if (isValidated) {
                System.out.println("Consensus System : Validation of Transation riceived by consensus core ! ");
                if (containerToEmit != null) {
                    blockChainService.registryTransactionOnBlockChain(containerToEmit.getCryptedTransaction());
                     // send transaction to receiver of the transaction
                    // inutile il aura simplement a synchroniser son wallet depis la blockchain
                      // nodeUtils.socketEmitToNextThread(containerToEmit.getReceiverAddress().getAddress(), GenericObjectConvert.objectToString(containerToEmit));
                }
                consensusReceipeHash.remove(transaction2string);
            } else {
                System.out.println("Consensus System : l'integrite de la transaction n'a pas ete valide par les membre du consensus ! ");
            }
        }
    }


    private void sendTransitTransactionToConsensusMember(TransitTransaction transitTransaction, String transaction2string) throws Exception {
        if (transitTransaction != null) {
           blockChainService.registryTransactionOnBlockChain(transitTransaction.getCryptedTransaction());
            nodeUtils.socketEmitToNextThread(transitTransaction.getReceiverAddress().getAddress(), GenericObjectConvert.objectToString(transitTransaction));
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