package client.wallet.handler.personalWalletHandler;

import blockChain.chiffrement.ChiffrementUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import vendor.models.PrivateWallet;
import vendor.models.PublicWallet;
import vendor.models.Transaction;
import vendor.utils.GenericObjectConvert;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class PrivateWalletHandler {

    public final static String DIRECTORY = "/private-wallet/wallet/";

    public String walletId;

    public String address;

    private static byte[] walletPrivateKey = new byte[]{-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41, -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11, -8, 60, 69, 38, -33, 78, 55, -65, 104};

    private String filePath;

     private static final String ROOTPROJECT = Paths.get("").toAbsolutePath().getParent().toString();

    public PrivateWalletHandler() {
    }


    public PrivateWalletHandler(String id) {
        this.walletId = id;
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
        return personnalWallet;
    }

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

