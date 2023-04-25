package com.test.springMongo.transaction.consensus.nodeThreads.utils;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.test.springMongo.models.CryptedTransaction;
import com.test.springMongo.system.mongoDb.service.ElementRepository;
import com.test.springMongo.system.mongoDb.service.SequenceGeneratorService;
import com.test.springMongo.transaction.consensus.nodeThreads.RunnableThreadProcess;
import com.test.springMongo.transaction.initTransaction.initBlockChain.CreateBlockChain;
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
public class NodeUtils {

    public static int nodeValidatorLvl = 5; // 5 membres (thread) relais

    public static List<String> memberlreadyDefined = new LinkedList<>();

    @Autowired
    ElementRepository elementService;

    @Autowired
    CreateBlockChain createInitBlockChain;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;


    public String hashTransaction(String transactioneas) throws Exception {
        return ChiffrementUtils.generateHashKey(transactioneas);
    }

    public CryptedTransaction jsonToCryptedTransaction(String datas) throws JsonProcessingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        ObjectMapper objectMapper = new ObjectMapper();
        CryptedTransaction transaction = objectMapper.readValue(datas, CryptedTransaction.class);
        return transaction;
    }

    public boolean checkValidation(String masterHash, String myhash) {
        return masterHash.equals(myhash);
    }

    public void socketEmitToNextThread(String member, String cryptedTransaction2String) throws Exception {
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

    public static Class<?> getClassForName(String element) throws ClassNotFoundException {
        return Class.forName(element);
    }
}
