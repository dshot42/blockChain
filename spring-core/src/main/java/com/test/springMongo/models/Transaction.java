package com.test.springMongo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Transaction")
public class Transaction {
    @Id
    Long id;

    @Transient
    public static final String SEQUENCE_NAME = "transaction_sequence";

    Long idParent;
    PublicWallet senderAddress;
    PublicWallet receiverAddress;
    float amount;

    String hash;
    String immutableChainedHash;

    String dateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicWallet getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(PublicWallet senderAddress) {
        this.senderAddress = senderAddress;
    }

    public PublicWallet getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(PublicWallet receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getImmutableChainedHash() {
        return immutableChainedHash;
    }

    public void setImmutableChainedHash(String immutableChainedHash) {
        this.immutableChainedHash = immutableChainedHash;
    }

    public Long getIdParent() {
        return idParent;
    }

    public void setIdParent(Long idParent) {
        this.idParent = idParent;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }



}