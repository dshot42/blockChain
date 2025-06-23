package blockChain.transaction.seller;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.nodeThreads.utils.TransactionUtils;
import blockChain.transaction.consensus.ConsensusUtils;

import org.springframework.stereotype.Service;
import vendor.models.TransitTransaction;
import vendor.transaction.service.InitTransactionDetails;
import vendor.transaction.service.PrivateWalletHandler;
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


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
    private static PrivateWallet privateWallet;

    private static byte[] walletKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};


    @Override
    public void run() {
        try {
            PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitTransactionDetails.remoteWallet.getAddress(), InitTransactionDetails.remoteWallet.getWalletId());
            privateWallet = privateWalletHandler.getWallet();

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

    public static TransitTransaction generateAckTransaction() throws URISyntaxException {
        TransitTransaction askTransaction = new TransitTransaction();
        askTransaction.setReceiverAddress(InitTransactionDetails.personnalWallet);

        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitTransactionDetails.remoteWallet.getAddress(), InitTransactionDetails.remoteWallet.getWalletId());
        PrivateWallet myPrivateWallet = privateWalletHandler.getWallet();
        askTransaction.setSenderAddress(privateWalletHandler.mapPrivateToPublicWaller(myPrivateWallet));
        askTransaction.setDateTime(LocalDateTime.now().toString());
        askTransaction.setKey(TransactionUtils.transactionPrivateKey);
        askTransaction.setState("SYN");
        askTransaction.setAmount(InitTransactionDetails.transacAmount);
        bufferAcskTransaction = askTransaction;
        return askTransaction;
    }

    public static void sendAskTransaction() throws Exception { // socket send directely ! ! !
        TransitTransaction askTransaction = generateAckTransaction();
        String json = GenericObjectConvert.objectToString(askTransaction);

        Socket socket = new Socket(askTransaction.getReceiverAddress().getAddress().split(":")[0], Integer.parseInt(askTransaction.getReceiverAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();

        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

    public void doTransaction(TransitTransaction cryptedTransaction) { // jury final // seller => pousser sur la block chaine apres validation
        try {
            String transaction2string = ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), TransactionUtils.transactionPrivateKey);
            ObjectMapper objectMapper = new ObjectMapper();
            Transaction transaction = objectMapper.readValue(transaction2string, Transaction.class);
            String thisHash = ChiffrementUtils.cryptAES(transaction2string, ChiffrementUtils.systemKey);
            transaction.setHash(thisHash);
            if (bufferAcskTransaction.getAmount() == transaction.getAmount() &&
                    transaction.getSenderAddress().getWalletId().equals(bufferAcskTransaction.getReceiverAddress().getWalletId()) &&
                    transaction.getReceiverAddress().getWalletId().equals(bufferAcskTransaction.getSenderAddress().getWalletId())) {
                  //  sendTransactionToBlockChain(transaction);
            } else {
                System.out.println("les informations de transaction sont incorrects ! ");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(InitTransactionDetails.remoteWallet.getAddress().split(":")[1]));
        while (true) {
            clientSocket = serverSocket.accept(); // attente de la transaction du buyer
            // thread blocké en mode listener
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            TransitTransaction cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());

            if (cryptedTransaction.getState().equals("ACK")) {//  state = feedback
                ConsensusUtils.systemConsensusAckFeedBackTransactionPersisted(InitTransactionDetails.remoteWallet, walletKey, nodeUtils, cryptedTransaction);
                System.out.println("Fin de la transaction par Consensus ! (RECEIVER)");
                break;
            } else if (cryptedTransaction.getState().equals("SYNACK")) {
                String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());
                System.out.println("SENDTRASACTION recive");
                if (nodeUtils.checkValidation(cryptedTransaction.getCryptedTransactionHash(), thisHash)) {
                  //  doTransaction(cryptedTransaction);
                } else
                    throw new Exception("Transaction validation failed, hash mismatch ! ");
            }
        }
        System.out.println("Transaction with Consensor process finished  ! ");

    }


    public void sendTransactionToBlockChain(Transaction transaction) throws Exception {

        StringEntity entity = new StringEntity(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction)),
                ContentType.APPLICATION_FORM_URLENCODED);

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8090/MongoDb/BlockChain/transaction");
        request.addHeader("content-type", "application/json");
        request.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity respEntity = response.getEntity();
            String data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                    StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            TransitTransaction returnedTransac = TransitTransaction.class.cast(GenericObjectConvert.stringToObject(data, TransitTransaction.class));
            System.out.println("push transaction to the block chain system : web service ");
            emitFeedBackBlockChainToSender(returnedTransac);

        } catch (Exception e) {
            System.out.println("Erreur lors de la communication avec le serveur, POST on block chain, "+ e);
        }
    }


    public void emitFeedBackBlockChainToSender(TransitTransaction cryptedTransaction) throws Exception {

        nodeUtils.persistTransactionOnWallet(cryptedTransaction, TransactionUtils.transactionPrivateKey, InitTransactionDetails.personnalWallet);
        nodeUtils.persistTransactionOnWallet(cryptedTransaction, TransactionUtils.transactionPrivateKey, InitTransactionDetails.remoteWallet);


        String json = GenericObjectConvert.objectToString(cryptedTransaction);

        Socket socket = new Socket(cryptedTransaction.getSenderAddress().getAddress().split(":")[0], Integer.parseInt(cryptedTransaction.getSenderAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();
        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

}
