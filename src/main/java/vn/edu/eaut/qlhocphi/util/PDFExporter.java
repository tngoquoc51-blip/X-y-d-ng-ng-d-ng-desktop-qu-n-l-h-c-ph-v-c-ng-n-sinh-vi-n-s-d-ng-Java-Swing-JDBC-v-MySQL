package vn.edu.eaut.qlhocphi.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Xuat bien lai / bao cao ra file PDF bang thu vien iText.
 * Yeu cau dependency com.itextpdf:itextpdf da khai bao trong pom.xml.
 *
 * Ghi chu: font Helvetica mac dinh cua iText khong ho tro dau tieng Viet
 * (se ra o vuong neu go co dau), nen toan bo text trong file nay co chu y
 * khong dung dau, giong quy uoc chung cua ca du an. Neu muon co dau that,
 * can nhung 1 file .ttf ho tro Unicode (vd Arial, Times New Roman) bang
 * BaseFont.createFont(..., BaseFont.IDENTITY_H, true) thay vi dung Font
 * mac dinh nhu duoi day.
 */
public class PDFExporter {

    /** Xuat mot bang du lieu don gian ra file PDF (dung cho bao cao). */
    public static void exportBangDuLieu(String filePath, String tieuDe,
                                        String[] headers, List<String[]> rows) throws IOException {
        Document document = new Document(PageSize.A4);
        boolean daMo = false;
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            daMo = true;

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph(tieuDe, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(47, 111, 237));
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (String[] rowData : rows) {
                for (String value : rowData) {
                    PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            document.add(table);
        } catch (DocumentException e) {
            throw new IOException("Loi tao file PDF: " + e.getMessage(), e);
        } finally {
            if (daMo && document.isOpen()) {
                document.close();
            }
        }
    }

    /** Ban cu, giu lai de tuong thich nguoc - nen dung xuatBienLaiChuyenNghiep() thay the. */
    public static void xuatBienLai(String filePath, String hoTenSV, String maSV,
                                   String tenHocKy, String soTienNop, String ngayNop) throws IOException {
        Document document = new Document(PageSize.A5);
        boolean daMo = false;
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            daMo = true;

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

            Paragraph title = new Paragraph("BIEN LAI THU HOC PHI", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Ho ten: " + hoTenSV, normalFont));
            document.add(new Paragraph("Ma SV: " + maSV, normalFont));
            document.add(new Paragraph("Hoc ky: " + tenHocKy, normalFont));
            document.add(new Paragraph("So tien nop: " + soTienNop, normalFont));
            document.add(new Paragraph("Ngay nop: " + ngayNop, normalFont));
        } catch (DocumentException e) {
            throw new IOException("Loi tao bien lai PDF: " + e.getMessage(), e);
        } finally {
            if (daMo && document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Xuat bien lai thu hoc phi ban CHUYEN NGHIEP - co tieu de truong, so phieu,
     * khung so tien noi bat kem so tien doc bang chu, bang tong ket cong no
     * (Tong hoc phi / Da nop / Con lai) va 2 cot chu ky Nguoi nop - Nguoi thu.
     *
     * @param tenTruong     Ten truong hien o dau bien lai (VD "TRUONG DAI HOC EAUT")
     * @param maPhieuThu    Ma phieu thu (dung de sinh so bien lai BLxxxxxx)
     * @param hoTenSV       Ho ten sinh vien
     * @param maSV          Ma so sinh vien
     * @param tenHocKy      Ten hoc ky cua hoa don duoc thanh toan
     * @param tongHocPhi    Tong tien hoc phi cua hoa don (SoTien)
     * @param soTienNop     So tien nop trong lan giao dich nay
     * @param conNoConLai   So tien con no CUA HOA DON sau lan nop nay (0 neu da dong du)
     * @param hinhThuc      TIEN_MAT / CHUYEN_KHOAN / THANH_TOAN_ONLINE
     * @param maGiaoDich    Ma giao dich (co the null/rong neu nop tien mat)
     * @param nguoiThu      Ten nguoi thu (co the null/rong neu thanh toan online)
     * @param ngayNop       Ngay nop da dinh dang san (dd/MM/yyyy HH:mm)
     */
    public static void xuatBienLaiChuyenNghiep(String filePath, String tenTruong, int maPhieuThu,
                                               String hoTenSV, String maSV, String tenHocKy,
                                               BigDecimal tongHocPhi, BigDecimal soTienNop, BigDecimal conNoConLai,
                                               String hinhThuc, String maGiaoDich, String nguoiThu,
                                               String ngayNop) throws IOException {
        Document document = new Document(PageSize.A5, 32, 32, 24, 24);
        boolean daMo = false;
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            daMo = true;

            BaseColor mauChinh = new BaseColor(0x2F, 0x6F, 0xED);
            BaseColor mauChu = new BaseColor(0x1F, 0x29, 0x37);
            BaseColor mauMo = new BaseColor(0x6B, 0x74, 0x80);
            BaseColor mauVien = new BaseColor(0xE2, 0xE6, 0xEC);
            BaseColor mauXanhLa = new BaseColor(0x22, 0xA0, 0x6B);
            BaseColor mauDo = new BaseColor(0xE1, 0x4B, 0x4B);
            BaseColor mauNenNhat = new BaseColor(0xEE, 0xF3, 0xFD);

            Font fontTruong = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, mauChinh);
            Font fontPhongBan = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, mauMo);
            Font fontTieuDe = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, mauChu);
            Font fontSoPhieu = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, mauMo);
            Font fontNhan = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, mauMo);
            Font fontGiaTri = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, mauChu);
            Font fontSoTienLon = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, mauChinh);
            Font fontBangChu = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, mauChu);
            Font fontNho = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, mauMo);
            Font fontChanKy = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, mauChu);

            // ---------- Dau bien lai: ten truong + tieu de + so phieu ----------
            Paragraph truong = new Paragraph(tenTruong == null || tenTruong.isBlank() ? "TRUONG DAI HOC" : tenTruong, fontTruong);
            truong.setAlignment(Element.ALIGN_CENTER);
            document.add(truong);

            Paragraph phongBan = new Paragraph("Phong Ke hoach - Tai chinh", fontPhongBan);
            phongBan.setAlignment(Element.ALIGN_CENTER);
            phongBan.setSpacingAfter(10);
            document.add(phongBan);

            LineSeparator gachNgang = new LineSeparator(1.2f, 100, mauChinh, Element.ALIGN_CENTER, -2);
            document.add(new Chunk(gachNgang));

            Paragraph tieuDe = new Paragraph("BIEN LAI THU HOC PHI", fontTieuDe);
            tieuDe.setAlignment(Element.ALIGN_CENTER);
            tieuDe.setSpacingBefore(10);
            document.add(tieuDe);

            Paragraph soPhieu = new Paragraph(
                    "So phieu: BL" + String.format("%06d", maPhieuThu) + "      Ngay lap: " + ngayNop, fontSoPhieu);
            soPhieu.setAlignment(Element.ALIGN_CENTER);
            soPhieu.setSpacingAfter(16);
            document.add(soPhieu);

            // ---------- Thong tin sinh vien / giao dich ----------
            PdfPTable bangThongTin = new PdfPTable(2);
            bangThongTin.setWidthPercentage(100);
            bangThongTin.setWidths(new float[]{1.3f, 2f});

            themDong(bangThongTin, "Ho va ten:", hoTenSV, fontNhan, fontGiaTri);
            themDong(bangThongTin, "Ma so sinh vien:", maSV, fontNhan, fontGiaTri);
            themDong(bangThongTin, "Hoc ky:", tenHocKy, fontNhan, fontGiaTri);
            themDong(bangThongTin, "Hinh thuc thanh toan:", dichHinhThuc(hinhThuc), fontNhan, fontGiaTri);
            themDong(bangThongTin, "Nguoi thu:",
                    (nguoiThu == null || nguoiThu.isBlank()) ? "He thong (thanh toan online)" : nguoiThu,
                    fontNhan, fontGiaTri);
            if (maGiaoDich != null && !maGiaoDich.isBlank()) {
                themDong(bangThongTin, "Ma giao dich:", maGiaoDich, fontNhan, fontGiaTri);
            }
            document.add(bangThongTin);

            document.add(new Paragraph(" ", fontNho));

            // ---------- O so tien noi bat ----------
            PdfPTable oSoTien = new PdfPTable(1);
            oSoTien.setWidthPercentage(100);
            PdfPCell oTien = new PdfPCell();
            oTien.setBackgroundColor(mauNenNhat);
            oTien.setBorderColor(mauChinh);
            oTien.setBorderWidth(1f);
            oTien.setPadding(12);

            Paragraph nhanSoTien = new Paragraph("SO TIEN DA NOP", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, mauMo));
            nhanSoTien.setAlignment(Element.ALIGN_CENTER);
            oTien.addElement(nhanSoTien);

            Paragraph giaTriTien = new Paragraph(MoneyUtils.format(soTienNop), fontSoTienLon);
            giaTriTien.setAlignment(Element.ALIGN_CENTER);
            giaTriTien.setSpacingBefore(4);
            oTien.addElement(giaTriTien);

            Paragraph bangChu = new Paragraph("(" + soTienBangChu(soTienNop) + ")", fontBangChu);
            bangChu.setAlignment(Element.ALIGN_CENTER);
            bangChu.setSpacingBefore(4);
            oTien.addElement(bangChu);

            oSoTien.addCell(oTien);
            document.add(oSoTien);

            document.add(new Paragraph(" ", fontNho));

            // ---------- Bang tong ket cong no cua hoa don ----------
            PdfPTable bangTongKet = new PdfPTable(2);
            bangTongKet.setWidthPercentage(100);
            bangTongKet.setWidths(new float[]{2f, 1.2f});

            BigDecimal daNopLuyKe = tongHocPhi.subtract(conNoConLai);
            themDongTongKet(bangTongKet, "Tong hoc phi hoc ky", MoneyUtils.format(tongHocPhi), fontNhan, fontGiaTri, mauVien);
            themDongTongKet(bangTongKet, "Da nop (luy ke)", MoneyUtils.format(daNopLuyKe), fontNhan, fontGiaTri, mauVien);

            boolean daDongDu = conNoConLai.compareTo(BigDecimal.ZERO) <= 0;
            PdfPCell nhanConLai = new PdfPCell(new Phrase("Con lai", fontNhan));
            nhanConLai.setBorder(Rectangle.TOP);
            nhanConLai.setBorderColor(mauVien);
            nhanConLai.setPadding(6);
            bangTongKet.addCell(nhanConLai);

            Font fontConLai = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, daDongDu ? mauXanhLa : mauDo);
            PdfPCell giaTriConLai = new PdfPCell(new Phrase(daDongDu ? "Da dong du" : MoneyUtils.format(conNoConLai), fontConLai));
            giaTriConLai.setBorder(Rectangle.TOP);
            giaTriConLai.setBorderColor(mauVien);
            giaTriConLai.setPadding(6);
            giaTriConLai.setHorizontalAlignment(Element.ALIGN_RIGHT);
            bangTongKet.addCell(giaTriConLai);

            document.add(bangTongKet);

            document.add(new Paragraph(" ", fontNho));
            document.add(new Paragraph(" ", fontNho));

            // ---------- Chu ky ----------
            PdfPTable bangChuKy = new PdfPTable(2);
            bangChuKy.setWidthPercentage(100);

            PdfPCell oNguoiNop = new PdfPCell();
            oNguoiNop.setBorder(Rectangle.NO_BORDER);
            Paragraph tieuNguoiNop = new Paragraph("NGUOI NOP TIEN", fontChanKy);
            tieuNguoiNop.setAlignment(Element.ALIGN_CENTER);
            oNguoiNop.addElement(tieuNguoiNop);
            Paragraph ghiChuNop = new Paragraph("(Ky, ghi ro ho ten)", fontNho);
            ghiChuNop.setAlignment(Element.ALIGN_CENTER);
            ghiChuNop.setSpacingBefore(30);
            oNguoiNop.addElement(ghiChuNop);
            Paragraph tenNop = new Paragraph(hoTenSV, fontGiaTri);
            tenNop.setAlignment(Element.ALIGN_CENTER);
            oNguoiNop.addElement(tenNop);
            bangChuKy.addCell(oNguoiNop);

            PdfPCell oNguoiThu = new PdfPCell();
            oNguoiThu.setBorder(Rectangle.NO_BORDER);
            Paragraph tieuNguoiThu = new Paragraph("NGUOI THU TIEN", fontChanKy);
            tieuNguoiThu.setAlignment(Element.ALIGN_CENTER);
            oNguoiThu.addElement(tieuNguoiThu);
            Paragraph ghiChuThu = new Paragraph("(Ky, ghi ro ho ten)", fontNho);
            ghiChuThu.setAlignment(Element.ALIGN_CENTER);
            ghiChuThu.setSpacingBefore(30);
            oNguoiThu.addElement(ghiChuThu);
            Paragraph tenThu = new Paragraph(nguoiThu == null ? "" : nguoiThu, fontGiaTri);
            tenThu.setAlignment(Element.ALIGN_CENTER);
            oNguoiThu.addElement(tenThu);
            bangChuKy.addCell(oNguoiThu);

            document.add(bangChuKy);

            Paragraph ghiChuCuoi = new Paragraph(
                    "Bien lai duoc he thong tao tu dong luc " + ngayNop + ". Vui long luu lai de doi chieu khi can thiet.",
                    fontNho);
            ghiChuCuoi.setAlignment(Element.ALIGN_CENTER);
            ghiChuCuoi.setSpacingBefore(20);
            document.add(ghiChuCuoi);

        } catch (DocumentException e) {
            throw new IOException("Loi tao bien lai PDF: " + e.getMessage(), e);
        } finally {
            if (daMo && document.isOpen()) {
                document.close();
            }
        }
    }

    private static void themDong(PdfPTable bang, String nhan, String giaTri, Font fontNhan, Font fontGiaTri) {
        PdfPCell oNhan = new PdfPCell(new Phrase(nhan, fontNhan));
        oNhan.setBorder(Rectangle.NO_BORDER);
        oNhan.setPaddingBottom(6);
        bang.addCell(oNhan);

        PdfPCell oGiaTri = new PdfPCell(new Phrase(giaTri == null ? "" : giaTri, fontGiaTri));
        oGiaTri.setBorder(Rectangle.NO_BORDER);
        oGiaTri.setPaddingBottom(6);
        bang.addCell(oGiaTri);
    }

    private static void themDongTongKet(PdfPTable bang, String nhan, String giaTri,
                                        Font fontNhan, Font fontGiaTri, BaseColor mauVien) {
        PdfPCell oNhan = new PdfPCell(new Phrase(nhan, fontNhan));
        oNhan.setBorder(Rectangle.BOTTOM);
        oNhan.setBorderColor(mauVien);
        oNhan.setPadding(6);
        bang.addCell(oNhan);

        PdfPCell oGiaTri = new PdfPCell(new Phrase(giaTri, fontGiaTri));
        oGiaTri.setBorder(Rectangle.BOTTOM);
        oGiaTri.setBorderColor(mauVien);
        oGiaTri.setPadding(6);
        oGiaTri.setHorizontalAlignment(Element.ALIGN_RIGHT);
        bang.addCell(oGiaTri);
    }

    private static String dichHinhThuc(String hinhThuc) {
        if (hinhThuc == null) return "";
        switch (hinhThuc) {
            case "TIEN_MAT": return "Tien mat";
            case "CHUYEN_KHOAN": return "Chuyen khoan ngan hang";
            case "THANH_TOAN_ONLINE": return "Thanh toan online";
            default: return hinhThuc;
        }
    }

    private static final String[] CHU_SO = {
            "khong", "mot", "hai", "ba", "bon", "nam", "sau", "bay", "tam", "chin"
    };

    /** Doc 1 so tien (VND, phan nguyen) thanh chu, kieu "Ba trieu, hai tram nghin dong". */
    public static String soTienBangChu(BigDecimal soTien) {
        if (soTien == null) return "";
        long so = soTien.longValue();
        if (so == 0) return "Khong dong";
        if (so < 0) return "Am " + soTienBangChu(soTien.negate());

        String[] donViNhom = {"", " nghin", " trieu", " ty"};
        List<Integer> nhoms = new ArrayList<>();
        long tmp = so;
        while (tmp > 0) {
            nhoms.add((int) (tmp % 1000));
            tmp /= 1000;
        }

        StringBuilder ketQua = new StringBuilder();
        for (int i = nhoms.size() - 1; i >= 0; i--) {
            int nhom = nhoms.get(i);
            if (nhom == 0) continue;
            boolean coNhomLonHonTruocDo = i < nhoms.size() - 1;
            ketQua.append(docNhom3So(nhom, coNhomLonHonTruocDo)).append(donViNhom[i]).append(", ");
        }

        String ketQuaCuoi = ketQua.toString();
        if (ketQuaCuoi.endsWith(", ")) {
            ketQuaCuoi = ketQuaCuoi.substring(0, ketQuaCuoi.length() - 2);
        }
        ketQuaCuoi = ketQuaCuoi.substring(0, 1).toUpperCase() + ketQuaCuoi.substring(1);
        return ketQuaCuoi + " dong";
    }

    /** Doc 1 nhom 3 chu so (0..999). coNhomLonHonTruocDo=true neu phia truoc con nhom khac (vd "1 trieu, khong tram linh nam nghin"). */
    private static String docNhom3So(int soNhom, boolean coNhomLonHonTruocDo) {
        int tram = soNhom / 100;
        int chuc = (soNhom % 100) / 10;
        int donVi = soNhom % 10;
        StringBuilder sb = new StringBuilder();

        if (tram > 0 || coNhomLonHonTruocDo) {
            sb.append(CHU_SO[tram]).append(" tram ");
        }

        if (chuc == 0) {
            if (donVi > 0 && (tram > 0 || coNhomLonHonTruocDo)) sb.append("linh ");
        } else if (chuc == 1) {
            sb.append("muoi ");
        } else {
            sb.append(CHU_SO[chuc]).append(" muoi ");
        }

        if (donVi == 1 && chuc >= 2) {
            sb.append("mot");
        } else if (donVi == 5 && chuc >= 1) {
            sb.append("lam");
        } else if (donVi > 0) {
            sb.append(CHU_SO[donVi]);
        }

        return sb.toString().trim();
    }
}