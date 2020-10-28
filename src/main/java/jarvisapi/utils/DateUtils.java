package jarvisapi.utils;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DateUtils {

    public static Date getExpirationDate(long durationInSecond) {
        return new Date(new Date().getTime() + (durationInSecond * 1000));
    }

    public static boolean isDateExpired(Date expirationDate) {
        return new Date().before(expirationDate);
    }
}
