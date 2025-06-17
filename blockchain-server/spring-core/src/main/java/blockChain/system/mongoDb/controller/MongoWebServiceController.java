package blockChain.system.mongoDb.controller;


import blockChain.system.mongoDb.repository.ElementRepository;
import blockChain.system.mongoDb.service.BlockChainService;
import blockChain.system.mongoDb.service.SequenceGeneratorService;
import blockChain.system.mongoDb.webSocket.MongoWebConsensusListener;
import blockChain.transaction.buyer.SendTransactionProcess;
import blockChain.transaction.initTransaction.initBlockChain.CreateBlockChain;
import blockChain.transaction.seller.AckAndReceiveTransactionProcess;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vendor.models.PublicWallet;

import javax.validation.Valid;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/MongoDb")
public class MongoWebServiceController {

    @Autowired
    ElementRepository elementService;
    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    BlockChainService blockChainService;


    // TransactionHandlers
    @Autowired
    static SendTransactionProcess sendTransactionController;
    @Autowired
    static AckAndReceiveTransactionProcess askTransactionController;



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

    @PostMapping("/Send/Transaction")
    private static void launchTransaction() throws InterruptedException {
        Thread tSend = new Thread(sendTransactionController); // acheteur
        tSend.start();
        Thread.sleep(1000);

        Thread tAsk = new Thread(askTransactionController); // vendeur
        tAsk.start();
    }


    @GetMapping("/{element}/{field}/{value}/{filter}")
    public Object getAllElements(@PathVariable(value = "element") String element, @PathVariable(value = "field") String field,
                                 @PathVariable(value = "value") String value, @PathVariable(value = "filter") String filter) throws ClassNotFoundException {
        return ResponseEntity.ok(elementService.getElementBy(this.getClassForName(element), field, value, filter));
    }

    @GetMapping("/{element}/{field}/{value}")
    public Object getAllElements(@PathVariable(value = "element") String element, @PathVariable(value = "field") String field,
                                 @PathVariable(value = "value") String value) throws ClassNotFoundException {
        return ResponseEntity.ok(elementService.getElementBy(this.getClassForName(element), field, value));
    }

    @GetMapping("/{element}")
    public Object getAllElements(@PathVariable(value = "element") String element) throws ClassNotFoundException {
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
    public ResponseEntity<Object> getTransaction(@Valid @RequestBody String datas) throws Exception {
        return ResponseEntity.ok(blockChainService.checkIntegrityOfTransaction(datas));
    }

    @PostMapping("/BlockChain/transaction")
    public ResponseEntity<Object> createOrUpdateBlockChain(@Valid @RequestBody String datas) throws Exception {
        return ResponseEntity.ok(blockChainService.registryTransactionOnBlockChain(datas));
    }

    ///////////////////// specificWallet /////////////////// a mettre ailleurs !

    @GetMapping("cryptedWallet/wallet/PublicWallet/{field}/{value}")
    public ResponseEntity<Object> getAllElements( @PathVariable(value = "field") String field,
                                 @PathVariable(value = "value") String value) throws Exception {
        if (elementService.getElementBy(PublicWallet.class, field, value).size() != 0)
            return ResponseEntity.ok(elementService.getElementBy(PublicWallet.class, field, value).get(0));
        else
            return ResponseEntity.ok(null);
    }
}