USE qlhocphi;

-- Tao tai khoan dang nhap cho SV001 (mat khau: 123456)
INSERT INTO TaiKhoan (TenDangNhap, MatKhauHash, HoTen, VaiTro, MaSV)
VALUES ('sv001', SHA2('123456', 256), 'Nguyen Van A', 'SINHVIEN', 'SV001');

-- Tao 1 hoa don hoc phi cho SV001 o Hoc ky 1 (MaHocKy = 1 co san tu schema.sql)
INSERT INTO HoaDonHocPhi (MaSV, MaHocKy, SoTinChi, SoTien, HanThanhToan)
VALUES ('SV001', 1, 15, 8250000, '2025-10-30');

-- Ghi nhan 1 phieu thu (SV da dong 1 phan) - de bang "Lich su thanh toan" co du lieu
INSERT INTO PhieuThu (MaHoaDon, SoTienNop, HinhThuc, NguoiThu)
VALUES (LAST_INSERT_ID(), 3000000, 'TIEN_MAT', 'Ke toan A');