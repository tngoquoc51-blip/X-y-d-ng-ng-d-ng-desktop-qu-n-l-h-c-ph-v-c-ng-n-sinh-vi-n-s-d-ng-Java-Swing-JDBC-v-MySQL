package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.model.TaiKhoan;

/**
 * Noi giu tham chieu toi tai khoan DANG DANG NHAP trong phien lam viec hien tai
 * cua ung dung desktop (chi 1 nguoi dung/lan chay, khac voi web server nhieu
 * nguoi dung dong thoi nen dung bien static la an toan o day). Duoc gan 1 lan
 * duy nhat ngay sau khi dang nhap thanh cong (xem MainFrame), sau do bat ky
 * Service nao cung co the doc lai de biet "ai" dang thuc hien hanh dong, phuc
 * vu ghi Nhat ky he thong ma khong can sua chu ky (constructor) cua tat ca
 * cac Service da co san.
 */
public class AuditContext {
    private static TaiKhoan nguoiDungHienTai;

    public static void datNguoiDung(TaiKhoan tk) {
        nguoiDungHienTai = tk;
    }

    public static TaiKhoan layNguoiDung() {
        return nguoiDungHienTai;
    }
}