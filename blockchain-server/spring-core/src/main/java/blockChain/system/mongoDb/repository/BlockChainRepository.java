package blockChain.system.mongoDb.repository;

import blockChain.models.Block;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockChainRepository extends MongoRepository<Block, Long> {
}