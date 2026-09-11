package vn.edu.eaut.qlhocphi.model;

/** Vai tro cua tai khoan dang nhap he thong. */
public enum VaiTro {
    ADMIN,          // Chi bao tri & cau hinh he thong (tai khoan, backup, nhat ky)
    PHONGDAOTAO,    // Phong dao tao: quan ly sinh vien, hoc ky, hoa don, cong no, bao cao...
    KETOAN,         // Quan ly sinh vien, hoa don, thanh toan, cong no
    SINHVIEN        // Chi xem cong no/lich su thanh toan cua chinh minh
}