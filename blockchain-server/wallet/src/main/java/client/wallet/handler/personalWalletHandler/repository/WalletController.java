package client.wallet.handler.personalWalletHandler.repository;

import client.wallet.handler.personalWalletHandler.InitWallet;
import client.wallet.handler.personalWalletHandler.PrivateWalletHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @GetMapping("/get")
    public Object getClientWallet() {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler("walletPersonnal", "Personnal");
        return ResponseEntity.ok(privateWalletHandler.getWallet());
    }


    @GetMapping("/clear")
    public Object clearWallet() {
        try {
            PrivateWalletHandler.clearWallet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  ResponseEntity.ok("clear done");
    }

// todo remove et refaire les call sur port 8091 !
    @GetMapping("/data/{name}")
    public Object getClientWallet(@PathVariable(value = "name") String name) {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(InitWallet.personnalWallet.getAddress(), InitWallet.personnalWallet.getWalletId());
        return ResponseEntity.ok(privateWalletHandler.getWallet());
    }


    @GetMapping("/refresh/{name}")
    public Object refreshWallet(@PathVariable(value = "name") String name) throws Exception {
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(name);
        return ResponseEntity.ok(privateWalletHandler.refreshWallet());
    }
}
