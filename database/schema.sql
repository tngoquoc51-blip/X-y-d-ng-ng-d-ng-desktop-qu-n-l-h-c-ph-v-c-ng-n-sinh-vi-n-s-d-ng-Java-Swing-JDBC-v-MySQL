-- ============================================================
-- CSDL: Quan ly hoc phi va cong no sinh vien
-- ============================================================
CREATE DATABASE IF NOT EXISTS qlhocphi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qlhocphi;

-- ---------------- Tai khoan dang nhap ----------------
CREATE TABLE TaiKhoan (
                          MaTK INT AUTO_INCREMENT PRIMARY KEY,
                          TenDangNhap VARCHAR(50) NOT NULL UNIQUE,
                          MatKhauHash VARCHAR(255) NOT NULL,
                          HoTen VARCHAR(100) NOT NULL,
                          VaiTro ENUM('ADMIN','KETOAN','SINHVIEN') NOT NULL DEFAULT 'KETOAN',
                          MaSV VARCHAR(20) DEFAULT NULL,          -- lien ket toi sinh vien neu VaiTro = SINHVIEN
                          TrangThai TINYINT NOT NULL DEFAULT 1,   -- 1 = hoat dong, 0 = khoa
                          NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP
);


-- ---------------- Sinh vien ----------------
CREATE TABLE SinhVien (
                          MaSV VARCHAR(20) PRIMARY KEY,
                          HoTen VARCHAR(100) NOT NULL,
                          Lop VARCHAR(30),
                          Khoa VARCHAR(100),
                          NgaySinh DATE,
                          Email VARCHAR(100),
                          SoDienThoai VARCHAR(20),
                          TrangThai TINYINT NOT NULL DEFAULT 1
);

-- ---------------- Hoc ky ----------------
CREATE TABLE HocKy (
                       MaHocKy INT AUTO_INCREMENT PRIMARY KEY,
                       TenHocKy VARCHAR(50) NOT NULL,
                       NamHoc VARCHAR(20) NOT NULL,
                       DonGiaTinChi DECIMAL(12,0) NOT NULL DEFAULT 0,
                       NgayBatDau DATE,
                       NgayKetThuc DATE
);

-- ---------------- Hoa don hoc phi ----------------
CREATE TABLE HoaDonHocPhi (
                              MaHoaDon INT AUTO_INCREMENT PRIMARY KEY,
                              MaSV VARCHAR(20) NOT NULL,
                              MaHocKy INT NOT NULL,
                              SoTinChi INT NOT NULL DEFAULT 0,
                              SoTien DECIMAL(12,0) NOT NULL,
                              HanThanhToan DATE,
                              NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV) ON DELETE CASCADE,
                              FOREIGN KEY (MaHocKy) REFERENCES HocKy(MaHocKy) ON DELETE CASCADE
);

-- ---------------- Phieu thu (giao dich thanh toan) ----------------
CREATE TABLE PhieuThu (
                          MaPhieuThu INT AUTO_INCREMENT PRIMARY KEY,
                          MaHoaDon INT NOT NULL,
                          SoTienNop DECIMAL(12,0) NOT NULL,
                          NgayNop DATETIME DEFAULT CURRENT_TIMESTAMP,
                          HinhThuc ENUM('TIEN_MAT','CHUYEN_KHOAN','THANH_TOAN_ONLINE') NOT NULL DEFAULT 'TIEN_MAT',
                          MaGiaoDich VARCHAR(100),          -- ma giao dich tu cong thanh toan (neu co)
                          NguoiThu VARCHAR(100),
                          FOREIGN KEY (MaHoaDon) REFERENCES HoaDonHocPhi(MaHoaDon) ON DELETE CASCADE
);

-- ---------------- Du lieu mau ----------------
INSERT INTO TaiKhoan (TenDangNhap, MatKhauHash, HoTen, VaiTro)
VALUES ('admin', SHA2('admin123', 256), 'Quan tri vien', 'ADMIN');

INSERT INTO HocKy (TenHocKy, NamHoc, DonGiaTinChi, NgayBatDau, NgayKetThuc)
VALUES ('Hoc ky 1', '2025-2026', 550000, '2025-09-01', '2026-01-15');

INSERT INTO SinhVien (MaSV, HoTen, Lop, Khoa, Email) VALUES
                                                         ('SV001', 'Nguyen Van A', 'CNTT01', 'Cong nghe thong tin', 'a@example.com'),
                                                         ('SV002', 'Tran Thi B', 'CNTT01', 'Cong nghe thong tin', 'b@example.com');