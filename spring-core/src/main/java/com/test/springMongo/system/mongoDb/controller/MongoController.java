package com.test.springMongo.system.mongoDb.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.test.springMongo.system.mongoDb.repository.ElementRepository;
import com.test.springMongo.system.mongoDb.service.BlockChainService;
import com.test.springMongo.system.mongoDb.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/MongoDb")
public class MongoController {

    @Autowired
    ElementRepository elementService;
    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    BlockChainService blockChainService;

    /***
     *
     * @param element
     * @param field
     * @param value
     * @param filter
     * @return
     * @throws ClassNotFoundException
     * les filtres peuvent etre unique ou composés avec un separateur ';' exemple age lt 50 AND gt 20 => age/20;50/lt;gt
     */
    @GetMapping("/{element}/{field}/{value}/{filter}")
    public Object getAllElements(@PathVariable(value = "element") String element, @PathVariable(value = "field") String field,
                                 @PathVariable(value = "value") String value, @PathVariable(value = "filter") String filter) throws ClassNotFoundException {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        return ResponseEntity.ok(elementService.getElementBy(this.getClassForName(element), field, value, filter));
    }

    @GetMapping("/{element}/{field}/{value}")
    public Object getAllElements(@PathVariable(value = "element") String element, @PathVariable(value = "field") String field,
                                 @PathVariable(value = "value") String value) throws ClassNotFoundException {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        return ResponseEntity.ok(elementService.getElementBy(this.getClassForName(element), field, value));
    }

    @GetMapping("/{element}")
    public Object getAllElements(@PathVariable(value = "element") String element) throws ClassNotFoundException {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        return ResponseEntity.ok(elementService.getAllElements(this.getClassForName(element)));
    }

    @PostMapping("/{element}")
    public ResponseEntity<Object> createOrUpdateElement(@PathVariable(value = "element") String element, @Valid @RequestBody String datas) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(datas);
        ((ObjectNode) jsonNode).put("_id", sequenceGeneratorService.generateSequence(element + "_seq"));

        return ResponseEntity.ok(elementService.updateOrInsert(this.getClassForName(element), jsonNode.toString()));
    }

    ///////
    @PutMapping("/{element}/{id}")
    public ResponseEntity<Object> updateElement(@PathVariable(value = "element") String element,
                                                @PathVariable(value = "id") Long elementId, @Valid @RequestBody String datas) throws Throwable {
        return ResponseEntity.ok(elementService.updateElement(this.getClassForName(element), elementId, datas));
    }

    @DeleteMapping("/{element}/{id}")
    public ResponseEntity<Object> deleteElement(@PathVariable(value = "element") String element,
                                                @PathVariable(value = "id") Long elementId)
            throws Throwable {
        return ResponseEntity.ok(elementService.deleteElement(this.getClassForName(element), elementId));
    }

    private static Class<?> getClassForName(String element) throws ClassNotFoundException {
        return Class.forName(element);
    }

    ///////////////////// specificBlockChain /////////////////// a mettre ailleurs !

    @PostMapping("/BlockChain/transaction/checkIntegrity")
    public Object getTransaction(@Valid @RequestBody String datas) throws Exception {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        return ResponseEntity.ok(blockChainService.checkIntegrityOfTransaction(datas));
    }

    @PostMapping("/BlockChain/transaction")
    public ResponseEntity<Object> createOrUpdateBlockChain(@Valid @RequestBody String datas) throws Exception {
        return ResponseEntity.ok(blockChainService.registryTransactionOnBlockChain(datas));
    }

}