package client.wallet.handler.personalWalletHandler.repository;

import client.wallet.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import vendor.models.PrivateWallet;
import vendor.models.TransitTransaction;
import vendor.transaction.service.InitTransactionDetails;
import vendor.transaction.service.PrivateWalletHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vendor.utils.GenericObjectConvert;

import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    TransactionService transactionService;

    @GetMapping("/getDefaultWallet")
    public Object getDefaultWallet() throws Exception {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();
        privateWalletHandler.synchronizeTransaction();
        return ResponseEntity.ok(privateWalletHandler.getWallet());
    }

    @PostMapping("/send")
    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST})
    public ResponseEntity<String> handlePostRequest(@RequestBody @Valid String requestData) {

        try {
            System.out.println("Send transaction to consensus : " + requestData);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(requestData);

            InitTransactionDetails.setTransationDetails(jsonNode.get("from").asText(), jsonNode.get("to").asText(), jsonNode.get("amount").asLong());
            // Launch the transaction process

            transactionService.sendAskTransaction();
            // this method will send the transaction to the consensus middleware
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

    @GetMapping("/transaction/validation") // receive feedback from server after push transaction on block chain
    public Object validationTransaction() throws Exception {

        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();
        privateWalletHandler.synchronizeTransaction();
        privateWalletHandler.emitTransactionToSocketClient();
        System.out.println("get transactionValidation from server, emit to Node socket client 3000");
        return ResponseEntity.ok("update wallet done ");
    }

    @GetMapping("/clear")
    public Object clearWallet() {
        try {
            PrivateWalletHandler.clearWallet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("clear done");
    }

    @GetMapping("/get/{name}")
    public Object getWallet(@PathVariable(value = "name") String name) {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();
        // pas de changement de port juste de nom pour les tests de differents wallet
        return ResponseEntity.ok(privateWalletHandler.getWallet());
    }


    @GetMapping("/refresh/{name}")
    public Object refreshWallet(@PathVariable(value = "name") String name) throws Exception {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();
        return ResponseEntity.ok(privateWalletHandler.refreshWallet());
    }
}
