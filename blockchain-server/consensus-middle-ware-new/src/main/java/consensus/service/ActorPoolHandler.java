package consensus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import consensus.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ActorPoolHandler {


    @Autowired
    TransactionUtils transactionUtils;
    public String host = "127.0.0.1";
    public int nbOfNode = 3; // nombre de membres du jury
    public int nbOfMembersPerNodes = 5; // consensus de 5 membres

    public Map<Integer, List<Integer>> memberPerNode = new HashMap<>();
    List<Integer> memberlreadyDefined = new LinkedList<>();

    boolean isReady = false;

    public void start() {
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
        ServerSocket serverSocket = new ServerSocket(newMember);
        System.out.println("Thread Actor Socket on Node : " + nodeLvl + " is ready on port: " + serverSocket.getLocalPort());
        Socket clientSocket = serverSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String datas = in.readLine();

        ObjectMapper objectMapper = new ObjectMapper();
        TransitTransaction tt = objectMapper.readValue(datas, TransitTransaction.class);

        System.out.println(("Node " + nodeLvl + " received transaction: " + tt.getCryptedTransaction()));
        if (transactionUtils.checkTransactionValidity(tt) ){
            broadCastTransactionToNextNodeMembers(tt, nodeLvl + 1);

        } else {
            System.out.println("Transaction violation by one of the consensus member");
            System.out.println(", stop broadcasting to next node, Transaction aborted ! ");
        }

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


    public void broadCastTransactionFirstNode(Integer nodeLvl) throws Exception {

        TransitTransaction tt = transactionUtils.setTransitTransaction();
        this.memberPerNode.get(nodeLvl).forEach(
                member -> {
                    try {
                        Socket socket = new Socket(this.host, member);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.write(GenericObjectConvert.objectToString(tt));
                        PrintWriter writer = new PrintWriter(out, true);
                        writer.println();
                    } catch (Exception e) {
                        System.out.println("Error while broadcasting transaction to member " + member + ": " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
    }

    public void broadCastTransactionToNextNodeMembers(TransitTransaction tt, Integer nodeLvl) {

        if (nodeLvl > nbOfNode) {
            System.out.println("Send transaction to block chain server , transaction: " + tt.getCryptedTransactionHash());
            // submit to the server
        } else {
            this.memberPerNode.get(nodeLvl).forEach(
                    member -> {
                        try {
                            Socket socket = new Socket(this.host, member);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            String cryptedTransaction2String = GenericObjectConvert.objectToString(tt);
                            out.write(cryptedTransaction2String);
                            PrintWriter writer = new PrintWriter(out, true);
                            writer.println();
                        } catch (Exception e) {
                            System.out.println("Error while broadcasting transaction to member " + member + ": " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
        }

    }

    public void dispatchTransactionOnNodes() throws Exception {
        this.broadCastTransactionFirstNode(1);
    }

    public void broadCastCryptedTransaction() {

    }


}
