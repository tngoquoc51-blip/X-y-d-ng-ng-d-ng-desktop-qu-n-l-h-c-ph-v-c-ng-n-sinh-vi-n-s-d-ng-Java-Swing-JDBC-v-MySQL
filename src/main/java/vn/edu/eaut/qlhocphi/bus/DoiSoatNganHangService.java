package vn.edu.eaut.qlhocphi.bus;

import org.apache.poi.ss.usermodel.*;
import vn.edu.eaut.qlhocphi.dal.GiaoDichNganHangDAO;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.*;
import vn.edu.eaut.qlhocphi.util.VietnameseTextUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * "Doi soat thanh toan thong minh": nhap sao ke ngan hang (Excel/CSV) roi tu dong
 * khop tung giao dich voi hoa don con no bang thuat toan mo (fuzzy) - doc ma SV
 * trong noi dung CK, so khop ten khong dau bang Levenshtein, ket hop kiem tra so
 * tien - cham diem tin cay 0-100%.
 *
 * Kien truc tach rieng "nguon du lieu giao dich" (hien tai: file sao ke) khoi
 * "engine doi soat" (chayDoiSoatTuDong) - sau nay neu tich hop webhook ngan hang
 * that (SePay/Casso) chi can them 1 nguon nhap moi goi cung dao.them(), toan bo
 * engine phia duoi dung lai nguyen, khong doi.
 */
public class DoiSoatNganHangService {
    private final GiaoDichNganHangDAO dao = new GiaoDichNganHangDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final CongNoService congNoService = new CongNoService();
    private final SinhVienService sinhVienService = new SinhVienService();

    /** Nguong tin cay: >= day la TU_DONG_KHOP (cho phep duyet hang loat), thap hon la NGHI_VAN. */
    private static final int NGUONG_TU_DONG_KHOP = 85;
    private static final int NGUONG_NGHI_VAN = 55;

    private static final Pattern MA_SV_PATTERN = Pattern.compile("\\b\\d{6,10}\\b");

    public static class KetQuaNhap {
        public int tongDong, themMoi, trungLap, loi;
    }

    // ================== BUOC 1: NHAP SAO KE ==================

    public KetQuaNhap nhapSaoKe(File file) throws IOException, SQLException {
        List<GiaoDichNganHang> danhSach;
        String ten = file.getName().toLowerCase();
        if (ten.endsWith(".csv")) {
            danhSach = docFileCSV(file);
        } else if (ten.endsWith(".xlsx") || ten.endsWith(".xls")) {
            danhSach = docFileExcel(file);
        } else {
            throw new IOException("Chi ho tro file .csv, .xlsx hoac .xls");
        }

        KetQuaNhap kq = new KetQuaNhap();
        kq.tongDong = danhSach.size();
        for (GiaoDichNganHang gd : danhSach) {
            try {
                dao.them(gd);
                kq.themMoi++;
            } catch (SQLIntegrityConstraintViolationException trungMa) {
                kq.trungLap++; // Da nhap truoc do (trung MaThamChieu) - bo qua, khong loi
            } catch (SQLException ex) {
                kq.loi++;
            }
        }
        return kq;
    }

    /** Doc file CSV xuat tu app ngan hang. Tu dong nhan dien cot theo ten tieu de (khong phan
     *  biet hoa/thuong, co/khong dau) trong dong dau tien: Ngay GD, So tien, Noi dung, Ma tham chieu. */
    private List<GiaoDichNganHang> docFileCSV(File file) throws IOException {
        List<GiaoDichNganHang> list = new ArrayList<>();
        List<String> dong = Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
        if (dong.isEmpty()) return list;

        String[] tieuDe = taChCsv(dong.get(0));
        Map<String, Integer> chiSoCot = nhanDienCot(tieuDe);

        for (int i = 1; i < dong.size(); i++) {
            if (dong.get(i).isBlank()) continue;
            String[] o = taChCsv(dong.get(i));
            GiaoDichNganHang gd = dongThanhGiaoDich(o, chiSoCot, i);
            if (gd != null) list.add(gd);
        }
        return list;
    }

    private List<GiaoDichNganHang> docFileExcel(File file) throws IOException {
        List<GiaoDichNganHang> list = new ArrayList<>();
        try (InputStream is = new FileInputStream(file); Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) return list;

            Row dongTieuDe = sheet.getRow(sheet.getFirstRowNum());
            int soCot = dongTieuDe.getLastCellNum();
            String[] tieuDe = new String[soCot];
            for (int c = 0; c < soCot; c++) tieuDe[c] = layChuoiO(dongTieuDe.getCell(c));
            Map<String, Integer> chiSoCot = nhanDienCot(tieuDe);

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String[] o = new String[soCot];
                for (int c = 0; c < soCot; c++) o[c] = layChuoiO(row.getCell(c));
                GiaoDichNganHang gd = dongThanhGiaoDich(o, chiSoCot, r);
                if (gd != null) list.add(gd);
            }
        }
        return list;
    }

    private String layChuoiO(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toString();
            }
            return new java.math.BigDecimal(cell.getNumericCellValue()).toPlainString();
        }
        return cell.toString().trim();
    }

    /** Do tim cot theo tu khoa trong tieu de (da bo dau) - linh hoat voi nhieu ngan hang khac nhau. */
    private Map<String, Integer> nhanDienCot(String[] tieuDe) {
        Map<String, Integer> ketQua = new HashMap<>();
        for (int i = 0; i < tieuDe.length; i++) {
            String t = VietnameseTextUtils.boDauVietHoa(tieuDe[i]);
            if (t.contains("NGAY")) ketQua.putIfAbsent("NGAY", i);
            if (t.contains("SO TIEN") || t.contains("GIA TRI") || t.contains("CO") || t.contains("CREDIT")) ketQua.putIfAbsent("SOTIEN", i);
            if (t.contains("NOI DUNG") || t.contains("DIEN GIAI") || t.contains("DESCRIPTION")) ketQua.putIfAbsent("NOIDUNG", i);
            if (t.contains("MA") || t.contains("THAM CHIEU") || t.contains("REFERENCE") || t.contains("SO CT")) ketQua.putIfAbsent("MATC", i);
        }
        return ketQua;
    }

    private GiaoDichNganHang dongThanhGiaoDich(String[] o, Map<String, Integer> cot, int soDong) {
        try {
            String noiDung = layO(o, cot.get("NOIDUNG"));
            String soTienStr = layO(o, cot.get("SOTIEN"));
            BigDecimal soTien = parseSoTien(soTienStr);
            if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) return null; // bo qua dong Ghi No / rong

            String maTC = layO(o, cot.get("MATC"));
            if (maTC.isBlank()) {
                // Khong co cot ma tham chieu rieng trong file -> tu sinh 1 ma dua tren noi dung
                // dong do (van chong duoc trung lap NEU nhap lai dung file do 2 lan).
                maTC = "AUTO-" + Math.abs((noiDung + soTien + soDong).hashCode());
            }

            LocalDateTime thoiGian = parseNgay(layO(o, cot.get("NGAY")));

            GiaoDichNganHang gd = new GiaoDichNganHang();
            gd.setMaThamChieu(maTC);
            gd.setThoiGianGiaoDich(thoiGian != null ? thoiGian : LocalDateTime.now());
            gd.setSoTien(soTien);
            gd.setNoiDung(noiDung);
            gd.setTrangThaiDoiSoat("CHUA_XU_LY");
            return gd;
        } catch (Exception ex) {
            return null; // dong loi dinh dang -> bo qua, khong lam hong ca file
        }
    }

    private String layO(String[] o, Integer idx) {
        if (idx == null || idx >= o.length || o[idx] == null) return "";
        return o[idx].trim();
    }

    private BigDecimal parseSoTien(String s) {
        if (s == null || s.isBlank()) return null;
        String sach = s.replaceAll("[^0-9.\\-]", "");
        if (sach.isBlank() || sach.equals("-")) return null;
        try {
            return new BigDecimal(sach).abs();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDateTime parseNgay(String s) {
        if (s == null || s.isBlank()) return null;
        String[] mauThu = {"dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
        for (String mau : mauThu) {
            try {
                if (mau.contains("HH")) return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(mau));
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(mau)).atStartOfDay();
            } catch (Exception ignored) { }
        }
        return null;
    }

    /** Tach 1 dong CSV theo dau phay, co ho tro truong nam trong ngoac kep (co the chua dau phay). */
    private String[] taChCsv(String dong) {
        List<String> ketQua = new ArrayList<>();
        StringBuilder hienTai = new StringBuilder();
        boolean trongNgoacKep = false;
        for (char c : dong.toCharArray()) {
            if (c == '"') trongNgoacKep = !trongNgoacKep;
            else if (c == ',' && !trongNgoacKep) { ketQua.add(hienTai.toString()); hienTai.setLength(0); }
            else hienTai.append(c);
        }
        ketQua.add(hienTai.toString());
        return ketQua.toArray(new String[0]);
    }

    // ================== BUOC 2: ENGINE DOI SOAT THONG MINH ==================

    /** Chay doi soat cho tat ca giao dich CHUA_XU_LY, so khop voi danh sach hoa don con no. */
    public void chayDoiSoatTuDong() throws SQLException {
        List<GiaoDichNganHang> chuaXuLy = dao.layTheoTrangThai("CHUA_XU_LY");
        if (chuaXuLy.isEmpty()) return;

        List<HoaDonHocPhi> danhSachConNo = congNoService.layDanhSachConNo();

        for (GiaoDichNganHang gd : chuaXuLy) {
            KetQuaKhop ketQua = timHoaDonKhopNhat(gd, danhSachConNo);
            if (ketQua == null) {
                continue; // khong co ung vien nao ca -> giu CHUA_XU_LY de admin tu xu ly
            }
            String trangThaiMoi = ketQua.diem >= NGUONG_TU_DONG_KHOP ? "TU_DONG_KHOP"
                    : ketQua.diem >= NGUONG_NGHI_VAN ? "NGHI_VAN" : "CHUA_XU_LY";
            if (trangThaiMoi.equals("CHUA_XU_LY")) continue;
            dao.capNhatKetQuaDoiSoat(gd.getMaGiaoDich(), trangThaiMoi, ketQua.hoaDon.getMaHoaDon(), ketQua.diem);
        }
    }

    private static class KetQuaKhop {
        HoaDonHocPhi hoaDon;
        int diem;
    }

    /**
     * Cham diem tung hoa don con no ung voi 1 giao dich, chon hoa don diem cao nhat.
     * Diem = trung binh co trong so cua: (1) co tim thay Ma SV chinh xac trong noi dung
     * hay khong (uu tien tuyet doi), (2) do tuong dong ten SV (Levenshtein, khong dau),
     * (3) so tien giao dich co khop (>=) voi so con no hay khong.
     */
    private KetQuaKhop timHoaDonKhopNhat(GiaoDichNganHang gd, List<HoaDonHocPhi> danhSachConNo) {
        String noiDungChuan = VietnameseTextUtils.boDauVietHoa(gd.getNoiDung());
        Set<String> maSVTrongNoiDung = timTatCaMaTrongChuoi(gd.getNoiDung());

        KetQuaKhop tot = null;
        for (HoaDonHocPhi hd : danhSachConNo) {
            int diemMaSV = maSVTrongNoiDung.contains(hd.getMaSV()) ? 100 : 0;
            int diemTen = VietnameseTextUtils.doTuongDongChuaChuoi(gd.getNoiDung(), hd.getTenSV());
            boolean soTienKhop = gd.getSoTien().compareTo(hd.tinhConNo()) >= 0;

            int diemTong;
            if (diemMaSV == 100) {
                // Tim thay dung ma SV trong noi dung -> rat dang tin, chi can so tien hop ly
                diemTong = soTienKhop ? 98 : 70;
            } else {
                // Khong co ma SV -> chi dua vao do khop ten + so tien, uu tien thap hon
                diemTong = soTienKhop ? diemTen : (int) (diemTen * 0.6);
            }

            if (tot == null || diemTong > tot.diem) {
                tot = new KetQuaKhop();
                tot.hoaDon = hd;
                tot.diem = diemTong;
            }
        }
        return tot;
    }

    private Set<String> timTatCaMaTrongChuoi(String noiDung) {
        Set<String> ketQua = new HashSet<>();
        if (noiDung == null) return ketQua;
        Matcher m = MA_SV_PATTERN.matcher(noiDung);
        while (m.find()) ketQua.add(m.group());
        return ketQua;
    }

    // ================== BUOC 3: XAC NHAN (TAO PHIEU THU THAT) ==================

    /** Admin/Ke toan xac nhan 1 giao dich khop dung hoa don -> tao PhieuThu that,
     *  hoa don duoc cong tien ngay (dong bo voi CongNoPanel/HoaDonPanel dang dung chung). */
    public void xacNhanKhop(GiaoDichNganHang gd, HoaDonHocPhi hoaDon, TaiKhoan nguoiDuyet) throws SQLException {
        BigDecimal soTienGhiNhan = gd.getSoTien().min(hoaDon.tinhConNo().max(gd.getSoTien()));
        // Ghi nhan dung so tien giao dich thuc te (khong vuot qua so da chuyen), du co the > conNo (nop du/thua)
        soTienGhiNhan = gd.getSoTien();

        PhieuThu pt = new PhieuThu();
        pt.setMaHoaDon(hoaDon.getMaHoaDon());
        pt.setSoTienNop(soTienGhiNhan);
        pt.setNgayNop(gd.getThoiGianGiaoDich()); // dung dung ngay tren sao ke, khong phai gio duyet
        pt.setHinhThuc("CHUYEN_KHOAN");
        pt.setMaGiaoDich(gd.getMaThamChieu());
        pt.setNguoiThu(nguoiDuyet != null ? nguoiDuyet.getHoTen() + " (doi soat tu dong)" : "He thong doi soat");
        phieuThuDAO.them(pt);

        dao.danhDauDaXacNhan(gd.getMaGiaoDich(), nguoiDuyet != null ? nguoiDuyet.getTenDangNhap() : "he_thong");

        new NhatKyHeThongService().ghi(nguoiDuyet, "THANH_TOAN", "HoaDon #" + hoaDon.getMaHoaDon(),
                "Doi soat ngan hang tu dong: SV " + hoaDon.getMaSV() + " - " + MoneyFormat(soTienGhiNhan)
                        + " (do tin cay " + (gd.getDoTinCay() != null ? gd.getDoTinCay() : "-") + "%)");
    }

    /** Duyet hang loat TAT CA giao dich dang o muc "TU_DONG_KHOP" (do tin cay cao) trong 1 lan. */
    public int duyetHangLoatTuDongKhop(TaiKhoan nguoiDuyet) throws SQLException {
        List<GiaoDichNganHang> danhSach = dao.layTheoTrangThai("TU_DONG_KHOP");
        int thanhCong = 0;
        for (GiaoDichNganHang gd : danhSach) {
            if (gd.getMaHoaDonKhop() == null) continue;
            HoaDonHocPhi hd = congNoService.layDanhSachConNo().stream()
                    .filter(h -> h.getMaHoaDon() == gd.getMaHoaDonKhop()).findFirst().orElse(null);
            if (hd == null) continue; // hoa don co the da duoc thu bang cach khac tu truoc
            xacNhanKhop(gd, hd, nguoiDuyet);
            thanhCong++;
        }
        return thanhCong;
    }

    public void boQua(int maGiaoDich) throws SQLException {
        dao.boQua(maGiaoDich);
    }

    public List<GiaoDichNganHang> layTheoTrangThai(String trangThai) throws SQLException {
        return dao.layTheoTrangThai(trangThai);
    }

    private String MoneyFormat(BigDecimal b) {
        return new java.text.DecimalFormat("#,###").format(b) + " d";
    }
}