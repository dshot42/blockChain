package blockChain.transaction.seller;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.nodeThreads.utils.TransactionUtils;
import blockChain.transaction.consensus.ConsensusUtils;
import client.wallet.handler.personalWalletHandler.InitTransactionDetails;
import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import vendor.utils.GenericObjectConvert;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import vendor.models.PrivateWallet;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AckAndReceiveTransactionProcess implements Runnable { // processus du vendeur

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private Socket socketOfSeller;

    @Autowired
    TransactionUtils nodeUtils;

    private static TransitTransaction bufferAcskTransaction;



    @Override
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

    public static TransitTransaction generateAckTransaction() throws JsonProcessingException {
        TransitTransaction askTransaction = new TransitTransaction();
        askTransaction.setReceiverAddress(InitTransactionDetails.personnalWallet);

        PrivateWalletHandler privatePrivateWalletHandler = new PrivateWalletHandler(InitTransactionDetails.remoteWallet.getAddress(), InitTransactionDetails.remoteWallet.getWalletId());
        PrivateWallet myPrivateWallet = privatePrivateWalletHandler.getWallet();
        askTransaction.setSenderAddress(privatePrivateWalletHandler.mapPrivateToPublicWaller(myPrivateWallet));
        askTransaction.setDateTime(LocalDateTime.now().toString());
        askTransaction.setState("SYN");

        askTransaction.setAmount((float)InitTransactionDetails.transacAmount);
        bufferAcskTransaction = askTransaction;
        return askTransaction;
    }

    public static void sendAskTransaction() throws Exception { // init process of transaction message !
        TransitTransaction askTransaction = generateAckTransaction();
        String json = GenericObjectConvert.objectToString(askTransaction);

        Socket socket = new Socket(askTransaction.getReceiverAddress().getAddress().split(":")[0], Integer.parseInt(askTransaction.getReceiverAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();

        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

    public void doTransaction(TransitTransaction cryptedTransaction) throws Exception { // jury final // seller => pousser sur la block chaine apres validation

            String transaction2string = ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), ChiffrementUtils.systemKey);
            ObjectMapper objectMapper = new ObjectMapper();
            Transaction transaction = objectMapper.readValue(transaction2string, Transaction.class);
            String thisHash = ChiffrementUtils.cryptAES(transaction2string, ChiffrementUtils.systemKey);

            if (bufferAcskTransaction.getAmount() == transaction.getAmount() &&
                    transaction.getSenderAddress().getWalletId().equals(bufferAcskTransaction.getReceiverAddress().getWalletId()) &&
                    transaction.getReceiverAddress().getWalletId().equals(bufferAcskTransaction.getSenderAddress().getWalletId())) {
                     sendTransactionOnBlockChain(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction)));
            } else {
                System.out.println("les informations de transaction sont incorrects ! ");
            }


    }

    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(InitTransactionDetails.remoteWallet.getAddress().split(":")[1]));
        while (true) {
            System.out.println("Attente de la transaction du buyer ...");
            clientSocket = serverSocket.accept(); // attente de la transaction du buyer
            // thread blocké en mode listener
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            TransitTransaction cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());

            if (cryptedTransaction.getState().equals("SYNACK")) {//  state = feedback
                String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());
                System.out.println("SENDTRASACTION recive");
                if (nodeUtils.checkValidation(cryptedTransaction.getCryptedTransactionHash(), thisHash)) {
                    doTransaction(cryptedTransaction);
                } else
                    throw new Exception("Transaction validation failed, hash mismatch ! ");

            } else if (cryptedTransaction.getState().equals("ACK")) {
                ConsensusUtils.systemConsensusAckFeedBackTransactionPersisted(InitTransactionDetails.remoteWallet, cryptedTransaction);
                System.out.println("Fin de la transaction par Consensus ! (RECEIVER)");
                break;
            }
        }

        System.out.println("Transaction with Consensor process finished  ! ");

    }


    public void sendTransactionOnBlockChain(String cryptedTransac) throws Exception {
        // problem here

        StringEntity entity = new StringEntity(cryptedTransac,
                ContentType.APPLICATION_FORM_URLENCODED);

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8090/MongoDb/BlockChain/transaction");
        request.addHeader("content-type", "application/json");
        request.setEntity(entity);
        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity respEntity = response.getEntity();

        System.out.println("BlockChain/transaction RESPONSE : " + response.getStatusLine().getStatusCode());

        String data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        TransitTransaction returnedTransac = TransitTransaction.class.cast(GenericObjectConvert.stringToObject(data, TransitTransaction.class));
        emitFeedBackBlockChainToSender(returnedTransac);
    }


    public void emitFeedBackBlockChainToSender(TransitTransaction cryptedTransaction) throws Exception {
        System.out.println(cryptedTransaction.getSenderAddress());
        nodeUtils.persistTransactionOnWallet(cryptedTransaction,cryptedTransaction.getKey(), InitTransactionDetails.remoteWallet);

        String json = GenericObjectConvert.objectToString(cryptedTransaction);

        // fixme => pas d'addresse en feedback , deja checker ce qui part /transaction
        System.out.println("Transaction feedback to sender : "+ cryptedTransaction.getSenderAddress().getAddress());
        Socket socket = new Socket(cryptedTransaction.getSenderAddress().getAddress().split(":")[0], Integer.parseInt(cryptedTransaction.getSenderAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();
        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

}
