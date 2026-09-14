-- ============================================================
-- Migration: Hỗ trợ mã hóa dữ liệu nhạy cảm (AES-256-GCM)
-- ============================================================
-- Không cần thay đổi cấu trúc bảng vì vẫn lưu VARCHAR.
-- Ứng dụng sẽ tự động:
--   - Mã hóa khi INSERT / UPDATE (Email, SoDienThoai, SoDienThoaiPhuHuynh, QueQuan, DiaChi)
--   - Giải mã khi SELECT
--   - Dữ liệu cũ (plaintext) vẫn đọc được bình thường.
--
-- Sau khi deploy code mới, chạy một lần:
--   DataEncryptionMigrator.main() để mã hóa toàn bộ dữ liệu cũ.
-- ============================================================

-- Chỉ cần đảm bảo cột đủ dài (Base64 + prefix dài hơn plaintext)
ALTER TABLE SinhVien
    MODIFY COLUMN Email VARCHAR(500),
    MODIFY COLUMN SoDienThoai VARCHAR(200),
    MODIFY COLUMN SoDienThoaiPhuHuynh VARCHAR(200),
    MODIFY COLUMN QueQuan VARCHAR(500),
    MODIFY COLUMN DiaChi VARCHAR(500);