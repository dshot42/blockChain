package blockChain.nodeThreads.utils;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.nodeThreads.RunnableConsensusMemberThreadProcess;
import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.stereotype.Component;
import vendor.models.PublicWallet;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Component
public class TransactionUtils {

    public static byte[] transactionPrivateKey = ChiffrementUtils.systemKey; // la clef de chiffrement des transactions
    public static int nodeValidatorLvl = 5; // 5 membres (thread) relais

    public static List<String> memberlreadyDefined = new LinkedList<>();



    public String hashTransaction(String transactionasString) throws Exception {
        return ChiffrementUtils.generateHashKey(transactionasString);
    }

    public TransitTransaction jsonToCryptedTransaction(String datas) throws JsonProcessingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        ObjectMapper objectMapper = new ObjectMapper();
        TransitTransaction transaction = objectMapper.readValue(datas, TransitTransaction.class);
        return transaction;
    }

    public boolean checkValidation(String masterHash, String myhash) {
        return masterHash.equals(myhash);
    }

    public void socketEmitToNextThread(String member, String cryptedTransaction2String) throws Exception {
        System.out.println("next ! " + member);
        Socket socket = new Socket(member.split(":")[0], Integer.parseInt(member.split(":")[1]));
        OutputStream output = socket.getOutputStream();
        byte[] data = cryptedTransaction2String.getBytes();
        output.write(data);
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
        System.out.println("Send Crypted transaction to member : " + member);
    }


    public String getRandomNextNodeMember() {
        Random rand = new Random();
        String newMember = "127.0.0.1:" + (String.valueOf(rand.nextInt(999) + 5000));
        if (memberlreadyDefined.contains(newMember))
            return getRandomNextNodeMember();

        memberlreadyDefined.add(newMember);
        return newMember;
    }

    public void startNextNodeMemberThread(String thisMember) throws Exception {
        String nextMember = getRandomNextNodeMember();
        RunnableConsensusMemberThreadProcess nodeMember = new RunnableConsensusMemberThreadProcess(thisMember, nextMember, this);
        Thread t = new Thread(nodeMember);
        t.start();
    }

    public void emitCryptedTransactionOnNode(String cryptedTransactiondata) throws Exception {
        String firstMember = getRandomNextNodeMember();
        startNextNodeMemberThread(firstMember);
        Thread.sleep(100); //   wait for the thread to start
        socketEmitToNextThread(firstMember, cryptedTransactiondata);
    }

    public static void persistTransactionOnWallet(TransitTransaction cryptedTransaction, byte[] privateKey, PublicWallet publicWallet) throws Exception {
        System.out.println("persistTransactionOnWallet => Transaction received from the consensus system : " + publicWallet.getWalletId());
        Transaction transaction = Transaction.class.cast(GenericObjectConvert
                .stringToObject(ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), ChiffrementUtils.systemKey), Transaction.class));

        persistTransactionOnWallet(transaction, publicWallet);
    }

    public static void persistTransactionOnWallet(Transaction transaction, PublicWallet publicWallet) throws Exception {

        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(publicWallet.getAddress(), publicWallet.getWalletId());
        privateWalletHandler.insertNewTransaction(transaction);
        System.out.println("new Transaction inserted into the user's wallet : " + publicWallet.getWalletId());
    }

    public static Class<?> getClassForName(String element) throws ClassNotFoundException {
        return Class.forName(element);
    }


}
