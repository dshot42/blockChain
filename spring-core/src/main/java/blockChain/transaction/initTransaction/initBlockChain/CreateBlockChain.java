package blockChain.transaction.initTransaction.initBlockChain;

import blockChain.chiffrement.ChiffrementUtils;
import blockChain.models.Block;
import blockChain.system.mongoDb.repository.ElementRepository;
import blockChain.system.mongoDb.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class CreateBlockChain {

    @Autowired
    ElementRepository elementService;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    public void initBlockChain() {
        if (elementService.count(Block.class) == 0) {
            Block block = createNewBlock();
            elementService.updateOrInsert(Block.class, block);
        }
    }

    public Block createNewBlock() {
        Block block = new Block();
        block.setIdEntity(sequenceGeneratorService.generateSequence(Block.class.getName() + "_seq"));
        block.setIdParent(null);
        block.setTransactions(new LinkedList<>());
        try {
            if (block.getIdEntity() == 1) // init block
                block.setImmutableChainedHash(ChiffrementUtils.generateHashKey(block.toString()));
            else {
                Block lastBlock = Block.class.cast(elementService.getElementById(Block.class, elementService.count(Block.class)));
                block.setImmutableChainedHash(ChiffrementUtils.generateHashKey(lastBlock.getImmutableChainedHash() + block.toString()));
            }
        } catch (Exception e) {
            System.out.println("Hash fail, " + e);
        }
        return block;
    }
}
