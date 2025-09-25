package blockChain.transaction.seller;

import blockChain.nodeThreads.utils.TransactionUtils;
import blockChain.transaction.consensus.ConsensusUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vendor.models.TransitTransaction;
import vendor.transaction.service.InitTransactionDetails;
import vendor.transaction.service.PrivateWalletHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Service
public class AckAndReceiveTransactionProcess implements Runnable { // processus du vendeur

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static final byte[] walletKey = new byte[]
            {-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41,
                    -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11,
                    -8, 60, 69, 38, -33, 78, 55, -65, 104};
    @Autowired
    TransactionUtils nodeUtils;

    @Override
    public void run() {
        try {
            PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitTransactionDetails.remoteWallet.getAddress(), InitTransactionDetails.remoteWallet.getWalletId());
            privateWalletHandler.getWallet();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        socketClientStart();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void socketClientStart() throws Exception {
        serverSocket = new ServerSocket(Integer.parseInt(InitTransactionDetails.remoteWallet.getAddress().split(":")[1]));
        while (true) {
            clientSocket = serverSocket.accept(); // attente de la transaction du buyer
            // thread blocké en mode listener
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            TransitTransaction cryptedTransaction = nodeUtils.jsonToCryptedTransaction(in.readLine());

            if (cryptedTransaction.getState().equals("ACK")) {//  state = feedback
                ConsensusUtils.systemConsensusAckFeedBackTransactionPersisted(InitTransactionDetails.remoteWallet, walletKey, nodeUtils, cryptedTransaction);
                System.out.println("{ACK} Fin de la transaction par Consensus ! (RECEIVER)");
                break;
            } else if (cryptedTransaction.getState().equals("SYNACK")) {
                String thisHash = nodeUtils.hashTransaction(cryptedTransaction.getCryptedTransaction());
                System.out.println("{SYNACK} SENDTRASACTION recive");
                if (nodeUtils.checkValidation(cryptedTransaction.getCryptedTransactionHash(), thisHash)) {
                    //  doTransaction(cryptedTransaction);
                } else
                    throw new Exception("Transaction validation failed, hash mismatch ! ");
            }
        }
        System.out.println("Transaction by Consensus process ending with SUCCESS  ! ");
    }


}
