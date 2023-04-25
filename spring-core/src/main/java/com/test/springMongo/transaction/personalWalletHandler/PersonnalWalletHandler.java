package com.test.springMongo.transaction.personalWalletHandler;

import com.chiffrement.ChiffrementUtils;
import com.test.springMongo.models.PersonnalWallet;
import com.test.springMongo.transaction.consensus.nodeThreads.utils.GenericObjectConvert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.util.Scanner;

public class PersonnalWalletHandler implements Runnable {

    public static String DIRECTORY = "./src/main/resources/";

    public String name;

    public String address;

    private static byte[] walletPrivateKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};

    public PersonnalWalletHandler(String name, String address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public void run() {
        String filePath = "C:\\Users\\Come\\IdeaProjects\\crypto-block-chain\\spring-core\\src\\main\\resources\\" + "wallet\\wallet" + name + ".txt";

        File file = new File(filePath);

        String walletData = readWalletFile(file);

        if (walletData.isEmpty()) {
            //create new unique wallet
            try {
                PersonnalWallet personnalWallet = new PersonnalWallet("127.0.0.1:4444", ChiffrementUtils.generateHashKey(name + "wallet"));
                updateWallet(file, personnalWallet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                PersonnalWallet personnalWallet = PersonnalWallet.class.cast(
                        GenericObjectConvert.stringToObject(
                                ChiffrementUtils.decryptAES(walletData, walletPrivateKey),
                                PersonnalWallet.class));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void saveWallet(File file, PersonnalWallet personnalWallet) throws Exception {
        String cryptedWall = ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(personnalWallet), walletPrivateKey);
        FileWriter fw = new FileWriter(file, false);
        fw.write(cryptedWall);
        fw.close();
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
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return data;
    }

    public static String getAbsolutePathJar() {
        String path = null;
        try {
            path = PersonnalWallet.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            path = path.substring(1, path.length() - 1);
            String toRemove = path.substring(path.lastIndexOf("/"), path.length());
            path = path.substring(0, path.length() - toRemove.length()) + "/";
            return path;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateWallet(File file, PersonnalWallet personnalWallet) throws Exception {
        String cryptedWall = ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(personnalWallet), walletPrivateKey);

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(cryptedWall);
        fileWriter.close();
    }

}

