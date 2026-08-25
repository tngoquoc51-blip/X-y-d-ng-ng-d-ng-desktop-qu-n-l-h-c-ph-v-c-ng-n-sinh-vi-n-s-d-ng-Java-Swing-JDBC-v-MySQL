package vn.edu.eaut.qlhocphi.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Tien ich xu ly va dinh dang ngay thang (dd/MM/yyyy). */
public class DateUtils {

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Chuyen LocalDate -> chuoi dd/MM/yyyy. Tra ve "" neu null. */
    public static String format(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT);
    }

    /** Chuyen LocalDateTime -> chuoi dd/MM/yyyy HH:mm. Tra ve "" neu null. */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMAT);
    }

    /** Chuyen chuoi dd/MM/yyyy -> LocalDate. Tra ve null neu chuoi rong hoac sai dinh dang. */
    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Kiem tra ngay da qua han so voi hom nay hay chua. */
    public static boolean isPastDue(LocalDate date) {
        return date != null && LocalDate.now().isAfter(date);
    }

    /** So ngay con lai tinh tu hom nay den han (co the am neu da qua han). */
    public static long soNgayConLai(LocalDate han) {
        if (han == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), han);
    }
}