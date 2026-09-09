package vn.edu.eaut.qlhocphi.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** Tao va doc ma QR chua thong tin ca nhan sinh vien (KHONG gom Khoa/Lop -
 *   2 truong nay luon do Admin tu chon khi nhap vao he thong, dam bao dung
 *  cau truc to chuc hien tai cua truong thay vi tin tuong du lieu ben ngoai). */
public class QRCodeSinhVienUtils{

    /** Ma hoa 1 SinhVien thanh chuoi noi dung QR dang "KEY:VALUE" moi dong. */
    public static String maHoaNoiDungSinhVien(SinhVien sv) {
        StringBuilder sb = new StringBuilder();
        sb.append("MASV:").append(rong(sv.getMaSV())).append("\n");
        sb.append("HOTEN:").append(rong(sv.getHoTen())).append("\n");
        sb.append("NGAYSINH:").append(sv.getNgaySinh() != null ? sv.getNgaySinh().toString() : "").append("\n");
        sb.append("QUEQUAN:").append(rong(sv.getQueQuan())).append("\n");
        sb.append("DIACHI:").append(rong(sv.getDiaChi())).append("\n");
        sb.append("EMAIL:").append(rong(sv.getEmail())).append("\n");
        sb.append("SDT:").append(rong(sv.getSoDienThoai()));
        return sb.toString();
    }

    /** Giai ma nguoc lai tu noi dung QR -> 1 SinhVien (Khoa/Lop de trong, cho Admin tu dien). */
    public static SinhVien phanTichNoiDungSinhVien(String noiDung) {
        Map<String, String> banDo = new HashMap<>();
        for (String dong : noiDung.split("\n")) {
            int viTri = dong.indexOf(':');
            if (viTri <= 0) continue;
            banDo.put(dong.substring(0, viTri).trim(), dong.substring(viTri + 1).trim());
        }
        SinhVien sv = new SinhVien();
        sv.setMaSV(banDo.getOrDefault("MASV", ""));
        sv.setHoTen(banDo.getOrDefault("HOTEN", ""));
        String ngaySinh = banDo.get("NGAYSINH");
        if (ngaySinh != null && !ngaySinh.isBlank()) {
            try { sv.setNgaySinh(LocalDate.parse(ngaySinh)); } catch (Exception ignored) {}
        }
        sv.setQueQuan(banDo.getOrDefault("QUEQUAN", ""));
        sv.setDiaChi(banDo.getOrDefault("DIACHI", ""));
        sv.setEmail(banDo.getOrDefault("EMAIL", ""));
        sv.setSoDienThoai(banDo.getOrDefault("SDT", ""));
        sv.setTrangThai(true);
        return sv;
    }

    public static BufferedImage taoAnhQR(String noiDung, int kichThuoc) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = writer.encode(noiDung, BarcodeFormat.QR_CODE, kichThuoc, kichThuoc, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    public static void luuAnhQR(BufferedImage anh, File file) throws IOException {
        ImageIO.write(anh, "PNG", file);
    }

    /** Doc noi dung ma QR tu 1 file anh da co san (.png/.jpg) - dung cho "Them tu ma QR". */
    public static String docAnhQR(File file) throws Exception {
        BufferedImage anh = ImageIO.read(file);
        if (anh == null) throw new IllegalArgumentException("File khong phai la anh hop le.");
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(anh)));
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

    private static String rong(String s) { return s == null ? "" : s; }
}