package blockChain.system.mongoDb.service;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.Block;
import blockChain.models.PublicWallet;
import blockChain.models.Transaction;
import blockChain.models.TransactionContainerToEmit;
import blockChain.system.mongoDb.repository.ElementRepository;
import blockChain.transaction.initTransaction.initBlockChain.CreateBlockChain;
import blockChain.transaction.nodeThreads.utils.GenericObjectConvert;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        return persistBlockChain(transaction, wallets);

        //  on persiste les wallets ! + la transaction en court
    }

    private TransactionContainerToEmit persistBlockChain(Transaction transaction, List<PublicWallet> wallets) throws Exception {


        Block blockChain = Block.class.cast(elementRepository.getElementById(Block.class, elementRepository.count(Block.class)));
        List<Transaction> listTransacOfTheBlock = new LinkedList<>();
        if (blockChain.getTransactions().size() % 100 == 0) { // 100 transaction = nouveau block
            blockChain = createInitBlockChain.createNewBlock();
            transaction.setImmutableChainedHash(ChiffrementUtils.generateHashKey(GenericObjectConvert.objectToString(transaction)));

        } else if (blockChain.getTransactions().size() == 0) {
            transaction.setImmutableChainedHash(transaction.getHash());
        } else {
            listTransacOfTheBlock = blockChain.getTransactions();
            transaction.setImmutableChainedHash(ChiffrementUtils.generateHashKey(listTransacOfTheBlock.get(listTransacOfTheBlock.size() - 1).getImmutableChainedHash()
                    + transaction.getHash())); // on chaine les hash
        }

        transaction.setId(sequenceGeneratorService.generateSequence(Transaction.class.getName() + "_seq"));
        transaction.setHash(ChiffrementUtils.generateHashKey(GenericObjectConvert.objectToString(transaction)));
        transaction.setBlockHash(blockChain.getImmutableChainedHash());


        listTransacOfTheBlock.add(transaction);
        blockChain.setTransactions(listTransacOfTheBlock);

        elementRepository.updateOrInsert(Block.class, blockChain);

        System.out.println("System - transaction immutable hash: " + transaction.getImmutableChainedHash() + ", register on block chain ! ");

        wallets.forEach(wallet -> {
            try {
                persistPublicWalletTransaction(wallet, transaction);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


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

        List<Object> blockChainPublicWalletObj = elementRepository.getElementBy(PublicWallet.class, "uniqueWalletId", wallet.getUniqueWalletId());

        List<Transaction> transactionsList = new LinkedList<>();
        PublicWallet blockChainPublicWallet = null;
        if (blockChainPublicWalletObj.size() == 0) {
            blockChainPublicWallet = new PublicWallet();
            blockChainPublicWallet.setId(sequenceGeneratorService.generateSequence(PublicWallet.class.getName() + "_seq"));
            blockChainPublicWallet.setUniqueWalletId(wallet.getUniqueWalletId());
            blockChainPublicWallet.setAddress(wallet.getAddress());
        } else {
            blockChainPublicWallet = PublicWallet.class.cast(blockChainPublicWalletObj.get(0));
            transactionsList = blockChainPublicWallet.getTransactions();
        }
        transactionsList.add(transaction);
        blockChainPublicWallet.setTransactions(transactionsList);

        elementRepository.updateOrInsert(PublicWallet.class, blockChainPublicWallet);
        return true;
    }


    public boolean checkIntegrityOfTransaction(String transactionToString) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction entryTransaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString), Transaction.class);
        return checkIntegrityOfTransaction(entryTransaction);
    }

    public boolean checkIntegrityOfTransaction(Transaction entryTransaction) throws Exception {

        Block block = Block.class.cast(elementRepository.getElementBy(Block.class, "immutableChainedHash", entryTransaction.getBlockHash()).get(0));

        if (block.getTransactions() != null) {
            Optional<Transaction> transactionOnBlockChain = block.getTransactions()
                    .stream().filter(t -> t.getImmutableChainedHash().equals(entryTransaction.getImmutableChainedHash())).findAny();
            transactionOnBlockChain.get();

            if (transactionOnBlockChain.isPresent()) {
                //   System.out.println("Systeme : check integry of transaction succes {" + entryTransaction.getHash() + "}");
                return transactionOnBlockChain.get().getHash().equals(entryTransaction.getHash());
            }
            System.out.println("Systeme : block chain  violation of integrity  {" + entryTransaction.getHash() + "} ! ");
        } else return true;


        return false; // dans tous les autres => false (existe pas ou hash incorrect)
    }

}
