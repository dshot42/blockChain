package blockChain.transaction.nodeThreads.utils;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.PublicWallet;
import blockChain.system.mongoDb.repository.ElementRepository;
import blockChain.system.mongoDb.service.SequenceGeneratorService;
import blockChain.transaction.initTransaction.initBlockChain.CreateBlockChain;
import blockChain.wallet.personalWalletHandler.PrivateWalletHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import blockChain.models.Transaction;
import blockChain.models.TransactionContainerToEmit;
import blockChain.transaction.nodeThreads.RunnableThreadProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public static int nodeValidatorLvl = 5; // 5 membres (thread) relais

    public static List<String> memberlreadyDefined = new LinkedList<>();

    @Autowired
    ElementRepository elementService;

    @Autowired
    CreateBlockChain createInitBlockChain;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    public static byte[] transactionPrivateKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};


    public String hashTransaction(String transactionasString) throws Exception {
        return ChiffrementUtils.generateHashKey(transactionasString);
    }

    public TransactionContainerToEmit jsonToCryptedTransaction(String datas) throws JsonProcessingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        ObjectMapper objectMapper = new ObjectMapper();
        TransactionContainerToEmit transaction = objectMapper.readValue(datas, TransactionContainerToEmit.class);
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
        RunnableThreadProcess nodeMember = new RunnableThreadProcess(thisMember, nextMember, this);
        Thread t = new Thread(nodeMember);
        t.start();
    }


    public void


    emitCryptedTransactionOnNode(String cryptedTransactiondata) throws Exception {
        String firstMember = getRandomNextNodeMember();
        startNextNodeMemberThread(firstMember);
        Thread.sleep(100); // le temps de demarrer la socket d'écoute
        socketEmitToNextThread(firstMember, cryptedTransactiondata);
    }

    public static void persistTransactionOnWallet(TransactionContainerToEmit cryptedTransaction, byte[] privateKey, PublicWallet publicWallet) throws Exception {

        Transaction transaction = Transaction.class.cast(GenericObjectConvert
                .stringToObject(ChiffrementUtils.decryptAES(cryptedTransaction.getCryptedTransaction(), ChiffrementUtils.systemKey), Transaction.class));

        persistTransactionOnWallet(transaction, privateKey, publicWallet);
    }

    public static void persistTransactionOnWallet(Transaction transaction, byte[] privateKey, PublicWallet publicWallet) throws Exception {

        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(publicWallet.getAddress(), publicWallet.getUniqueWalletId());
        privateWalletHandler.insertNewTransaction(transaction);
        System.out.println("new Transaction inserted into the user's wallet : " + publicWallet.getUniqueWalletId());
    }

    public static Class<?> getClassForName(String element) throws ClassNotFoundException {
        return Class.forName(element);
    }


}
