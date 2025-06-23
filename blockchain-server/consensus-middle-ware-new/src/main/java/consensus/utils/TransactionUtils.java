package consensus.utils;

import blockChain.chiffrement.ChiffrementUtils;

import vendor.transaction.service.InitTransactionDetails;
import vendor.transaction.service.PrivateWalletHandler;
import org.springframework.stereotype.Component;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;

import java.time.LocalDateTime;


@Component
public class TransactionUtils {


    public String hashTransaction(String transactionasString) throws Exception {
        return ChiffrementUtils.generateHashKey(transactionasString);
    }

    public TransitTransaction setTransitTransaction() throws Exception {
        Transaction tr = new Transaction();
        tr.setSenderAddress(InitTransactionDetails.personnalWallet);
        tr.setSenderAddress(InitTransactionDetails.remoteWallet);
        tr.setAmount(InitTransactionDetails.transacAmount);
        tr.setDateTime(String.valueOf(LocalDateTime.now()));

        TransitTransaction tt = new TransitTransaction();
        tt.setCryptedTransaction(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(tr), PrivateWalletHandler.walletPrivateKey));

        String thisHash = this.hashTransaction(tt.getCryptedTransaction());
        tt.setCryptedTransactionHash(thisHash);
        return tt;
    }

    public boolean checkTransactionValidity (TransitTransaction tt) throws Exception {
        return this.hashTransaction(tt.getCryptedTransaction()).equals( tt.getCryptedTransactionHash());
    }


}
