package blockChain.webService;

import blockChain.transaction.buyer.SendTransactionProcess;
import blockChain.transaction.seller.AckAndReceiveTransactionProcess;
import client.wallet.handler.personalWalletHandler.InitTransactionDetails;
import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @PostMapping("/send")
    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST})
    public ResponseEntity<String> handlePostRequest(@RequestBody @Valid String requestData) {

        System.out.println("Received data: " + requestData);
        try {

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(requestData);

            InitTransactionDetails.setTransationDetails(jsonNode.get("from").asText(), jsonNode.get("to").asText(), jsonNode.get("amount").asLong());

            // Launch the transaction process
            AckAndReceiveTransactionProcess.sendAskTransaction();
            //active le processus de transaction de la socket SendTransactionProcess => buyer !

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        } catch (RuntimeException e) {
           System.out.println("Consensus failed to perform the process "+ e);
        } catch (Exception e) {
            System.out.println("sendAskTransaction() " + e);
        }

        return ResponseEntity.ok("[SUCCES] commit  transaction  middleware !");
    }


}



