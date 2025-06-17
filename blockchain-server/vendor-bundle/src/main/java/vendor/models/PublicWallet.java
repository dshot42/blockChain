package vendor.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("PublicWallet")
public class PublicWallet  {
    @Id
    public Long id;
    public String address;

    public String walletId;

    public List<Transaction> transactions;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
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

    public PublicWallet() {};
    public PublicWallet(String address, String walletId, List<Transaction> transactions) {
        this.address = address;
        this.walletId = walletId;
        this.transactions = transactions;
    }

    public PublicWallet(String address, String walletId) {
        this.address = address;
        this.walletId = walletId;
    }


    @Transient
    public static final String SEQUENCE_NAME = "PublicWallet_sequence";


}
