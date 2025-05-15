package blockChain.transaction.nodeThreads.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class GenericObjectConvert {

    public static String objectToString(Object obj) throws JsonProcessingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return  GenericObjectConvert.cleanStringInLine(ow.writeValueAsString(obj));
    }


    public static Object stringToObject(String datas, Class clazz)  {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(datas, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String cleanStringInLine(String data) {
    return data.replaceAll("\r", "").replaceAll("\n", "");
    }
}
