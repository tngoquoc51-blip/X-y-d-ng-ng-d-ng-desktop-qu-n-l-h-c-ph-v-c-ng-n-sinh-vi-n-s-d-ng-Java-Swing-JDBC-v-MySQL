USE qlhocphi;

-- 1. Mở rộng ENUM VaiTro
ALTER TABLE taikhoan
    MODIFY COLUMN VaiTro ENUM('ADMIN','PHONGDAOTAO','KETOAN','SINHVIEN')
    COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'KETOAN';

-- 2. Mở rộng ENUM bảng thongbao
ALTER TABLE thongbao
    MODIFY COLUMN VaiTroNhan ENUM('ADMIN','PHONGDAOTAO','KETOAN','SINHVIEN','ALL')
    COLLATE utf8mb4_unicode_ci NOT NULL;

-- 3. Tạo tài khoản Phòng Đào Tạo (mật khẩu: 123456 – cùng hash với hệ thống hiện tại)
INSERT INTO taikhoan (TenDangNhap, MatKhauHash, HoTen, VaiTro, TrangThai, BatBuocDoiMatKhau)
SELECT 'pdt',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Phòng Đào Tạo',
       'PHONGDAOTAO',
       1,
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM taikhoan WHERE TenDangNhap = 'pdt');