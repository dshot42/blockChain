package consensus.service;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.nodeThreads.utils.TransactionUtils;
import client.wallet.handler.personalWalletHandler.InitTransactionDetails;
import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ActorPoolHandler {

    public String host ="127.0.0.1";
    public int nbOfNode = 3; // nombre de membres du jury
    public int nbOfMembersPerNodes = 5; // consensus de 5 membres

    public Map<Integer, List<Integer>> memberPerNode = new HashMap<>();
    List<Integer>  memberlreadyDefined = new LinkedList<>();
    @Autowired
    TransactionUtils transactionUtils;
    boolean isReady = false;

    public ActorPoolHandler() {
        // code in the other thread, can reference "var" variable
        for (int j = 1; j <= nbOfNode; j++) {
            this.memberPerNode.put(j, new ArrayList<>());

            ExecutorService executor = Executors.newFixedThreadPool(nbOfMembersPerNodes);
            // Submit tasks to the thread pool
            for (int i = 0; i < nbOfMembersPerNodes; i++) {
                int finalJ = j; //
                executor.submit(() -> {
                    try {
                        socketClientStart(finalJ);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        }

        isReady = true;
        System.out.println("Actor Pool Handler is ready with " + nbOfMembersPerNodes + " members.");
    }

    public void socketClientStart(Integer nodeLvl) throws Exception { // port  between 5000-5555
        int newMember = getRandomNextNodeMember();
        memberPerNode.get(nodeLvl).add(newMember);
        ServerSocket serverSocket = new ServerSocket(getRandomNextNodeMember());
        System.out.println("Thread Actor Socket on Node : " + nodeLvl+ " is ready on port: " + serverSocket.getLocalPort());
        Socket clientSocket = serverSocket.accept();


        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String datas = in.readLine();

        TransitTransaction cryptedTransaction = transactionUtils.jsonToCryptedTransaction(datas);
        System.out.println(("Node " + nodeLvl + " received transaction: " + cryptedTransaction.getCryptedTransaction()));
    }


    public int getRandomNextNodeMember() {
        Random rand = new Random();
        Integer newMember = rand.nextInt(999) + 5000;
        if (memberlreadyDefined.contains(newMember))
            return getRandomNextNodeMember();

        memberlreadyDefined.add(newMember);
        return newMember;
    }

    // le premier Node connait l'emmeteur donc renvoie uniquement un hash au serveur
    // le dernier Node ne le connait pas il renvoient la transaction chiffré


    // les sender sont des - et les receiver des + pour le calcul du montant du wallet !
    public TransitTransaction setTransitTransaction() throws Exception {
        Transaction tr = new Transaction();
        tr.setSenderAddress(InitTransactionDetails.personnalWallet);
        tr.setSenderAddress(InitTransactionDetails.remoteWallet);
        tr.setAmount(InitTransactionDetails.transacAmount);
        tr.setDateTime(String.valueOf(LocalDateTime.now()));

        TransitTransaction tt = new TransitTransaction();

        tt.setCryptedTransaction(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(tr), PrivateWalletHandler.walletPrivateKey));
        return tt;
    }

    public void broadCastTransaction(Integer nodeLvl) throws Exception {
        TransitTransaction cryptedTransaction = this.setTransitTransaction();
      this.memberPerNode.get(nodeLvl).forEach(
                member -> {
                    Socket socket = null;
                    try {
                        socket = new Socket(this.host, member);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        String cryptedTransaction2String = GenericObjectConvert.objectToString(cryptedTransaction);
                        out.write(cryptedTransaction2String);
                        PrintWriter writer = new PrintWriter(out, true);
                        writer.println();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });
    }

    public void dispatchTransactionOnNodes() throws Exception {
        this.broadCastTransaction(1);
    }

    public void broadCastCryptedTransaction() {

    }


}
