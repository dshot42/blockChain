package vendor.transaction.service;

import blockChain.chiffrement.ChiffrementUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;
import vendor.models.PrivateWallet;
import vendor.models.PublicWallet;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletService {

    public static boolean checkIntegrity(PrivateWallet wallet) {

        if (wallet.getTransactions() == null) return true;

        List<TransitTransaction> transactionViolation = wallet.getTransactions().stream().map(transaction -> {
            TransitTransaction askTransaction = null;
            try {
                askTransaction = new TransitTransaction();
                askTransaction.setReceiverAddress(InitTransactionDetails.personnalWallet);

                askTransaction.setCryptedTransaction(ChiffrementUtils.cryptAES(
                        GenericObjectConvert.objectToString(transaction)));

                PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitTransactionDetails.remoteWallet.getAddress(), InitTransactionDetails.remoteWallet.getWalletId());
                PrivateWallet myPrivateWallet = privateWalletHandler.getWallet();
                askTransaction.setSenderAddress(privateWalletHandler.mapPrivateToPublicWaller(myPrivateWallet));
                askTransaction.setDateTime(LocalDateTime.now().toString());
                askTransaction.setState("SYN");
                askTransaction.setAmount(InitTransactionDetails.transacAmount);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return askTransaction;
        }).collect(Collectors.toList()).parallelStream().filter(t -> {
            try {
                return !checkTransactionOnBlockChain(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return transactionViolation.size() == 0;
    }

    public static float getAmount(PrivateWallet wallet) {

        float amount = 0f;

        if (wallet.getTransactions() == null)
            return amount;

        for (Transaction t : wallet.getTransactions()) {
            if (t.getSenderAddress().getWalletId().equals(wallet.getWalletId()))
                amount -= t.getAmount();
            else
                amount += t.getAmount();
        }

        System.out.println("Montant total du wallet : " + amount);
        return amount;
    }


    public static boolean checkTransactionOnBlockChain(TransitTransaction tt) throws Exception {

        StringEntity entity = new StringEntity(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(tt)),
                ContentType.APPLICATION_FORM_URLENCODED);

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8090/MongoDb/BlockChain/transaction/checkIntegrity");
        request.addHeader("content-type", "application/json");
        request.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity respEntity = response.getEntity();
            String data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                    StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            return Boolean.valueOf(data);

        } catch (Exception e) {
            System.out.println("Erreur lors de la communication avec le serveur, Check integrity of the tt ");
        }
        return false;
    }


    public static PublicWallet getAllTransation(PrivateWallet privateWallet) {
        PublicWallet publicWallet = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpGet request = new HttpGet("http://localhost:8090/MongoDb/wallet/PublicWallet/walletId/" + privateWallet.getWalletId());
        request.addHeader("content-type", "application/json");
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity respEntity = response.getEntity();
            String data = new BufferedReader(new InputStreamReader(respEntity.getContent(),
                    StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            try {
                publicWallet = PublicWallet.class.cast(GenericObjectConvert.stringToObject(data, PublicWallet.class));
            } catch (Exception e) {
                return null;
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la communication avec le serveur, Refresh Wallet datas; ");
        }
        return publicWallet;
    }


    public static PrivateWallet bindPublicToPrivateTransaction(PublicWallet returnedPublicWallet, PrivateWallet myprivateWallet) {

        if (returnedPublicWallet != null) {
            myprivateWallet.setTransactions(returnedPublicWallet.getTransactions());
        }
        return myprivateWallet;
    }

}

