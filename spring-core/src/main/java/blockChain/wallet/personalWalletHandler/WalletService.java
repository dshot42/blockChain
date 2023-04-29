package blockChain.wallet.personalWalletHandler;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.PrivateWallet;
import blockChain.models.Transaction;
import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletService {

    public static boolean checkIntegrity(PrivateWallet wallet) {

        if (wallet.getTransactions() == null) return true;

        List<Transaction> transactionViolation =
                wallet.getTransactions().parallelStream().filter(t -> {
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
            if (t.getSenderAddress().getUniqueWalletId().equals(wallet.getUniqueWalletId()))
                amount -= t.getAmount();
            else
                amount += t.getAmount();
        }

        System.out.println("Montant total du wallet : " + amount);
        return amount;
    }


    public static boolean checkTransactionOnBlockChain(Transaction transaction) throws Exception {


        StringEntity entity = new StringEntity(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction)),
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
            System.out.println("Erreur lors de la communication avec le serveur, Check integrity of the transaction ");
        }
        return false;
    }

}

