package vn.edu.eaut.qlhocphi.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Bam mat khau bang SHA-256 (khop voi ham SHA2(...,256) dung trong schema.sql). */
public class PasswordUtils {

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Loi ma hoa mat khau", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equalsIgnoreCase(hashedPassword);
    }
}