package vn.edu.eaut.qlhocphi.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/** Dinh dang tien te VND cho hien thi len giao dien. */
public class MoneyUtils {
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###");

    public static String format(BigDecimal amount) {
        if (amount == null) return "0 d";
        return FORMAT.format(amount) + " d";
    }

    public static String format(long amount) {
        return FORMAT.format(amount) + " d";
    }
}