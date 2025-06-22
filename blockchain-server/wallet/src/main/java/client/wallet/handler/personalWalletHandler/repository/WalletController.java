package client.wallet.handler.personalWalletHandler.repository;

import client.wallet.handler.personalWalletHandler.InitTransactionDetails;

import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @GetMapping("/getDefaultWallet")
    public Object getDefaultWallet() {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler();
       // privateWalletHandler.synchronizeTransaction();
    // code pour synchroniser les transactions du wallet depuis la block chain
        return ResponseEntity.ok(privateWalletHandler.getWallet());
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
