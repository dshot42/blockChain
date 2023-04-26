package com.test.springMongo.system.mongoDb.service;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springMongo.models.Block;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.system.mongoDb.repository.ElementRepository;
import com.test.springMongo.transaction.initTransaction.initBlockChain.CreateBlockChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
public class BlockChainService {

    @Autowired
    ElementRepository elementRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    CreateBlockChain createInitBlockChain;


    public Transaction registryTransactionOnBlockChain(String transactionToString) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction transaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString), Transaction.class);

        if (!checkIntegrityOfActorOfTransaction(transaction)) {
            return null;
        }

        Block blockChain = Block.class.cast(elementRepository.getElementById(Block.class, elementRepository.count(Block.class)));
        List<Transaction> listTransac = new LinkedList<>();
        if (blockChain.getTransactions().size() == 100) { // 100 transaction = nouveau block
            blockChain = createInitBlockChain.createNewBlock();
            transaction.setImmutableChainedHash(transaction.getHash());
        } else if (blockChain.getTransactions().size() == 0) {
            transaction.setImmutableChainedHash(transaction.getHash());
        } else {
            listTransac = blockChain.getTransactions();
            transaction.setImmutableChainedHash(ChiffrementUtils.generateHashKey(listTransac.get(listTransac.size() - 1).getImmutableChainedHash()
                    + transaction.getHash())); // on chaine les hash
        }
        transaction.setId(sequenceGeneratorService.generateSequence(Transaction.class.getName() + "_seq"));
        listTransac.add(transaction);
        blockChain.setTransactions(listTransac);
        elementRepository.updateOrInsert(Block.class,blockChain);
        System.out.println("System - transaction immutable hash: " + transaction.getImmutableChainedHash() + ", register on block chain ! ");

        transaction.setBlockHash(blockChain.getImmutableChainedHash());
     //   transaction.setIdBlock(blockChain.getIdEntity());

        return transaction;
    }


    public boolean checkIntegrityOfActorOfTransaction(Transaction transactionToString) throws  Exception {

        return  true; //
        // comment faire ? je ne peux pas rechercher dans tous les blocks de la block chain a chaque fois ...
        // pourtant il faut un moyen !
    }


    public boolean checkIntegrityOfTransaction(String transactionToString) throws  Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction entryTransaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString), Transaction.class);

        Block block = Block.class.cast(elementRepository.getElementBy(Block.class,"immutableChainedHash",entryTransaction.getBlockHash()).get(0));

        Optional<Transaction> transactionOnBlockChain = block.getTransactions()
                .stream().filter(t-> t.getImmutableChainedHash().equals(entryTransaction.getImmutableChainedHash())).findAny();
        transactionOnBlockChain.get();

        if (transactionOnBlockChain.isPresent()) {
            System.out.println("Systeme : check integry of transaction succes {"+entryTransaction.getHash()+"}");
            return transactionOnBlockChain.get().getHash().equals(entryTransaction.getHash());
        }
        System.out.println("Systeme : violation of integrity de la block chain   {"+entryTransaction.getHash()+"}");
     return false; // dans tous les autres => false (existe pas ou hash incorrect)
    }

}
