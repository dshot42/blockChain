package com.test.springMongo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

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

    String blockHash;

    Long idBlock;

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

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public Long getIdBlock() {
        return idBlock;
    }

    public void setIdBlock(Long idBlock) {
        this.idBlock = idBlock;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Float.compare(that.amount, amount) == 0 && Objects.equals(id, that.id) && Objects.equals(idParent, that.idParent) && Objects.equals(senderAddress, that.senderAddress) && Objects.equals(receiverAddress, that.receiverAddress) && Objects.equals(hash, that.hash) && Objects.equals(immutableChainedHash, that.immutableChainedHash) && Objects.equals(dateTime, that.dateTime) && Objects.equals(blockHash, that.blockHash) && Objects.equals(idBlock, that.idBlock);
    }


}
