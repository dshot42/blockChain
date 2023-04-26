package com.test.springMongo.system.mongoDb.repository;

import com.test.springMongo.models.Block;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockChainRepository extends MongoRepository<Block, Long> {
}