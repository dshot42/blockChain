package blockChain.wallet.personalWalletHandler;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import vendor.models.PrivateWallet;
import vendor.models.PublicWallet;
import vendor.models.Transaction;

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

    public static String DIRECTORY = "/src/main/resources/wallet/";

    public String id;

    public String address;

    private static byte[] walletPrivateKey = new byte[]{-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41, -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11, -8, 60, 69, 38, -33, 78, 55, -65, 104};

    private String filePath;


    public PrivateWalletHandler() {
    }


    public PrivateWalletHandler(String id) {
        this.id = id;
        this.filePath = Paths.get("").toAbsolutePath() + DIRECTORY + "wallet" + this.id + ".txt";
    }

    public PrivateWalletHandler(String address, String id) {
        this.id = id;
        this.address = address;
        this.filePath = Paths.get("").toAbsolutePath() + DIRECTORY + "wallet" + this.id + ".txt";
    }

    public boolean testWallet() throws Exception {
        PrivateWallet privateWallet = getWallet();
        WalletService.getAmount(privateWallet);
        return WalletService.checkIntegrity(privateWallet);
    }

    public PrivateWallet refreshWallet() throws Exception {
        PrivateWallet privateWallet = getWallet();
        return persistWallet(new File(filePath), WalletService.bindPublicToPrivateTransaction(WalletService.getAllTransation(privateWallet), privateWallet));
    }

    public PrivateWallet getWallet() throws RuntimeException {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create wallet file: " + e.getMessage());
            }
        }
        String walletData = readWalletFile(file);
        PrivateWallet personnalWallet = null;
        try {
            if (walletData.isEmpty()) {
                //create new unique wallet
                personnalWallet = new PrivateWallet(address, id);
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
            socketEmitRefreshWalletAfterTransaction(transaction); // send transaction to front
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

    public void socketEmitRefreshWalletAfterTransaction(Transaction transaction) throws Exception {
        Socket socket = new Socket("127.0.0.1", 3000); //TCP Socket to node client
        socket.setSendBufferSize((int) 1e7); // // 100Mo
        OutputStream output = socket.getOutputStream();
        byte[] data = GenericObjectConvert.objectToString(transaction).getBytes();

        output.write(data);
        output.flush(); // flush the output stream
    }


    public PublicWallet mapPrivateToPublicWaller(PrivateWallet privateWallet) {
        if (privateWallet.getTransactions() == null) {
            return new PublicWallet(privateWallet.getAddress()
                    , privateWallet.getUniqueWalletId());
        }

        List<Transaction> publicTransacList = privateWallet.getTransactions().stream().map(t -> {
            Transaction publicTransac = new Transaction();
            publicTransac.setHash(t.getHash());
            publicTransac.setBlockHash(t.getBlockHash());
            publicTransac.setImmutableChainedHash(t.getImmutableChainedHash());
            return publicTransac;
        }).collect(Collectors.toList());

        return new PublicWallet(privateWallet.getAddress()
                , privateWallet.getUniqueWalletId(), publicTransacList);
    }


    public static void clearWallet() throws IOException {
        try {
            FileUtils.cleanDirectory(new File(Paths.get("").toAbsolutePath() + DIRECTORY));
            System.out.println("Wallet cleared successfully.");
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

}

