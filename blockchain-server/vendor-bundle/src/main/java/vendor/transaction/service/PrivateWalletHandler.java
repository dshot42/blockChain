package vendor.transaction.service;

import blockChain.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vendor.models.PrivateWallet;
import vendor.models.PublicWallet;
import vendor.models.Transaction;
import vendor.utils.GenericObjectConvert;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PrivateWalletHandler {

    public final static String DIRECTORY = "/private-wallet/wallet/";

    public String walletId;

    public String address;

    public static byte[] walletPrivateKey = new byte[]{-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41, -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11, -8, 60, 69, 38, -33, 78, 55, -65, 104};

    private String filePath;

    private static final String ROOTPROJECT = Paths.get("").toAbsolutePath().getParent().toString();

    public PrivateWalletHandler() {
        this.walletId = "Personnal";
        this.filePath = ROOTPROJECT + DIRECTORY + "wallet" + this.walletId + ".txt";
    }


    public PrivateWalletHandler(String address, String id) {
        this.walletId = id;
        this.address = address;
        System.out.println(ROOTPROJECT);
        this.filePath = ROOTPROJECT + DIRECTORY + "wallet" + this.walletId + ".txt";
    }

    public void testWallet() {
        PrivateWallet privateWallet = getWallet();
        WalletService.getAmount(privateWallet);
        WalletService.checkIntegrity(privateWallet);
    }

    public PrivateWallet refreshWallet() throws Exception {
        PrivateWallet privateWallet = getWallet();
        return persistWallet(new File(filePath), WalletService.bindPublicToPrivateTransaction(WalletService.getAllTransation(privateWallet), privateWallet));
    }

    public PrivateWallet getWallet() throws RuntimeException {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                System.out.println("Creating NEW WALLET ");
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Failed to create wallet file: " + e.getMessage());
            }
        }
        String walletData = readWalletFile(file);
        PrivateWallet personnalWallet = null;
        try {
            if (walletData.isEmpty()) {
                //create new unique wallet
                personnalWallet = new PrivateWallet(address, walletId);
                persistWallet(file, personnalWallet);

            } else {
                personnalWallet = PrivateWallet.class.cast(GenericObjectConvert.stringToObject(
                        ChiffrementUtils.decryptAES(walletData, walletPrivateKey), PrivateWallet.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Fail to persist Wallet , " + e);
        }
        System.out.println(" [SUCCES] get Wallet  : " + personnalWallet.walletId);

        return personnalWallet;
    }

    ///////////////////////////////////
    public static void sendPrivateKey() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonObject = objectMapper.createObjectNode();
        jsonObject.put("walletId", "Personnal");
        jsonObject.put("privateKey",GenericObjectConvert.objectToString(PrivateWalletHandler.walletPrivateKey));

        String jsonString = objectMapper.writeValueAsString(jsonObject);

        StringEntity entity = new StringEntity(jsonString,
                ContentType.APPLICATION_FORM_URLENCODED);

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8090/MongoDb/send/wallet/privatekey");
        request.addHeader("content-type", "application/json");
        request.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity respEntity = response.getEntity();
            String data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                    StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            System.out.println("push private key to the block chain system : " + data);

        } catch (Exception e) {
            System.out.println("Erreur sendPrivateKey() to the blockChain System, " + e);
        }
    }

    public void synchronizeTransaction()  {
        try {
            System.out.println("synchronizeTransaction() : Synchronization of transactions from the block chain system");
            String url = "http://localhost:8090/MongoDb/BlockChain/transation/synchronization";

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Set body parameters
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonObject = objectMapper.createObjectNode();
            jsonObject.put("walletId", "Personnal");

            String jsonString = objectMapper.writeValueAsString(jsonObject);
            // Send POST request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List> response = restTemplate.postForEntity(url, jsonString, List.class);

            try {
                // Print response
                List<String> responseData = (List<String>) response.getBody();
                System.out.println("synchronizeTransaction() : " + responseData.size() + " transactions received from the block chain system");


                ArrayList<Transaction> transactions = (ArrayList<Transaction>) responseData
                        .stream()
                        .map((String line) -> {
                                    try {
                                        return Transaction.class.cast(GenericObjectConvert.stringToObject(ChiffrementUtils.decryptAES(line, PrivateWalletHandler.walletPrivateKey), Transaction.class));
                                    } catch (Exception e) {
                                        throw new RuntimeException("Error cast crypted Transaction when synchronsation ", e);
                                    }
                                }
                        ).collect(Collectors.toList());

                PrivateWallet privateWallet = getWallet();
                privateWallet.setTransactions(transactions);
                persistWallet(new File(filePath), privateWallet);
            } catch (Exception e) {
                System.out.println("Erreur lors de la communication avec le serveur, POST on block chain, " + e);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la synchronisation des transactions, " + e);
        }


        // Print response

    }


    public void synchronizeTransactionOld() throws Exception {


        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonObject = objectMapper.createObjectNode();
        jsonObject.put("walletId", "Personnal");

        String jsonString = objectMapper.writeValueAsString(jsonObject);

        StringEntity entity = new StringEntity(jsonString,
                ContentType.APPLICATION_FORM_URLENCODED);

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8090/MongoDb/BlockChain/transation/synchronization");
        request.addHeader("content-type", "application/json");
        request.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity respEntity = response.getEntity();

            List<String> data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                    StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList());

            System.out.println("List ok " + data.size() + " transactions received from the block chain system");

            ArrayList<Transaction> transactions = (ArrayList<Transaction>) data
                    .stream()
                    .map((String line) -> {
                                try {
                                    return Transaction.class.cast(GenericObjectConvert.stringToObject(ChiffrementUtils.decryptAES(line, PrivateWalletHandler.walletPrivateKey), Transaction.class));
                                } catch (Exception e) {
                                    throw new RuntimeException("Error cast crypted Transaction when synchronsation ", e);
                                }
                            }
                    ).collect(Collectors.toList());

            System.out.println("synchronizeTransaction() : " + transactions.size() + " transactions received from the block chain system");
            PrivateWallet privateWallet = getWallet();
            privateWallet.setTransactions(transactions);
            persistWallet(new File(filePath), privateWallet);

        } catch (Exception e) {
            System.out.println("Erreur lors de la communication avec le serveur, POST on block chain, " + e);
        }
    }


    ///////////////////////////////////////////////


    public void insertNewTransaction(Transaction transaction) throws Exception {
        PrivateWallet privateWallet = getWallet();
        List<Transaction> oldTransac = privateWallet.getTransactions();

        if (oldTransac == null) oldTransac = new LinkedList<>();

        oldTransac.add(transaction);
        privateWallet.setTransactions(oldTransac);

        persistWallet(new File(filePath), privateWallet);
        try {
            emitTransactionToSocketClient(transaction); // send transaction to front
        } catch (Exception e) {
            System.out.println("Node socket client doesn't currently running, just run : crypto-block-chain\\node-client\\launcher.bat  : " + e.getMessage());
        }
    }

    private String readWalletFile(File file) {
        StringBuilder data = new StringBuilder();

        Scanner myReader = null;
        try {
            myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                data.append(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Read wallet failed ");
            e.printStackTrace();
        }
        return data.toString();
    }

    public static String getAbsolutePathJar() {
        String path = null;
        try {
            path = PrivateWallet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            path = path.substring(1, path.length() - 1);
            String toRemove = path.substring(path.lastIndexOf("/"), path.length());
            path = path.substring(0, path.length() - toRemove.length()) + "/";
            return path;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public PrivateWallet persistWallet(File file, PrivateWallet privateWallet) throws Exception {
        String cryptedWall = ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(privateWallet), walletPrivateKey);
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, false));
        fileWriter.write(cryptedWall);
        fileWriter.close();
        return privateWallet;
    }

    public void emitTransactionToSocketClient(Transaction transaction) throws Exception {
        // webSocketClient
        Socket socket = new Socket("127.0.0.1", 3000); //TCP Socket to my client node
        socket.setSendBufferSize((int) 1e7); // // 100Mo buffer
        OutputStream output = socket.getOutputStream();
        byte[] data = GenericObjectConvert.objectToString(transaction).getBytes();

        output.write(data);
        output.flush(); // flush the output stream
    }

    public void emitTransactionToSocketClient() throws Exception {
        // webSocketClient
        Socket socket = new Socket("127.0.0.1", 3000); //TCP Socket to my client node
        socket.setSendBufferSize((int) 1000);
        OutputStream output = socket.getOutputStream();
        byte[] data = GenericObjectConvert.objectToString("update transaction").getBytes();

        output.write(data);
        output.flush(); // flush the output stream
    }


    public PublicWallet mapPrivateToPublicWaller(PrivateWallet privateWallet) {
        if (privateWallet.getTransactions() == null) {
            return new PublicWallet(privateWallet.getAddress()
                    , privateWallet.getWalletId());
        }

        List<Transaction> publicTransacList = privateWallet.getTransactions().stream().map(t -> {
            Transaction publicTransac = new Transaction();
            publicTransac.setHash(t.getHash());
            publicTransac.setBlockHash(t.getBlockHash());
            publicTransac.setImmutableChainedHash(t.getImmutableChainedHash());
            return publicTransac;
        }).collect(Collectors.toList());

        System.out.println(" ici probleme, il faut que cela puisse etre autre que personnal !!! " + privateWallet.getWalletId());
        return new PublicWallet(privateWallet.getAddress()
                , privateWallet.getWalletId(), publicTransacList);
    }


    public static void clearWallet() throws IOException {
        try {
            FileUtils.cleanDirectory(new File(ROOTPROJECT + DIRECTORY));
            System.out.println("Wallet cleared successfully.");
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

}

