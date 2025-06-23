package client.wallet.handler.personalWalletHandler.repository;

import vendor.transaction.service.PrivateWalletHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @GetMapping("/getDefaultWallet")
    public Object getDefaultWallet() throws Exception {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();
        privateWalletHandler.synchronizeTransaction();
        return ResponseEntity.ok(privateWalletHandler.getWallet());
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
