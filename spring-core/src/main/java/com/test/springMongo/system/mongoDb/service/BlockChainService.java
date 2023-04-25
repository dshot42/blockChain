package com.test.springMongo.system.mongoDb.service;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springMongo.models.Block;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.transaction.initTransaction.initBlockChain.CreateBlockChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
@Component
public class BlockChainService {

    @Autowired
    ElementRepository elementRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    CreateBlockChain createInitBlockChain;


    public Block registryTransactionOnBlockChain(String transactionToString) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction transaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString), Transaction.class);

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
        System.out.println("transaction : " + transaction.getImmutableChainedHash() + ", register on block chain ! ");
        return blockChain;
    }
}
