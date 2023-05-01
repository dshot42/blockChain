package blockChain.wallet.personalWalletHandler;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.PrivateWallet;
import blockChain.models.PublicWallet;
import blockChain.models.Transaction;
import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import org.springframework.stereotype.Component;

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

    public static String DIRECTORY = "/src/main/resources/";

    public String id;

    public String address;

    private static byte[] walletPrivateKey = new byte[]{-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41, -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11, -8, 60, 69, 38, -33, 78, 55, -65, 104};

    private String filePath;


    public PrivateWalletHandler() {
    }


    public PrivateWalletHandler(String id) {
        this.id = id;
        this.filePath = Paths.get("").toAbsolutePath() + DIRECTORY + "wallet/wallet" + this.id + ".txt";
    }

    public PrivateWalletHandler(String address, String id) {
        this.id = id;
        this.address = address;
        this.filePath = Paths.get("").toAbsolutePath() + DIRECTORY + "wallet/wallet" + this.id + ".txt";
    }

    public boolean testWallet() throws Exception {
        PrivateWallet privateWallet = getWallet();
        WalletService.getAmount(privateWallet);
        privateWallet = persistWallet(new File(filePath), WalletService.bindPublicToPrivateTransaction(WalletService.getAllTransation(privateWallet), privateWallet));
        return WalletService.checkIntegrity(privateWallet);
    }

    public PrivateWallet refreshWallet() throws Exception { // on va send 2 fois une fois en webservice et une fois en socket : petit bug ! on devrait  passer emit socket en callbak
        PrivateWallet privateWallet = getWallet();
        return persistWallet(new File(filePath), WalletService.bindPublicToPrivateTransaction(WalletService.getAllTransation(privateWallet), privateWallet));
    }

    public PrivateWallet getWallet() {

        File file = new File(filePath);
        String walletData = readWalletFile(file);
        PrivateWallet personnalWallet = null;

        if (walletData.isEmpty()) {
            //create new unique wallet
            try {
                personnalWallet = new PrivateWallet(address, id);
                persistWallet(file, personnalWallet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                personnalWallet = PrivateWallet.class.cast(GenericObjectConvert.stringToObject(ChiffrementUtils.decryptAES(walletData, walletPrivateKey), PrivateWallet.class));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    }

    private String readWalletFile(File file) {
        String data = "";

        Scanner myReader = null;
        try {
            myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Read wallet failed ");
            e.printStackTrace();
        }
        return data;
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


    public PrivateWallet persistWallet(File file, PrivateWallet personnalWallet) throws Exception {
        String cryptedWall = ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(personnalWallet), walletPrivateKey);
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, false));
        fileWriter.write(cryptedWall);
        fileWriter.close();
        socketEmitRefreshWalletAfterTransaction(personnalWallet);
        // todo ! ! ! ! ! !
        return personnalWallet;
    }

    public void socketEmitRefreshWalletAfterTransaction(PrivateWallet privateWallet) throws Exception {

        Socket socket = new Socket("127.0.0.1", 3000);
        OutputStream output = socket.getOutputStream();
        byte[] data = GenericObjectConvert.objectToString(privateWallet).getBytes();
        output.write(data);
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
        System.out.println("Send wallet data to client front end ");

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


}

