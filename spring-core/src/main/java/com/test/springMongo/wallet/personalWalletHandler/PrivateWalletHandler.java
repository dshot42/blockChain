package com.test.springMongo.wallet.personalWalletHandler;

import com.chiffrement.ChiffrementUtils;
import com.test.springMongo.models.PrivateWallet;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.GenericObjectConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

@Component
public class PrivateWalletHandler {

    public static String DIRECTORY = "./src/main/resources/";

    public String id;

    public String address;

    private static byte[] walletPrivateKey = new byte[]{-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41, -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11, -8, 60, 69, 38, -33, 78, 55, -65, 104};

    private String filePath;



    public PrivateWalletHandler() {
    }

    public PrivateWalletHandler(String id) {
        this.id = id;
        this.address = address;
        this.filePath = "C:\\Users\\Come\\IdeaProjects\\crypto-block-chain\\spring-core\\src\\main\\resources\\" + "wallet\\wallet" + this.id + ".txt";
        ;
    }

    public boolean testWallet () {
        PrivateWallet privateWallet = getWallet();
        WalletService.getAmount(privateWallet);
        return WalletService.checkIntegrity(privateWallet);
    }

    public PrivateWallet getWallet() {

        File file = new File(filePath);

        String walletData = readWalletFile(file);

        PrivateWallet personnalWallet = null;

        if (walletData.isEmpty()) {
            //create new unique wallet
            try {
                personnalWallet = new PrivateWallet("127.0.0.1:4444", id);
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

    public void insertNewTransaction( Transaction transaction) throws Exception {
       PrivateWallet privateWallet =  getWallet();
        List<Transaction> oldTransac = privateWallet.getTransactions();

        if (oldTransac == null) oldTransac = new LinkedList<>();

        oldTransac.add(transaction);
        privateWallet.setTransactions(oldTransac);

        String filePath = "C:\\Users\\Come\\IdeaProjects\\crypto-block-chain\\spring-core\\src\\main\\resources\\" + "wallet\\wallet" + this.id + ".txt";

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
        BufferedWriter  fileWriter = new BufferedWriter(new FileWriter(file, false));
        fileWriter.write(cryptedWall);
        fileWriter.close();
        return personnalWallet;
    }


}

