package client.wallet.service;

import org.springframework.stereotype.Service;
import vendor.models.PrivateWallet;
import vendor.models.TransitTransaction;
import vendor.transaction.service.InitTransactionDetails;
import vendor.transaction.service.PrivateWalletHandler;
import vendor.utils.GenericObjectConvert;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;


@Service
public class TransactionService {

    public static void sendAskTransaction() throws Exception { // socket send directely ! ! !
        TransitTransaction askTransaction = generateAckTransaction();
        String json = GenericObjectConvert.objectToString(askTransaction);

        Socket socket = new Socket(askTransaction.getReceiverAddress().getAddress().split(":")[0], Integer.parseInt(askTransaction.getReceiverAddress().getAddress().split(":")[1]));
        OutputStream output = socket.getOutputStream();

        output.write(json.getBytes());
        PrintWriter writer = new PrintWriter(output, true);
        writer.println();
    }

    public static TransitTransaction generateAckTransaction() {
        TransitTransaction askTransaction = new TransitTransaction();
        askTransaction.setReceiverAddress(InitTransactionDetails.personnalWallet);
        System.out.println(" todo generateAckTransaction " + InitTransactionDetails.personnalWallet);
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitTransactionDetails.remoteWallet.getAddress(), InitTransactionDetails.remoteWallet.getWalletId());
        PrivateWallet myPrivateWallet = privateWalletHandler.getWallet();
        askTransaction.setSenderAddress(privateWalletHandler.mapPrivateToPublicWaller(myPrivateWallet));
        askTransaction.setDateTime(LocalDateTime.now().toString());
        askTransaction.setState("SYN");
        askTransaction.setAmount(InitTransactionDetails.transacAmount);
        return askTransaction;
    }

}
