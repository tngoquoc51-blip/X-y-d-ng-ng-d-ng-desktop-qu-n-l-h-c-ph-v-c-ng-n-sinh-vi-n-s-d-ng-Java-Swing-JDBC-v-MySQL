package vn.edu.eaut.qlhocphi.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

/**
 * Tien ich doc / tao ma QR, dung ZXing (khong can goi API mang, chay hoan toan
 * offline). Dung cho 2 tinh nang:
 * 1) Quet QR the sinh vien tai quay Ke toan -> tra cuu cong no tuc thi.
 * 2) (Bonus) Tao QR gan len ho so / bien lai sinh vien de sau nay quet lai de dinh danh.
 */
public class QrCodeUtils {

    /**
     * Doc noi dung QR tu 1 anh (frame camera hoac anh tai len). Tra ve null neu
     * anh khong chua ma QR nao doc duoc (KHONG nem loi ra ngoai - goi lien tuc
     * moi frame camera nen phai "im lang" khi khong tim thay).
     */
    public static String docQR(BufferedImage anh) {
        if (anh == null) return null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(anh);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result ket = new MultiFormatReader().decode(bitmap);
            String text = ket.getText();
            return (text == null || text.isBlank()) ? null : text.trim();
        } catch (NotFoundException | RuntimeException ex) {
            // Khong tim thay QR trong anh nay - binh thuong, tra ve null de goi lai o frame sau.
            return null;
        }
    }

    /** Tao anh QR (nen trang, cham den) tu 1 chuoi noi dung bat ky, kich thuoc vuong pixel. */
    public static BufferedImage taoAnhQR(String noiDung, int kichThuocPx) throws WriterException {
        BitMatrix matrix = new QRCodeWriter().encode(noiDung, BarcodeFormat.QR_CODE, kichThuocPx, kichThuocPx);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
    public static String docAnhQR(java.io.File file) throws java.io.IOException {
        BufferedImage anh = javax.imageio.ImageIO.read(file);
        return docQR(anh);
    }

    public static void luuAnhQR(BufferedImage anhQR, java.io.File file) throws java.io.IOException {
        javax.imageio.ImageIO.write(anhQR, "png", file);
    }
}
