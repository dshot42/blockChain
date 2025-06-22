package blockChain.system.mongoDb.service;

import blockChain.chiffrement.ChiffrementUtils;

import blockChain.system.mongoDb.repository.ElementRepository;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vendor.models.Block;
import vendor.models.PublicWallet;
import vendor.models.Transaction;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class BlockChainService {

    public HashMap<String,byte[]> privateKeys = new HashMap<>();

     byte [] privateKey = ChiffrementUtils.systemKey;


    @Autowired
    ElementRepository elementRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    CreateBlockChain createInitBlockChain;



    public ArrayList<Transaction> getAllTransactions(String walletId) {
        ArrayList<Transaction> transactions = new ArrayList<>();

        List<Block> blockChain = elementRepository.getAllElements(Block.class).stream().map(
                Block.class::cast
        ).collect(Collectors.toList());

        blockChain.forEach(block -> {
            if (block.getTransactions() != null) {
                block.getTransactions().forEach(transaction -> {
                    if (transaction.getSenderAddress().getWalletId().equals(walletId) ||
                            transaction.getReceiverAddress().getWalletId().equals(walletId)) {
                        transactions.add(transaction);
                    }
                });
            }
        });
        return transactions;
    }


    public TransitTransaction registryTransactionOnBlockChain(String transactionToString) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Transaction transaction = objectMapper.readValue(ChiffrementUtils.decryptAES(transactionToString,privateKey), Transaction.class);

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

    private TransitTransaction persistBlockChain(Transaction transaction, List<PublicWallet> wallets) throws Exception {

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

        TransitTransaction transitTransaction = new TransitTransaction();
        transitTransaction.setState("ACK");
        transitTransaction.setCryptedTransaction(ChiffrementUtils.cryptAES(GenericObjectConvert.objectToString(transaction), privateKey));
       System.out.println("System - sender address : " + transaction.getSenderAddress().getAddress());
        transitTransaction.setSenderAddress(transaction.getSenderAddress());
        transitTransaction.setReceiverAddress(transaction.getReceiverAddress());
        return transitTransaction;
    }


    // wallet persistance en feedback
    private boolean persistPublicWalletTransaction(PublicWallet wallet, Transaction transaction) throws Exception {
        // il faut get le wallet si il existe pas ! et on pousse la
        //  il faut checker si collection exist !
        // creer si elle existe pas !
        elementRepository.getOrCreateCollection("PublicWallet");

        List<Object> blockChainPublicWalletObj = elementRepository.getElementBy(PublicWallet.class, "walletId", wallet.getWalletId());

        List<Transaction> transactionsList = new LinkedList<>();
        PublicWallet blockChainPublicWallet = null;
        if (blockChainPublicWalletObj.size() == 0) {
            blockChainPublicWallet = new PublicWallet();
            blockChainPublicWallet.setId(sequenceGeneratorService.generateSequence(PublicWallet.class.getName() + "_seq"));
            blockChainPublicWallet.setWalletId(wallet.getWalletId());
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
                return transactionOnBlockChain.get().getHash().equals(entryTransaction.getHash());
            }
            System.out.println("Systeme : block chain  violation of integrity  {" + entryTransaction.getHash() + "} ! ");
        } else return true;
        return false; // dans tous les autres => false (existe pas ou hash incorrect)
    }

}
