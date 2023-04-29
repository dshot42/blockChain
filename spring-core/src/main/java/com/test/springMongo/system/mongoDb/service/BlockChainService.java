package com.test.springMongo.system.mongoDb.service;

import com.chiffrement.ChiffrementUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springMongo.models.Block;
import com.test.springMongo.models.PublicWallet;
import com.test.springMongo.models.Transaction;
import com.test.springMongo.models.TransactionContainerToEmit;
import com.test.springMongo.system.mongoDb.repository.ElementRepository;
import com.test.springMongo.transaction.initTransaction.initBlockChain.CreateBlockChain;
import com.test.springMongo.transaction.nodeThreads.utils.GenericObjectConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class BlockChainService {

    @Autowired
    ElementRepository elementRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    CreateBlockChain createInitBlockChain;


    public TransactionContainerToEmit registryTransactionOnBlockChain(String transactionToString) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction transaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString), Transaction.class);
        transaction.setHash(ChiffrementUtils.generateHashKey(GenericObjectConvert.objectToString(transaction)));

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


        AtomicBoolean validity = new AtomicBoolean(true);

        List<PublicWallet> wallets = new LinkedList<>();
        wallets.add(transaction.getReceiverAddress());
        wallets.add(transaction.getSenderAddress());


        wallets.forEach(wallet -> {
            try {
                if (wallet.getTransactions() != null) {
                    wallet.getTransactions().forEach((t) -> {
                        try {
                            if (!checkIntegrityOfTransaction(t)) {
                                validity.set(false);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        if (!validity.get()) // si un des 2 wallets est pas valide on annule tout !
            return null; // fin de la transaction !

        return persistBlockChain(transaction, blockChain, listTransac, wallets);

        //  on persiste les wallets ! + la transaction en court
    }

    private TransactionContainerToEmit persistBlockChain(Transaction transaction, Block blockChain, List<Transaction> listTransac, List<PublicWallet> wallets) throws Exception {
        wallets.forEach(wallet -> {
            try {
                persistPublicWalletTransaction(wallet, transaction);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        transaction.setBlockHash(blockChain.getImmutableChainedHash());
        transaction.setId(sequenceGeneratorService.generateSequence(Transaction.class.getName() + "_seq"));
        listTransac.add(transaction);
        blockChain.setTransactions(listTransac);
        elementRepository.updateOrInsert(Block.class, blockChain);
        System.out.println("System - transaction immutable hash: " + transaction.getImmutableChainedHash() + ", register on block chain ! ");

        TransactionContainerToEmit cryptedTransaction = new TransactionContainerToEmit();
        cryptedTransaction.setState("ACK");
        cryptedTransaction.setCryptedTransaction(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction), ChiffrementUtils.systemKey));
        cryptedTransaction.setSenderAddress(transaction.getSenderAddress());
        cryptedTransaction.setReceiverAddress(transaction.getReceiverAddress());
        return cryptedTransaction;
    }

    private boolean persistPublicWalletTransaction(PublicWallet wallet, Transaction transaction) throws Exception {
        // il faut get le wallet si il existe pas ! et on pousse la
        //  il faut checker si collection exist !
        // creer si elle existe pas !

        elementRepository.getOrCreateCollection("PublicWallet");
        List<Object> blockChainPublicWObj = elementRepository.getElementBy(PublicWallet.class, "uniqueWalletId", wallet.getUniqueWalletId());

        List<Transaction> transactionsList = new LinkedList<>();
        if (blockChainPublicWObj.size() == 0) {
            transactionsList.add(transaction);
        } else {
            wallet = PublicWallet.class.cast(blockChainPublicWObj.get(0));
            List<Transaction> walletTransaction = new LinkedList<>();
            if (wallet.getTransactions() != null)
                transactionsList = wallet.getTransactions();
            else {
                walletTransaction.add(transaction);
                // n'est pas sensé se produit, sauf bug avec un probleme de persistence en bdd !
                // cas deja obtenue lors de mes tests
            }
            wallet.setTransactions(transactionsList);
        }
        wallet.setId(sequenceGeneratorService.generateSequence(PublicWallet.class.getName() + "_seq"));

        elementRepository.updateOrInsert(PublicWallet.class, wallet);
        return true;
    }


    public boolean checkIntegrityOfTransaction(String transactionToString) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction entryTransaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString), Transaction.class);
        return checkIntegrityOfTransaction(entryTransaction);
    }

    public boolean checkIntegrityOfTransaction(Transaction entryTransaction) throws Exception {

        Block block = Block.class.cast(elementRepository.getElementBy(Block.class, "immutableChainedHash", entryTransaction.getBlockHash()).get(0));

        Optional<Transaction> transactionOnBlockChain = block.getTransactions()
                .stream().filter(t -> t.getImmutableChainedHash().equals(entryTransaction.getImmutableChainedHash())).findAny();
        transactionOnBlockChain.get();

        if (transactionOnBlockChain.isPresent()) {
            //   System.out.println("Systeme : check integry of transaction succes {" + entryTransaction.getHash() + "}");
            return transactionOnBlockChain.get().getHash().equals(entryTransaction.getHash());
        }
        System.out.println("Systeme : block chain  violation of integrity  {" + entryTransaction.getHash() + "} ! ");
        return false; // dans tous les autres => false (existe pas ou hash incorrect)
    }

}
