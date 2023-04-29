package com.test.springMongo.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("Block")
public class Block {

    @Transient
    public static final String SEQUENCE_NAME = "block_sequence";


    @Id
    // @JsonSchema(title = "Workorder Internal ID", metadata = @JSData(key = "order", value = "10"))
    @JsonProperty
    public Long idEntity;
    Long idParent;
    List<Transaction> transactions;
    String immutableChainedHash;


    public Long getIdEntity() {
        return idEntity;
    }

    public void setIdEntity(Long idEntity) {
        this.idEntity = idEntity;
    }

    public Long getIdParent() {
        return idParent;
    }

    public void setIdParent(Long idParent) {
        this.idParent = idParent;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getImmutableChainedHash() {
        return immutableChainedHash;
    }

    public void setImmutableChainedHash(String immutableChainedHash) {
        this.immutableChainedHash = immutableChainedHash;
    }
}
