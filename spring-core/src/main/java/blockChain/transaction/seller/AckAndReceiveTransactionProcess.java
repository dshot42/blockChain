package blockChain.transaction.seller;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.PrivateWallet;
import blockChain.transaction.consensus.ConsensusUtils;
import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import blockChain.transaction.nodeThreads.utils.TransactionUtils;
import blockChain.wallet.InitWallet;
import blockChain.wallet.personalWalletHandler.PrivateWalletHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import blockChain.models.Transaction;
import blockChain.models.TransactionContainerToEmit;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
public class AckAndReceiveTransactionProcess implements Runnable { // processus du vendeur

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    private Socket socketOfSeller;

    @Autowired
    TransactionUtils nodeUtils;

    private static TransactionContainerToEmit bufferAcskTransaction;


    private static PrivateWallet privateWallet;

    private static byte[] walletKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};
    private static byte[] transactionPrivateKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};

    @Override
    public void run() {
        try {
            PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitWallet.sellerWallet.getAddress(), InitWallet.sellerWallet.getUniqueWalletId());
            privateWallet = privateWalletHandler.getWallet();

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

    public TransactionContainerToEmit generateAckTransaction() {
        TransactionContainerToEmit askTransaction = new TransactionContainerToEmit();
        askTransaction.setReceiverAddress(InitWallet.buyerWallet);

        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitWallet.sellerWallet.getAddress(), InitWallet.sellerWallet.getUniqueWalletId());
        PrivateWallet myPrivateWallet = privateWalletHandler.getWallet();
        askTransaction.setSenderAddress(privateWalletHandler.mapPrivateToPublicWaller(myPrivateWallet));
        askTransaction.setDateTime(LocalDateTime.now().toString());
        askTransaction.setKey(transactionPrivateKey);
        askTransaction.setState("SYN");
        Random random = new Random();
        askTransaction.setAmount(random.nextInt(1000));
        bufferAcskTransaction = askTransaction;
        return askTransaction;
    }

    public void sendAskTransaction() throws Exception { // socket send directely ! ! !
        TransactionContainerToEmit askTransaction = generateAckTransaction();
        String json = GenericObjectConvert.objectToString(askTransaction);

        Socket socket = new Socket(askTransaction.getReceiverAddress().getAddress().split(":")[0], Integer.parseInt(askTransaction.getReceiverAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();

        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

    public void doTransaction(TransactionContainerToEmit cryptedTransaction) { // jury final // seller => pousser sur la block chaine apres validation
        try {
            String transaction2string = ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), transactionPrivateKey);
            ObjectMapper objectMapper = new ObjectMapper();
            Transaction transaction = objectMapper.readValue(transaction2string, Transaction.class);
            String thisHash = ChiffrementUtils.cryptAES(transaction2string, ChiffrementUtils.systemKey);

            if (bufferAcskTransaction.getAmount() == transaction.getAmount() &&
                    transaction.getSenderAddress().getUniqueWalletId().equals(bufferAcskTransaction.getReceiverAddress().getUniqueWalletId()) &&
                    transaction.getReceiverAddress().getUniqueWalletId().equals(bufferAcskTransaction.getSenderAddress().getUniqueWalletId())) {
              //  transaction.setHash(ChiffrementUtils.cryptAES(thisHash)); // on devrait meme le faire du coté system
                pushTransactionOnBlockChain(transaction);
            } else {
                System.out.println("les informations de transaction sont incorrects ! ");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(InitWallet.sellerWallet.getAddress().split(":")[1]));
        while (true) {
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            TransactionContainerToEmit cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());

            if (cryptedTransaction.getState().equals("ACK")) {//  state = feedback
                ConsensusUtils.systemConsensusAckFeedBackTransactionPersisted(InitWallet.sellerWallet, walletKey, nodeUtils, cryptedTransaction);
                System.out.println("Fin de la transaction par Consensus ! (RECEIVER)");
            } else if (cryptedTransaction.getState().equals("SYNACK")) {
                String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());
                System.out.println("SENDTRASACTION receive");
                if (nodeUtils.checkValidation(cryptedTransaction.getCryptedTransactionHash(), thisHash))
                    doTransaction(cryptedTransaction);

            }
        }

    }


    public void pushTransactionOnBlockChain(Transaction transaction) throws Exception {

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

            TransactionContainerToEmit returnedTransac = TransactionContainerToEmit.class.cast(GenericObjectConvert.stringToObject(data, TransactionContainerToEmit.class));
            System.out.println("send transaction hash : {" + transaction.getHash() + "} to the block chain system with success ");
            emitFeedBackBlockChainToSender(returnedTransac);

        } catch (Exception e) {
            System.out.println("Erreur lors de la communication avec le serveur, POST on block chain ");
        }
    }


    public void emitFeedBackBlockChainToSender(TransactionContainerToEmit cryptedTransaction) throws Exception {

        nodeUtils.persistTransactionOnWallet(cryptedTransaction, transactionPrivateKey, InitWallet.sellerWallet);

        String json = GenericObjectConvert.objectToString(cryptedTransaction);

        Socket socket = new Socket(cryptedTransaction.getSenderAddress().getAddress().split(":")[0], Integer.parseInt(cryptedTransaction.getSenderAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();
        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

}
