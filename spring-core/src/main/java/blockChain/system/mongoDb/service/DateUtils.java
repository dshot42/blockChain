package blockChain.system.mongoDb.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {


    public static Instant stringToDate(String date) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.FRANCE);
        return LocalDateTime.parse(date, dateTimeFormatter).toInstant(ZoneOffset.UTC);
    }
}
