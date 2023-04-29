package com.test.springMongo.system.mongoDb.repository;


import com.test.springMongo.system.mongoDb.criteriaFilter.CriteriaFilter;
import com.test.springMongo.system.mongoDb.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;


//@CrossOrigin(origins = "http://localhost:4200")
@Component
public class ElementRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;


    public List<Object> getAllElements(Class model) {
        return mongoTemplate.findAll(model);
    }


    public Object getElementById(Class model, Long elementId) {
        return mongoTemplate.findById(elementId, model);
    }

    public Long count(Class model) {
        return mongoTemplate.count(new Query(), model);
    }

    public List<Object> getElementBy(Class model, String field, String value, String filter) {
        Query query = queryFilter(field, value, filter);
        return mongoTemplate.find(query, model);
    }

    public List<Object> getElementBy(Class model, String field, Object value) {
        Query query = queryEquals(field, value);
        return mongoTemplate.find(query, model);
    }

    private static Query queryFilter(String field, String v, String filter) {
        Query query = new Query();
        switch (CriteriaFilter.valueOf(filter.toUpperCase())) {
            case IS:
                query.addCriteria(Criteria.where(field).is(v));
                break;
            case LIKE:
                query.addCriteria(Criteria.where(field).ne("%" + v + "%"));
                break;
            case LIKELAST:
                query.addCriteria(Criteria.where(field).ne("%" + v));
                break;
            case LIKEFIRST:
                query.addCriteria(Criteria.where(field).ne(v + "%"));
                break;
            case NOT:
                query.addCriteria(Criteria.where(field).ne(v));
                break;
            case LT:
                query.addCriteria(Criteria.where(field).lt(Float.parseFloat(v)));
                break;
            case LTE:
                query.addCriteria(Criteria.where(field).lte(Float.parseFloat(v)));
                break;
            case GT:
                query.addCriteria(Criteria.where(field).gt(Float.parseFloat(v)));
                break;
            case GTE:
                query.addCriteria(Criteria.where(field).gte(Float.parseFloat(v)));
                break;
        }
        return query;
    }

    private static Query queryEquals(String field, Object value) {
        Query query = new Query(Criteria.where(field).is(value));
        return query;
    }

    public Object updateOrInsert(Class model, Object datas) {
        return mongoTemplate.save(datas, model.getName().substring(model.getPackageName().length() + 1));
    }

    /////
    public Object updateElement(Class model, Long elementId, Object datas) throws Throwable {
        Object thisElement = this.getElementById(model, elementId);

        if (thisElement == null) return null; // gerer les exception !
        // jacksonMapper node des data
        return mongoTemplate.save(datas, model.getName().substring(model.getPackageName().length() + 1));
    }

    public Object deleteElement(Class model, Long elementId) {
        Object thisElement = this.getElementById(model, elementId);

        if (thisElement == null) return null; // gerer les exception !
        // jacksonMapper node des data
        Query query = queryEquals("_id", elementId);
        return mongoTemplate.remove(query, model.getName().substring(model.getPackageName().length() + 1));
    }

    public String getOrCreateCollection(String collectionName) {
        if (mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.getCollection(collectionName);
        } else {
            mongoTemplate.createCollection(collectionName);
        }
        return collectionName;
    }
}