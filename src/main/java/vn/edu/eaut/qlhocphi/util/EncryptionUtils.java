package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Tiện ích mã hóa / giải mã dữ liệu nhạy cảm (AES-256-GCM).
 * <p>
 * - Dùng cho: Email, Số điện thoại, Địa chỉ, Quê quán... của sinh viên.
 * - Key lấy từ application.properties (encryption.secret.key). Nếu không có thì dùng key mặc định.
 * - Hỗ trợ dữ liệu cũ (chưa mã hóa): nếu giải mã thất bại thì trả về nguyên văn → không mất dữ liệu khi nâng cấp.
 * - Format lưu DB: Base64( IV[12] + Ciphertext + Tag )
 */
public final class EncryptionUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;   // 96 bit
    private static final int GCM_TAG_LENGTH = 128; // bit
    private static final String PREFIX = "ENC:";   // đánh dấu dữ liệu đã mã hóa

    private static volatile SecretKey secretKey;

    private EncryptionUtils() {}

    private static SecretKey getKey() {
        if (secretKey == null) {
            synchronized (EncryptionUtils.class) {
                if (secretKey == null) {
                    String keyStr = AppConfig.get("encryption.secret.key", "QLHocPhi@2026#SecretKey!AES256");
                    try {
                        // Derive 256-bit key từ chuỗi cấu hình bằng SHA-256
                        MessageDigest sha = MessageDigest.getInstance("SHA-256");
                        byte[] keyBytes = sha.digest(keyStr.getBytes(StandardCharsets.UTF_8));
                        secretKey = new SecretKeySpec(keyBytes, "AES");
                    } catch (Exception e) {
                        throw new RuntimeException("Không thể khởi tạo khóa mã hóa", e);
                    }
                }
            }
        }
        return secretKey;
    }

    /**
     * Mã hóa chuỗi. Trả về null nếu input null/blank.
     * Kết quả luôn có prefix "ENC:" để dễ nhận biết.
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        // Đã mã hóa rồi thì không mã hóa lại
        if (plainText.startsWith(PREFIX)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + CipherText
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Giải mã. Nếu không phải dữ liệu mã hóa hoặc giải mã thất bại → trả về nguyên văn (an toàn với dữ liệu cũ).
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }
        // Không có prefix → dữ liệu cũ (plaintext)
        if (!cipherText.startsWith(PREFIX)) {
            return cipherText;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Giải mã thất bại → giữ nguyên (tránh mất dữ liệu)
            System.err.println("[EncryptionUtils] Giải mã thất bại, trả về nguyên văn: " + e.getMessage());
            return cipherText;
        }
    }

    /** Kiểm tra chuỗi đã được mã hóa chưa */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}