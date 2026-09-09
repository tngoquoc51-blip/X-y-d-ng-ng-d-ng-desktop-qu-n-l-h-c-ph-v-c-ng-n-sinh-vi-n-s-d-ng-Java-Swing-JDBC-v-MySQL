package vn.edu.eaut.qlhocphi.ai;

import java.math.BigDecimal;

/**
 * Ket qua AI doc duoc tu 1 anh chup bien lai/hoa don giay. Cac truong co the NULL
 * neu AI khong doc ro duoc tren anh (chu mo, anh chup lech, bien lai viet tay xau...)
 * - GUI se de nguoi dung tu kiem tra/sua lai truoc khi luu, khong bao gio tu dong
 * ghi thang vao CSDL de tranh sai lech tien bac.
 */
public class KetQuaOcrBienLai {
    private Integer maHoaDon;      // So hoa don neu tren bien lai co in (VD bien lai da in tu he thong truoc do)
    private String maSV;           // Ma sinh vien doc duoc (neu co)
    private String hoTen;          // Ho ten sinh vien tren bien lai
    private BigDecimal soTien;     // So tien nop
    private String ngay;           // Ngay tren bien lai, dang chuoi "dd/MM/yyyy" (giu nguyen chuoi AI tra ve, GUI tu parse)
    private String hinhThuc;       // "TIEN_MAT" hoac "CHUYEN_KHOAN" neu doan duoc, khong thi de null
    private String ghiChu;         // Ghi chu / noi dung khac AI doc duoc tren bien lai
    private boolean docDuoc = true;    // false neu anh qua mo/khong phai bien lai, khong doc duoc gi ca
    private String canhBao;        // Loi nhac cho ke toan, VD "Khong doc duoc so hoa don, vui long nhap tay"

    public Integer getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(Integer maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getHinhThuc() { return hinhThuc; }
    public void setHinhThuc(String hinhThuc) { this.hinhThuc = hinhThuc; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isDocDuoc() { return docDuoc; }
    public void setDocDuoc(boolean docDuoc) { this.docDuoc = docDuoc; }

    public String getCanhBao() { return canhBao; }
    public void setCanhBao(String canhBao) { this.canhBao = canhBao; }
}
