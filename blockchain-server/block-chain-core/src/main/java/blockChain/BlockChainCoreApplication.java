package blockChain;

import blockChain.system.mongoDb.service.BlockChainService;
import blockChain.system.mongoDb.service.CreateBlockChain;
import blockChain.system.mongoDb.webSocket.MongoWebConsensusListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;


@Configuration
@SpringBootApplication
public class BlockChainCoreApplication {

    static MongoWebConsensusListener mongoWebConsensusListener;

    static BlockChainService blockChainService;

    static CreateBlockChain createInitBlockChain;

    @Autowired
    public BlockChainCoreApplication(MongoWebConsensusListener mongoWebConsensusListener,
                                     BlockChainService blockChainService, CreateBlockChain createInitBlockChain) {

        this.mongoWebConsensusListener = mongoWebConsensusListener;
        this.blockChainService = blockChainService;
        this.createInitBlockChain = createInitBlockChain;

    }

    /***
     *
     * @param args
     * @throws Exception

     * ***************    SCENARIO    **************
     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur (tiers de confiace) renvoir un ackTransaction avec son address sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré avec la clef  au vendeur par le bien d'un noeud


     * ***************    SCENARIO 2   **************
     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur (tiers de confiace) renvoir un ackTransaction avec son address sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré au systeme et la transaction est validé par
     *    un consensus !
     */

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BlockChainCoreApplication.class, args);

        Thread systemSocketListener = new Thread(mongoWebConsensusListener); // acheteur
        systemSocketListener.start();
        createInitBlockChain.initBlockChain();

        // test
       blockChainService.getAllTransactions("Personnal");

    }


}
