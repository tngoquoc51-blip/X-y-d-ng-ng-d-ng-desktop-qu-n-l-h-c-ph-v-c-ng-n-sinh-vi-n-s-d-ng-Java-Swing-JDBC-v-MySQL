package vn.edu.eaut.qlhocphi.ai;

import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dich vu Chatbot - tra loi tu do moi chu de qua AI that (Google Gemini), dong thoi
 * UU TIEN tra loi CHINH XAC 100% cho 2 loai cau hoi tra cuu du lieu that: (1) cong no
 * theo ma SV, (2) thong tin/anh dai dien sinh vien.
 *
 * SUA LOI QUAN TRONG: ban truoc so khop tu khoa KHONG DAU (VD "thong tin") voi cau hoi
 * nguoi dung go CO DAU that ("Thông tin sinh viên SV001") - 2 chuoi nay khong bao gio
 * bang nhau trong Java, khien moi cau hoi co dau deu bi roi xuong nhanh "chuyen cho AI"
 * thay vi tra CSDL. Ban nay BO DAU CA 2 VE truoc khi so sanh, sua dut diem loi tren.
 *
 * GIOI HAN CAN BIET: moi lan goi traLoiChiTiet(...) la 1 lan hoi DOC LAP, khong mang
 * theo lich su hoi thoai truoc do - nguoi dung can neu ro Ma SV/ten trong CHINH cau hoi.
 */
public class ChatbotService {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();

    private static final Pattern PATTERN_MA_SV_CHU = Pattern.compile("SV\\d{3,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_MA_SV_SO = Pattern.compile("\\b\\d{6,}\\b");

    private static final String SYSTEM_PROMPT =
            "Ban la tro ly ao than thien, ten la \"Tro ly QL Hoc phi\", tro chuyen tu nhien nhu nguoi that. "
                    + "Ban dang hoat dong ben trong 1 ung dung desktop quan ly hoc phi va cong no sinh vien cua "
                    + "truong dai hoc. Ban co the tra loi MOI chu de nguoi dung hoi (kien thuc chung, tro chuyen "
                    + "phiem, tu van, giai thich...), khong chi gioi han trong chu de hoc phi. Tra loi ngan gon, "
                    + "de hieu, tu nhien bang tieng Viet (tru khi nguoi dung hoi bang ngon ngu khac). Neu nguoi "
                    + "dung hoi thong tin ca nhan sinh vien (ten, lop, khoa, hinh anh...), hay tra loi rang ban "
                    + "khong the tu tra cuu duoc va de nghi ho hoi lai kem ro Ma sinh vien.";

    public KetQuaTraLoi traLoiChiTiet(String cauHoi) {
        String cauHoiKhongDau = boDauTiengViet(cauHoi);

        try {
            String maSV = timMaSVTrongCauHoi(cauHoi);

            // Uu tien 1: hoi cong no -> tra loi CHINH XAC tu CSDL.
            if (maSV != null && (cauHoiKhongDau.contains("cong no") || cauHoiKhongDau.contains("hoc phi")
                    || cauHoiKhongDau.contains("con no"))) {
                return new KetQuaTraLoi(traLoiCongNo(maSV), null);
            }

            // Uu tien 2: hoi thong tin ca nhan/anh dai dien -> tra loi CHINH XAC tu CSDL + anh that (neu co).
            if (maSV != null && (cauHoiKhongDau.contains("thong tin") || cauHoiKhongDau.contains("hinh anh")
                    || cauHoiKhongDau.contains(" anh ") || cauHoiKhongDau.contains("anh cua")
                    || cauHoiKhongDau.contains("lop nao") || cauHoiKhongDau.contains("khoa nao")
                    || cauHoiKhongDau.contains("hoc lop") || cauHoiKhongDau.contains("sinh vien"))) {
                return traLoiThongTinSinhVien(maSV);
            }

            // Moi cau hoi con lai: chuyen het cho AI.
            if (ChatbotClient.daCauHinh()) {
                return new KetQuaTraLoi(ChatbotClient.goi(SYSTEM_PROMPT, cauHoi), null);
            }

            if (cauHoiKhongDau.contains("xin chao") || cauHoiKhongDau.equals("hi") || cauHoiKhongDau.equals("hello")) {
                return new KetQuaTraLoi("Xin chao! Toi la tro ly ao cua he thong quan ly hoc phi. "
                        + "Ban co the hoi toi ve cong no, thong tin sinh vien, hoac cach thanh toan. "
                        + "Vi du: \"Cong no cua SV001 con bao nhieu?\" hoac \"Thong tin sinh vien 20231475\".", null);
            }
            if (cauHoiKhongDau.contains("thanh toan")) {
                return new KetQuaTraLoi("Ban co the thanh toan hoc phi bang 3 cach: (1) Nop tien mat truc tiep "
                        + "tai phong ke toan, (2) Chuyen khoan ngan hang theo thong tin tren hoa don, "
                        + "(3) Thanh toan online ngay trong ung dung qua muc \"Thanh toan online\".", null);
            }
            return new KetQuaTraLoi("Chuc nang tro chuyen tu do can cau hinh \"ai.api.key\" trong "
                    + "application.properties de hoat dong day du. Hien tai toi chi co the tra loi cau hoi "
                    + "ve cong no va thong tin sinh vien (VD: \"Cong no cua SV001\").", null);

        } catch (SQLException e) {
            return new KetQuaTraLoi("Xin loi, he thong dang gap su co khi truy van du lieu. Vui long thu lai sau.", null);
        } catch (Exception e) {
            return new KetQuaTraLoi("Xin loi, toi khong the tra loi cau hoi nay luc nay. Chi tiet loi: " + e.getMessage(), null);
        }
    }

    public String traLoi(String cauHoi) {
        return traLoiChiTiet(cauHoi).getVanBan();
    }

    public String traLoiVoiAnh(String cauHoi, String base64Anh, String loaiAnh) {
        if (!ChatbotClient.daCauHinh()) {
            return "Chuc nang doc anh can cau hinh \"ai.api.key\" trong application.properties de hoat dong.";
        }
        try {
            String cauHoiCuoi = (cauHoi == null || cauHoi.isBlank()) ? "Hay mo ta va phan tich anh nay." : cauHoi;
            return ChatbotClient.goiVoiAnh(SYSTEM_PROMPT, cauHoiCuoi, base64Anh, loaiAnh);
        } catch (Exception e) {
            return "Khong the phan tich anh luc nay. Chi tiet loi: " + e.getMessage();
        }
    }

    /** Bo dau tieng Viet + chuyen chu thuong, dung de so khop tu khoa khong phu thuoc nguoi dung go co dau hay khong. */
    private String boDauTiengViet(String s) {
        if (s == null) return "";
        String norm = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
        return norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    private String timMaSVTrongCauHoi(String cauHoi) {
        Matcher mChu = PATTERN_MA_SV_CHU.matcher(cauHoi.toUpperCase());
        if (mChu.find()) return mChu.group();
        Matcher mSo = PATTERN_MA_SV_SO.matcher(cauHoi);
        if (mSo.find()) return mSo.group();
        return null;
    }

    private String traLoiCongNo(String maSV) throws SQLException {
        List<HoaDonHocPhi> hoaDons = hoaDonDAO.layTheoSinhVien(maSV);
        if (hoaDons.isEmpty()) {
            return "Khong tim thay hoa don hoc phi nao cho sinh vien " + maSV + ".";
        }
        BigDecimal tongNo = BigDecimal.ZERO;
        for (HoaDonHocPhi hd : hoaDons) {
            tongNo = tongNo.add(hd.tinhConNo());
        }
        if (tongNo.compareTo(BigDecimal.ZERO) <= 0) {
            return "Sinh vien " + maSV + " hien khong con no hoc phi. Cam on ban da theo doi!";
        }
        return "Sinh vien " + maSV + " hien con no tong cong " + tongNo.toPlainString()
                + " d, tren " + hoaDons.size() + " hoa don. Vui long thanh toan som de tranh bi tinh la phi qua han.";
    }

    private KetQuaTraLoi traLoiThongTinSinhVien(String maSV) throws SQLException {
        SinhVien sv = sinhVienDAO.timTheoMa(maSV);
        if (sv == null) {
            return new KetQuaTraLoi("Khong tim thay sinh vien co ma \"" + maSV + "\" trong he thong. "
                    + "Vui long kiem tra lai dung ma sinh vien.", null);
        }

        StringBuilder vanBan = new StringBuilder();
        vanBan.append("Thong tin sinh vien ").append(sv.getHoTen())
                .append(" (Ma SV: ").append(sv.getMaSV()).append(")\n")
                .append("- Lop: ").append(sv.getLop() == null ? "(chua co)" : sv.getLop()).append("\n")
                .append("- Khoa: ").append(sv.getKhoa() == null ? "(chua co)" : sv.getKhoa()).append("\n")
                .append("- Trang thai: ").append(sv.isTrangThai() ? "Dang hoc" : "Da nghi hoc");

        String duongDanAnh = timDuongDanAnhDaiDien(sv.getMaSV());
        if (duongDanAnh == null) {
            vanBan.append("\n\n(Sinh vien nay chua co anh dai dien trong he thong.)");
        }

        return new KetQuaTraLoi(vanBan.toString(), duongDanAnh);
    }

    private String timDuongDanAnhDaiDien(String maSV) {
        String[] duoi = {"png", "jpg", "jpeg"};
        for (String d : duoi) {
            File f = new File("avatars/" + maSV + "." + d);
            if (f.exists() && f.isFile()) return f.getAbsolutePath();
        }
        return null;
    }
}