package blockChain.chiffrement;

import java.util.Scanner;

public class EncryptData {


    public static void main(String[] args) throws Exception {
        if (args[0].equals("generateOwnKey")) {
            System.out.println("AES =" + ChiffrementUtils.cryptAES(ChiffrementUtils.getOwnDatas()));
            System.out.println("SHA =" + ChiffrementUtils.generateHashKey(ChiffrementUtils.getOwnDatas()));
        } else if (args[0].equals("generateClientKey")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Saisir Adresse physique . . . . . . . . . :");
           // String address = scanner.next();
            String address = "00-15-5D-6F-60-CC";
            System.out.println(address);
            System.out.print("Saisir Nom de l’hote . . . . . . . . . . :");
          //  String hostName = scanner.nextLine();
            String hostName = "ALT-5591-YTOU";
            System.out.println(hostName);
            System.out.println("        **** Key GENERATE ****      ");
            String datas = ChiffrementUtils.getClientDatas(address, hostName);
            String shaGen = ChiffrementUtils.generateHashKey(datas);
            System.out.println(shaGen);
            System.out.println("is it me ? " + ChiffrementUtils.hashCompare(shaGen));
        } else {
            System.out.println("error Args");
        }
    }


}
