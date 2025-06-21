package blockChain.transaction.consensus;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.nodeThreads.utils.TransactionUtils;
import blockChain.transaction.consensus.webSocket.ThreadPoolHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import vendor.utils.GenericObjectConvert;

import vendor.models.PublicWallet;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsensusUtils {

    public static int numberConsensusMember = 5;

    public static void sendTransactionToBlockChainSystem(String cryptedTransactiondata) throws IOException {
        System.out.println(" ConsensusUtils =>Socket system consensus" + ThreadPoolHandler.systemSocketAddress);

        try {
            Socket socket = new Socket(ThreadPoolHandler.systemSocketAddress.split(":")[0], Integer.parseInt(ThreadPoolHandler.systemSocketAddress.split(":")[1]));
            if (socket.isConnected()) {
                OutputStream output = socket.getOutputStream();
                byte[] data = cryptedTransactiondata.getBytes();
                //fixme remonter depuis ici vers le sender
                output.write(data);

                PrintWriter writer = new PrintWriter(output, true);
                writer.println();
            } else {
                System.out.println("sendTransactionToBlockChainSystem => Socket not connected to the system consensus");
            }
        } catch (Exception e) {
            System.out.println("sendTransactionToBlockChainSystem => Error socket  " + e.getMessage());
        }
    }


    public static void systemConsensusAckFeedBackTransactionPersisted(PublicWallet wallet,TransitTransaction transitTransaction) throws Exception {
        System.out.println("persite on wallet");
        Transaction returnedTransac = Transaction.class.cast(GenericObjectConvert.stringToObject(ChiffrementUtils.decryptAES(transitTransaction.getCryptedTransaction(), ChiffrementUtils.systemKey), Transaction.class));
        System.out.println("send transaction ImmutableChainedHash : {" + returnedTransac.getImmutableChainedHash() + "} to the block chain system with success ");
        System.out.println("ACK system receive");
        TransactionUtils.persistTransactionOnWallet(returnedTransac, wallet);
        System.out.println("Transaction persite sur le wall de : " + wallet.getWalletId());
    }

}
