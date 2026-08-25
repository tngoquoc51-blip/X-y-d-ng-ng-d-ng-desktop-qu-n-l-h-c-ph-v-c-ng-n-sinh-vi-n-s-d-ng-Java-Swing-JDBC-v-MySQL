USE qlhocphi;

-- ============================================================
-- MIGRATION: Dang nhap Google + bat buoc doi mat khau lan dau
-- Chay 1 LAN DUY NHAT sau khi da co schema.sql + seed_data.sql.
--
-- Cach chay:
--   mysql -u root -p qlhocphi < database/migration_google_login.sql
-- ============================================================

ALTER TABLE TaiKhoan
    ADD COLUMN GoogleEmail VARCHAR(150) NULL UNIQUE AFTER MaSV,
    ADD COLUMN BatBuocDoiMatKhau TINYINT(1) NOT NULL DEFAULT 0 AFTER TrangThai;

-- Vi du: gan san tai khoan Google cho SV001 de test dang nhap Google ngay
-- (doi "sv001.test@gmail.com" thanh Gmail that cua ban de tu test).
-- UPDATE TaiKhoan SET GoogleEmail = 'sv001.test@gmail.com' WHERE TenDangNhap = 'sv001';