package com.test.springMongo.transaction.consensus;

import com.chiffrement.ChiffrementUtils;
import com.test.springMongo.models.PublicWallet;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.models.TransactionContainerToEmit;
import com.test.springMongo.transaction.nodeThreads.utils.GenericObjectConvert;
import com.test.springMongo.transaction.nodeThreads.utils.TransactionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsensusUtils {

    public static String systemAddress = "127.0.0.1:9999";

    public static void sendTransactionToBlockChainSystem(String cryptedTransactiondata) throws IOException {
        Socket socket = new Socket(ConsensusThreadProcess.systemSocketAddress.split(":")[0], Integer.parseInt(ConsensusThreadProcess.systemSocketAddress.split(":")[1]));
        OutputStream output = socket.getOutputStream();
        byte[] data = cryptedTransactiondata.getBytes();
        output.write(data);
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }


    public static void systemConsensusAckFeedBackTransactionPersisted(PublicWallet wallet, byte[] walletKey, TransactionUtils nodeUtils, TransactionContainerToEmit transactionContainerToEmit) throws Exception {
        System.out.println("persite on wallet");
        Transaction returnedTransac = Transaction.class.cast(GenericObjectConvert.stringToObject(ChiffrementUtils.decryptAES(transactionContainerToEmit.getCryptedTransaction(), ChiffrementUtils.systemKey), Transaction.class));
        System.out.println("send transaction hash : {" + returnedTransac.getHash() + "} to the block chain system with success ");
        System.out.println("ACK system receive");
        nodeUtils.persistTransactionOnWallet(returnedTransac, walletKey, wallet);
        System.out.println("Transaction persité sur le wall de : " + wallet.getUniqueWalletId());
    }

}
