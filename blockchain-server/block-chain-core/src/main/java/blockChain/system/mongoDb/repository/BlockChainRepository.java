package blockChain.system.mongoDb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vendor.models.Block;

@Repository
public interface BlockChainRepository extends MongoRepository<Block, Long> {
}