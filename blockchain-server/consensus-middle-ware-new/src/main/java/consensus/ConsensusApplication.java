package consensus;

import consensus.service.ActorPoolHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class ConsensusApplication {
    ActorPoolHandler actorPoolHandler;

    @Autowired
    public ConsensusApplication(ActorPoolHandler actorPoolHandler) {
        this.actorPoolHandler = actorPoolHandler;
    }

    /***
     *
     * @param args
     * @throws Exception

     * ***************    SCENARIO    **************
     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur (tiers de confiace) renvoir un ackTransaction avec son address sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré avec la clef  au vendeur par le bief d'un noeud


     * ***************    SCENARIO 2   **************
     *    lacheteur envoi un message au vendeur pour une demande de transaction
     *    le vendeur (tiers de confiance) recoit un ackTransaction avec son address, sa clef et le montant !
     *    lacheteur renvoie toutes les informations chiffré au systeme et la transaction est validé par
     *    un consensus !
     */

    public static void main(String[] args) {
        SpringApplication.run(ConsensusApplication.class, args);
        System .out.println("Consensus System socket listener is instantiated and currently running");


    }


}
