package blockChain.transaction.consensus;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.nodeThreads.utils.TransactionUtils;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;

import vendor.models.PublicWallet;
import vendor.models.Transaction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsensusUtils {

    public static int numberConsensusMember = 5;

    public static void sendTransactionToBlockChainSystem(String cryptedTransactiondata) throws IOException {
        Socket socket = new Socket(ConsensusThreadProcess.systemSocketAddress.split(":")[0], Integer.parseInt(ConsensusThreadProcess.systemSocketAddress.split(":")[1]));
        OutputStream output = socket.getOutputStream();
        byte[] data = cryptedTransactiondata.getBytes();
        output.write(data);
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
        System.out.println("Transaction sent to the block chain system with success");
    }


    public static void systemConsensusAckFeedBackTransactionPersisted(PublicWallet wallet, byte[] walletKey, TransactionUtils nodeUtils, TransitTransaction transactionContainerToEmit) throws Exception {
        System.out.println("persite on wallet");
        Transaction returnedTransac = Transaction.class.cast(GenericObjectConvert.stringToObject(ChiffrementUtils.decryptAES(transactionContainerToEmit.getCryptedTransaction(), ChiffrementUtils.systemKey), Transaction.class));
        System.out.println("send transaction ImmutableChainedHash : {" + returnedTransac.getImmutableChainedHash() + "} to the block chain system with success ");
        System.out.println("ACK system receive");
        TransactionUtils.persistTransactionOnWallet(returnedTransac, walletKey, wallet);
        System.out.println("Transaction persite sur le wall de : " + wallet.getWalletId());
    }

}
