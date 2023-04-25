package com.test.springMongo.transaction.seller;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springMongo.models.CryptedTransaction;
import com.test.springMongo.models.PublicWallet;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.GenericObjectConvert;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.NodeUtils;
import com.test.springMongo.transaction.initTransaction.initPublicWallet.InitWallet;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
public class AskAndReceiveTransactionProcess implements Runnable { // processus du vendeur

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;


    @Autowired
    NodeUtils nodeUtils;

    private static byte[] privateKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};

    @Override
    public void run() {
        try {
            sendAskTransaction();

            ExecutorService executor
                    = Executors.newSingleThreadExecutor();

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

    public CryptedTransaction generateAckTransaction() {
        CryptedTransaction askTransaction = new CryptedTransaction();
        askTransaction.setReceiverAddress(InitWallet.buyerWallet);
        askTransaction.setSenderAddress(InitWallet.sellerWallet);
        askTransaction.setDateTime(LocalDateTime.now().toString());
        askTransaction.setKey(privateKey);
        return askTransaction;
    }

    public void sendAskTransaction() throws Exception { // socket send directely ! ! !
        CryptedTransaction askTransaction = generateAckTransaction();
        String json = GenericObjectConvert.objectToString(askTransaction);

        Socket socket = new Socket(askTransaction.getReceiverAddress().getAddress().split(":")[0], Integer.parseInt(askTransaction.getReceiverAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();

        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

    public void doTransaction(CryptedTransaction cryptedTransaction) { // jury final // seller => pousser sur la block chaine apres validation
        try {
            String transaction2string = ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), privateKey);
            ObjectMapper objectMapper = new ObjectMapper();
            Transaction transaction = objectMapper.readValue(transaction2string, Transaction.class);

            transaction.setHash(ChiffrementUtils.generateHashKey(transaction.toString()));
            sendTransactionToBlockChain(transaction);
            // credit // debit wallet !
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(InitWallet.sellerWallet.getAddress().split(":")[1]));
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        CryptedTransaction cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());
        String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());

        if (nodeUtils.checkValidation(cryptedTransaction.getHash(), thisHash))
            doTransaction(cryptedTransaction);

    }


    public void sendTransactionToBlockChain(Transaction transaction) throws Exception {

        StringEntity entity = new StringEntity(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction)),
                ContentType.APPLICATION_FORM_URLENCODED);

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8090/MongoDb/BlockChain");
        request.addHeader("content-type", "application/json");
        request.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity respEntity = response.getEntity();
            System.out.println("send transaction to the block chain system ," + respEntity.getContent());
            String data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                    StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));


        } catch (Exception e) {
            System.out.println("Erreur lors de la communication avec le serveur, ");
        }
    }

}
