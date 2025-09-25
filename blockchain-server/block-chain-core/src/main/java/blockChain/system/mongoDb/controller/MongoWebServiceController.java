package blockChain.system.mongoDb.controller;


import blockChain.chiffrement.ChiffrementUtils;
import blockChain.system.mongoDb.repository.ElementRepository;
import blockChain.system.mongoDb.service.BlockChainService;
import blockChain.system.mongoDb.service.SequenceGeneratorService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vendor.models.PublicWallet;
import vendor.models.Transaction;
import vendor.models.TransitTransaction;
import vendor.utils.GenericObjectConvert;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/MongoDb")
public class MongoWebServiceController {

    @Autowired
    ElementRepository elementService;
    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    BlockChainService blockChainService;

    @PostMapping("/send/wallet/privatekey")
    public ResponseEntity<Object> setPrivateKey(@Valid @RequestBody String datas) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(datas);
        String walletId = jsonNode.get("walletId").asText();
        byte[] privateKey = (byte[]) GenericObjectConvert.stringToObject(jsonNode.get("privateKey").asText(),byte[].class);
        // set in static map //
        System.out.println(privateKey);
        blockChainService.privateKeys.put(walletId, privateKey);
        return ResponseEntity.ok("Serveur Received private key.");
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

    ///////////////////// specificBlockChain ///////////////////

    @PostMapping("/BlockChain/transaction/checkIntegrity")
    public ResponseEntity<Object> getTransaction(@Valid @RequestBody String datas) throws Exception {
        return ResponseEntity.ok(blockChainService.checkIntegrityOfTransaction(datas));
    }

    @PostMapping("/BlockChain/transaction")
    public ResponseEntity<Object> createOrUpdateBlockChain(@Valid @RequestBody String datas) throws Exception {
        try {
            blockChainService.registryTransactionOnBlockChain(TransitTransaction.class.cast(GenericObjectConvert.stringToObject(datas, TransitTransaction.class)));
            System.out.println("\"/BlockChain/transaction\" Transaction successfully registered on the blockchain !  ");
            return ResponseEntity.ok("update blockChain");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error during transaction processing: " + e.getMessage());
        }

    }


   @PostMapping("/BlockChain/transation/synchronization")
    public ResponseEntity<List> getAllTransaction(@Valid @RequestBody String data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(data);
            String walletId = jsonNode.get("walletId").asText();
            List<Transaction> transactions = blockChainService.getAllTransactions(walletId);
            if (transactions == null) {
                throw new Exception("No transactions found for the given wallet ID: " +walletId);
            }

            List<String> cryptedTransactions = transactions.stream().map(t -> {
                try {
                    return ChiffrementUtils.cryptAES(objectMapper.writeValueAsString(t), ChiffrementUtils.systemKey);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            System.out.println("\"/BlockChain/transation/synchronization\" Synchronization of transactions from the blockchain, " + cryptedTransactions.size() + " transactions found.  ");


            return ResponseEntity.ok(cryptedTransactions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonList("Error during transaction processing: " + e.getMessage()));
        }

    }

    ///////////////////// specificWallet /////////////////// a mettre ailleurs !

    @GetMapping("cryptedWallet/wallet/PublicWallet/{field}/{value}")
    public ResponseEntity<Object> getAllElements(@PathVariable(value = "field") String field,
                                                 @PathVariable(value = "value") String value) throws Exception {
        if (elementService.getElementBy(PublicWallet.class, field, value).size() != 0)
            return ResponseEntity.ok(elementService.getElementBy(PublicWallet.class, field, value).get(0));
        else
            return ResponseEntity.ok(null);
    }
}