package com.test.springMongo.system.mongoDb.controller;


import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/todo")
public class BlockChainController {

    /*
    @Autowired
    protected BlockChainRepository blockChainRepository;
    @Autowired
    protected SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    BlockChainService blockChainService;

    @GetMapping("/find")
    public ResponseEntity<List<Transaction>> getAllElements() {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        return ResponseEntity.ok((List<Transaction>) blockChainRepository.findAll().stream().map((b) -> Block.class.cast(b).getTransactions()));
    }

    @GetMapping("/find/{hash}")
    public ResponseEntity<Transaction> getElementByHash(@PathVariable(value = "hash") String hash)
            throws Throwable {

        return ResponseEntity.ok((List<Transaction>)  blockChainRepository.findBy(hash,new Query(Criteria.add)).find().stream().map((b) -> Block.class.cast(b).getTransactions()));

    }

    */
    /*
    @PostMapping("/{element}")
    public Transaction createElement(@PathVariable(value = "element") String element, @Valid @RequestBody String datas) throws IOException, ClassNotFoundException {
        Transaction resp = null;
        switch (element) {
            case "User":
                User user = new ObjectMapper().readValue(datas, User.class);
                user.setId(sequenceGeneratorService.generateSequence(User.class.getName() + "_seq"));
                resp = userRepository.save(user);
                break;
            case "WorkOrderCN":
                Workorder workorder = new ObjectMapper().readValue(datas, WorkorderCN.class);
                workorder.setIdEntity(sequenceGeneratorService.generateSequence(Workorder.class.getName() + "_seq"));
                resp = workorderRepository.save(workorder);
                break;
        }
        return resp;
    }

    /////////
    @PutMapping("/{element}/{id}")
    public ResponseEntity<Block> updateElement(
            @PathVariable(value = "id") Long elementId, @Valid @RequestBody String datas) throws Throwable {
        User thisElement = userRepository.findById(elementId)
                .orElseThrow(() -> new Exception("Element not found for this id :: " + elementId));

        // jacksonMapper node des data
        thisElement = new ObjectMapper().readValue(datas, User.class);
        thisElement.setId(elementId);
        ElementEntity updatedElement = userRepository.save(thisElement);
        return ResponseEntity.ok(updatedElement);
    }

    @DeleteMapping("/{element}/{id}")
    public Map<String, Boolean> deleteElement(@PathVariable(value = "id") Long elementId)
            throws Throwable {
        User element = userRepository.findById(elementId)
                .orElseThrow(() -> new Exception("Element not found for this id :: " + elementId));

        userRepository.delete(element);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

     */
}