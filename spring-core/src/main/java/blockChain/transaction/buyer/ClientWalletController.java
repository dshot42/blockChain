package blockChain.transaction.buyer;


import blockChain.wallet.personalWalletHandler.PrivateWalletHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ClientWallet")
public class ClientWalletController {

    @GetMapping("/data/{name}")
    public Object getClientWallet(@PathVariable(value = "name") String name) {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(name);
        return ResponseEntity.ok(privateWalletHandler.getWallet());
    }


    @GetMapping("/refresh/{name}")
    public Object refreshWallet(@PathVariable(value = "name") String name) throws Exception {
        //curl -X GET localhost:8090/api/vi/elements/WorkOrder
        PrivateWalletHandler privateWalletHandler = new PrivateWalletHandler(name);
        return ResponseEntity.ok(privateWalletHandler.refreshWallet());
    }

}