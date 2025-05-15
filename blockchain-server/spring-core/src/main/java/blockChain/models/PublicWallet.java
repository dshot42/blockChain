package blockChain.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("PublicWallet")
public class PublicWallet  {
    public PublicWallet() {
    }

    public PublicWallet(String address, String uniqueWalletId, List<Transaction> transactions) {
        this.address = address;
        this.uniqueWalletId = uniqueWalletId;
        this.transactions = transactions;
    }

    public PublicWallet(String address, String uniqueWalletId) {
        this.address = address;
        this.uniqueWalletId = uniqueWalletId;
    }


    @Transient
    public static final String SEQUENCE_NAME = "PublicWallet_sequence";

    @Id
    public Long id;
    public String address;

    public String uniqueWalletId;

    public List<Transaction> transactions;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUniqueWalletId() {
        return uniqueWalletId;
    }

    public void setUniqueWalletId(String uniqueWalletId) {
        this.uniqueWalletId = uniqueWalletId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
