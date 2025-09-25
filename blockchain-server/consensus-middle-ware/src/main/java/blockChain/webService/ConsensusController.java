package blockChain.webService;

import blockChain.transaction.buyer.SendTransactionProcess;
import blockChain.transaction.seller.AckAndReceiveTransactionProcess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vendor.transaction.service.InitTransactionDetails;


import javax.validation.Valid;

@CrossOrigin(origins = "*") // important
@RestController
@RequestMapping("/transaction")
public class ConsensusController {


    // TransactionHandlers


    @GetMapping("/ok")
    public Object ok() {
        System.out.println("consensus ok ");
        return ResponseEntity.ok("consensus ok ");
    }

}



