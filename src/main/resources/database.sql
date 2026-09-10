CREATE DATABASE IF NOT EXISTS `qlhocphi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `qlhocphi`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `taikhoan`
--

DROP TABLE IF EXISTS `taikhoan`;
CREATE TABLE `taikhoan` (
                            `MaTK` int NOT NULL AUTO_INCREMENT,
                            `TenDangNhap` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `MatKhauHash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `HoTen` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `VaiTro` enum('ADMIN','KETOAN','SINHVIEN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'KETOAN',
                            `MaSV` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `GoogleEmail` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `TrangThai` tinyint NOT NULL DEFAULT '1',
                            `BatBuocDoiMatKhau` tinyint(1) NOT NULL DEFAULT '0',
                            `NgayTao` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`MaTK`),
                            UNIQUE KEY `TenDangNhap` (`TenDangNhap`),
                            KEY `idx_google_email` (`GoogleEmail`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `taikhoan` WRITE;
INSERT INTO `taikhoan` VALUES
                           (2,'20231552@eaut.edu.vn','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Ngô Quốc Tuấn','SINHVIEN','20231552',NULL,1,0,'2026-08-12 10:28:08'),
                           (3,'ngoquoctuan','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Quản trị viên','ADMIN',NULL,NULL,1,0,'2026-08-13 09:23:49'),
                           (6,'sv001','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Nguyễn Thu Huyền','SINHVIEN','SV001','ngoquoctuanabc29@gmail.com',1,0,'2026-08-13 16:50:06'),
                           (7,'sv002','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Ngô Quốc Tuấn','SINHVIEN','20231552','tngoquoc51@gmail.com',1,0,'2026-08-23 12:36:26'),
                           (8,'kt001','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Ngô Quốc Tuấn','KETOAN',NULL,'ngoquoctuanabc29@gmail.com',1,0,'2026-08-25 12:39:33');
UNLOCK TABLES;

--
-- Table structure for table `sinhvien`
--

DROP TABLE IF EXISTS `sinhvien`;
CREATE TABLE `sinhvien` (
                            `MaSV` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `HoTen` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `Lop` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `Khoa` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `NgaySinh` date DEFAULT NULL,
                            `Email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `SoDienThoai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `TrangThai` tinyint NOT NULL DEFAULT '1',
                            `AnhDaiDien` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `SoDienThoaiPhuHuynh` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `QueQuan` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `DiaChi` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            PRIMARY KEY (`MaSV`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `sinhvien` WRITE;
INSERT INTO `sinhvien` VALUES
                           ('09093003','Nguyễn Thu Huyền','DL14.5','Du lịch','2003-09-09','ngoquoctuanabc29@gmail.com','0962817574',1,NULL,'0962817574','Hà Nội','Hà Nội'),
                           ('20230101','Nguyễn Văn An','CNTT14.1','Công nghệ thông tin','2005-03-12','20230101@eaut.edu.vn','0901000101',1,NULL,'0962817574',NULL,NULL),
                           ('20230102','Trần Thị Bích','CNTT14.1','Công nghệ thông tin','2005-07-21','20230102@eaut.edu.vn','0901000102',1,NULL,'0962817574',NULL,NULL),
                           ('20230103','Lê Hoàng Cường','CNTT14.1','Công nghệ thông tin','2005-01-05','20230103@eaut.edu.vn','0901000103',1,NULL,'0962817574',NULL,NULL),
                           ('20230104','Phạm Thị Dũng','CNTT14.1','Công nghệ thông tin','2004-11-30','20230104@eaut.edu.vn','0901000104',1,NULL,'0962817574',NULL,NULL),
                           ('20230105','Hoàng Văn Đức','CNTT14.2','Công nghệ thông tin','2005-05-18','20230105@eaut.edu.vn','0901000105',1,NULL,'0962817574',NULL,NULL),
                           ('20230106','Vũ Thị Giang','CNTT14.2','Công nghệ thông tin','2005-09-09','20230106@eaut.edu.vn','0901000106',1,NULL,'0962817574',NULL,NULL),
                           ('20230107','Đặng Văn Hải','CNTT14.2','Công nghệ thông tin','2004-12-14','20230107@eaut.edu.vn','0901000107',1,NULL,'0962817574',NULL,NULL),
                           ('20230108','Bùi Thị Hòa','CNTT14.2','Công nghệ thông tin','2005-02-27','20230108@eaut.edu.vn','0901000108',1,NULL,'0962817574',NULL,NULL),
                           ('20230201','Ngô Quốc Khánh','KT15.1','Kinh tế','2005-04-16','20230201@eaut.edu.vn','0901000201',1,NULL,'0962817574',NULL,NULL),
                           ('20230202','Đỗ Thị Lan','KT15.1','Kinh tế','2005-08-08','20230202@eaut.edu.vn','0901000202',1,NULL,'0962817574',NULL,NULL),
                           ('20230203','Nguyễn Văn Minh','KT15.1','Kinh tế','2004-10-22','20230203@eaut.edu.vn','0901000203',1,NULL,'0962817574',NULL,NULL),
                           ('20230204','Trần Thị Ngọc','KT15.1','Kinh tế','2005-06-03','20230204@eaut.edu.vn','0901000204',1,NULL,'0962817574',NULL,NULL),
                           ('20230205','Lê Văn Phúc','KT15.2','Kinh tế','2005-01-19','20230205@eaut.edu.vn','0901000205',1,NULL,'0962817574',NULL,NULL),
                           ('20230206','Phạm Thị Quyên','KT15.2','Kinh tế','2004-09-25','20230206@eaut.edu.vn','0901000206',1,NULL,'0962817574',NULL,NULL),
                           ('20230207','Hoàng Văn Sơn','KT15.2','Kinh tế','2005-03-30','20230207@eaut.edu.vn','0901000207',1,NULL,'0962817574',NULL,NULL),
                           ('20230208','Vũ Thị Thảo','KT15.2','Kinh tế','2005-11-11','20230208@eaut.edu.vn','0901000208',1,NULL,'0962817574',NULL,NULL),
                           ('20230301','Đặng Văn Thành','CK12.1','Cơ khí','2004-05-05','20230301@eaut.edu.vn','0901000301',1,NULL,'0962817574',NULL,NULL),
                           ('20230302','Bùi Thị Thu','CK12.1','Cơ khí','2005-07-17','20230302@eaut.edu.vn','0901000302',1,NULL,'0962817574',NULL,NULL),
                           ('20230303','Ngô Văn Tiến','CK12.1','Cơ khí','2005-02-14','20230303@eaut.edu.vn','0901000303',1,NULL,'0962817574',NULL,NULL),
                           ('20230304','Đỗ Thị Trang','CK12.1','Cơ khí','2004-08-29','20230304@eaut.edu.vn','0901000304',1,NULL,'0962817574',NULL,NULL),
                           ('20230305','Nguyễn Văn Tuấn','CK12.2','Cơ khí','2005-04-06','20230305@eaut.edu.vn','0901000305',1,NULL,'0962817574',NULL,NULL),
                           ('20230306','Trần Thị Uyên','CK12.2','Cơ khí','2005-10-10','20230306@eaut.edu.vn','0901000306',1,NULL,'0962817574',NULL,NULL),
                           ('20230307','Lê Văn Việt','CK12.2','Cơ khí','2004-12-01','20230307@eaut.edu.vn','0901000307',1,NULL,'0962817574',NULL,NULL),
                           ('20230308','Phạm Thị Xuân','CK12.2','Cơ khí','2005-06-23','20230308@eaut.edu.vn','0901000308',1,NULL,'0962817574',NULL,NULL),
                           ('20230401','Hoàng Văn Anh','DDT13.1','Điện - Điện tử','2005-01-08','20230401@eaut.edu.vn','0901000401',1,NULL,'0962817574',NULL,NULL),
                           ('20230402','Vũ Thị Bảo','DDT13.1','Điện - Điện tử','2004-11-15','20230402@eaut.edu.vn','0901000402',1,NULL,'0962817574',NULL,NULL),
                           ('20230403','Đặng Văn Chinh','DDT13.1','Điện - Điện tử','2005-09-19','20230403@eaut.edu.vn','0901000403',1,NULL,'0962817574',NULL,NULL),
                           ('20230404','Bùi Thị Diễm','DDT13.1','Điện - Điện tử','2005-03-27','20230404@eaut.edu.vn','0901000404',1,NULL,'0962817574',NULL,NULL),
                           ('20230405','Ngô Văn Dương','DDT13.2','Điện - Điện tử','2004-07-02','20230405@eaut.edu.vn','0901000405',1,NULL,'0962817574',NULL,NULL),
                           ('20230406','Đỗ Thị Hạnh','DDT13.2','Điện - Điện tử','2005-05-13','20230406@eaut.edu.vn','0901000406',1,NULL,'0962817574',NULL,NULL),
                           ('20230407','Nguyễn Văn Hiếu','DDT13.2','Điện - Điện tử','2005-02-20','20230407@eaut.edu.vn','0901000407',1,NULL,'0962817574',NULL,NULL),
                           ('20230408','Trần Thị Hương','DDT13.2','Điện - Điện tử','2004-10-04','20230408@eaut.edu.vn','0901000408',1,NULL,'0962817574',NULL,NULL),
                           ('20230501','Lê Văn Khoa','NN16.1','Ngoại ngữ','2005-08-16','20230501@eaut.edu.vn','0901000501',1,NULL,'0962817574',NULL,NULL),
                           ('20230502','Phạm Thị Liên','NN16.1','Ngoại ngữ','2005-04-24','20230502@eaut.edu.vn','0901000502',1,NULL,'0962817574',NULL,NULL),
                           ('20230503','Hoàng Văn Long','NN16.1','Ngoại ngữ','2004-12-09','20230503@eaut.edu.vn','0901000503',1,NULL,'0962817574',NULL,NULL),
                           ('20230504','Vũ Thị Mai','NN16.1','Ngoại ngữ','2005-06-30','20230504@eaut.edu.vn','0901000504',1,NULL,'0962817574',NULL,NULL),
                           ('20230505','Đặng Văn Nam','NN16.2','Ngoại ngữ','2005-01-27','20230505@eaut.edu.vn','0901000505',1,NULL,'0962817574',NULL,NULL),
                           ('20230506','Bùi Thị Oanh','NN16.2','Ngoại ngữ','2004-09-12','20230506@eaut.edu.vn','0901000506',1,NULL,'0962817574',NULL,NULL),
                           ('20230507','Ngô Văn Phong','NN16.2','Ngoại ngữ','2005-03-05','20230507@eaut.edu.vn','0901000507',1,NULL,'0962817574',NULL,NULL),
                           ('20230508','Đỗ Thị Quế','NN16.2','Ngoại ngữ','2005-11-28','20230508@eaut.edu.vn','0901000508',1,NULL,'0962817574',NULL,NULL),
                           ('20230601','Nguyễn Văn Sáng','XD11.1','Xây dựng','2004-06-19','20230601@eaut.edu.vn','0901000601',1,NULL,'0962817574',NULL,NULL),
                           ('20230602','Trần Thị Tám','XD11.1','Xây dựng','2005-02-08','20230602@eaut.edu.vn','0901000602',1,NULL,'0962817574',NULL,NULL),
                           ('20230603','Lê Văn Thắng','XD11.1','Xây dựng','2005-10-17','20230603@eaut.edu.vn','0901000603',1,NULL,'0962817574',NULL,NULL),
                           ('20230604','Phạm Thị Thủy','XD11.1','Xây dựng','2004-08-03','20230604@eaut.edu.vn','0901000604',1,NULL,'0962817574',NULL,NULL),
                           ('20230605','Hoàng Văn Tùng','XD11.2','Xây dựng','2005-05-26','20230605@eaut.edu.vn','0901000605',1,NULL,'0962817574',NULL,NULL),
                           ('20230606','Vũ Thị Vân','XD11.2','Xây dựng','2005-01-14','20230606@eaut.edu.vn','0901000606',1,NULL,'0962817574',NULL,NULL),
                           ('20230607','Đặng Văn Vinh','XD11.2','Xây dựng','2004-07-31','20230607@eaut.edu.vn','0901000607',1,NULL,'0962817574',NULL,NULL),
                           ('20230608','Bùi Thị Yến','XD11.2','Xây dựng','2005-09-22','20230608@eaut.edu.vn','0901000608',1,NULL,'0962817574',NULL,NULL),
                           ('20231475','Nguyễn Hữu Cát','DCCNTT14.4','Công nghệ thông tin',NULL,'20231475@eaut.edu.vn','',1,NULL,'0962817574',NULL,NULL),
                           ('20231552','Ngô Quốc Tuấn','DCCNTT14.4','Công nghệ thông tin',NULL,'ngoquoctuanabc29@gmail.com','0123456789',1,NULL,'0962817574',NULL,NULL),
                           ('20231553','Ngô Quốc Tuấn','DCCNTT14.4','Công nghệ thông tin','2005-03-07','ngoquoctuanabc29@gmail.com','0962817574',1,NULL,'0962817574','Hà Nội','Xuân Mai, Hà Nội'),
                           ('SV001','Nguyễn Thu Huyền','CNTT01','Công nghệ thông tin',NULL,'tngoquoc@51gmail.con','0962817574',1,'avatars/SV001.png','0962817574',NULL,NULL),
                           ('SV002','Trần Thị Bích','CNTT01','Công nghệ thông tin',NULL,'b@example.com',NULL,1,NULL,'0962817574',NULL,NULL),
                           ('TEST999','Nguyễn Văn Test','DCOTO14.4','Ô tô','2004-01-01','ngoquoctuanabc29@gmail.com','0962817574',1,NULL,'0962817574','Hà Nội','Hà Nội');
UNLOCK TABLES;

--
-- Table structure for table `hocky`
--

DROP TABLE IF EXISTS `hocky`;
CREATE TABLE `hocky` (
                         `MaHocKy` int NOT NULL AUTO_INCREMENT,
                         `TenHocKy` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                         `NamHoc` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
                         `DonGiaTinChi` decimal(12,0) NOT NULL DEFAULT '0',
                         `NgayBatDau` date DEFAULT NULL,
                         `NgayKetThuc` date DEFAULT NULL,
                         PRIMARY KEY (`MaHocKy`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `hocky` WRITE;
INSERT INTO `hocky` VALUES
                        (1,'Học kỳ 1','2025-2026',550000,'2025-09-01','2026-01-15'),
                        (4,'Học kỳ 1','2025-2026',550000,'2025-09-01','2026-01-15'),
                        (5,'Học kỳ 1','2025-2026',550000,'2025-09-01','2026-01-15'),
                        (6,'Học kỳ 1','2025-2026',550000,'2025-09-01','2026-01-15'),
                        (7,'Học kỳ 1','2025-2026',550000,'2025-09-01','2026-01-15'),
                        (8,'Học kỳ 2','2025-2026',550000,'2026-01-16','2026-05-30'),
                        (9,'Học kỳ Hè','2025-2026',550000,'2026-06-01','2026-08-15'),
                        (10,'Học kỳ 1','2024-2025',500000,'2024-09-01','2025-01-15'),
                        (12,'Học kỳ 2','2026-2027',50000,'2027-01-09','2027-05-25'),
                        (13,'Học kỳ Hè 2026','2026-2027',1000000,'2027-02-09','2027-09-30'),
                        (14,'Học kỳ học lại 2026','2025-2026',850000,'2026-09-09','2026-10-10'),
                        (15,'Học kỳ Hè 2026','2026-2027',350000,'2026-09-01','2026-09-29');
UNLOCK TABLES;

--
-- Table structure for table `hoadonhocphi`
--

DROP TABLE IF EXISTS `hoadonhocphi`;
CREATE TABLE `hoadonhocphi` (
                                `MaHoaDon` int NOT NULL AUTO_INCREMENT,
                                `MaSV` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
                                `MaHocKy` int NOT NULL,
                                `SoTinChi` int NOT NULL DEFAULT '0',
                                `SoTien` decimal(12,0) NOT NULL,
                                `HanThanhToan` date DEFAULT NULL,
                                `NgayTao` datetime DEFAULT CURRENT_TIMESTAMP,
                                `NgayNhacNoGanNhat` date DEFAULT NULL,
                                `TyLeMienGiam` decimal(5,2) NOT NULL DEFAULT '0.00',
                                `LyDoMienGiam` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                `ThoiDiemGuiEmailSV` datetime DEFAULT NULL,
                                `ThoiDiemGuiSmsPH` datetime DEFAULT NULL,
                                PRIMARY KEY (`MaHoaDon`),
                                KEY `MaSV` (`MaSV`),
                                KEY `MaHocKy` (`MaHocKy`),
                                CONSTRAINT `hoadonhocphi_ibfk_1` FOREIGN KEY (`MaSV`) REFERENCES `sinhvien` (`MaSV`) ON DELETE CASCADE,
                                CONSTRAINT `hoadonhocphi_ibfk_2` FOREIGN KEY (`MaHocKy`) REFERENCES `hocky` (`MaHocKy`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `hoadonhocphi` WRITE;
INSERT INTO `hoadonhocphi` VALUES
                               (3,'SV001',1,15,8250000,'2025-10-30','2026-08-13 16:57:00',NULL,0.00,NULL,NULL,NULL),
                               (4,'SV001',8,15,8250000,'2026-03-15','2026-08-21 14:06:30',NULL,0.00,NULL,NULL,NULL),
                               (5,'SV001',9,8,4400000,'2026-08-30','2026-08-21 14:06:30',NULL,0.00,NULL,NULL,NULL),
                               (6,'SV001',10,16,8000000,'2024-10-30','2026-08-21 14:06:30',NULL,0.00,NULL,NULL,NULL),
                               (8,'20231552',8,30,16500000,'2026-08-30','2026-08-28 15:08:35',NULL,0.00,NULL,NULL,NULL),
                               (9,'20231552',13,15,15000000,'2029-08-29','2026-08-29 00:10:08',NULL,0.00,NULL,NULL,NULL),
                               (10,'sv001',13,15,15000000,'2026-08-29','2026-08-29 00:10:35',NULL,0.00,NULL,NULL,NULL),
                               (11,'sv001',13,20,20000000,'2026-09-09','2026-08-29 21:09:14',NULL,0.00,NULL,NULL,NULL),
                               (12,'20231552',15,30,10500000,'2026-08-24','2026-09-01 14:18:22','2026-09-02',25.00,'Học bổng',NULL,NULL);
UNLOCK TABLES;

--
-- Table structure for table `calamviec`
--

DROP TABLE IF EXISTS `calamviec`;
CREATE TABLE `calamviec` (
                             `MaCa` int NOT NULL AUTO_INCREMENT,
                             `MaTK` int NOT NULL,
                             `TenNhanVien` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                             `ThoiGianMoCa` datetime NOT NULL,
                             `ThoiGianDongCa` datetime DEFAULT NULL,
                             `TongTienMat` decimal(12,0) NOT NULL DEFAULT '0',
                             `TongTienChuyenKhoan` decimal(12,0) NOT NULL DEFAULT '0',
                             `TongTienOnline` decimal(12,0) NOT NULL DEFAULT '0',
                             `SoGiaoDich` int NOT NULL DEFAULT '0',
                             `TrangThai` enum('DANG_MO','DA_DONG') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DANG_MO',
                             PRIMARY KEY (`MaCa`),
                             KEY `MaTK` (`MaTK`),
                             CONSTRAINT `calamviec_ibfk_1` FOREIGN KEY (`MaTK`) REFERENCES `taikhoan` (`MaTK`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `calamviec` WRITE;
INSERT INTO `calamviec` VALUES (1,8,'NGÔ QUỐC TUẤN','2026-08-30 16:19:45','2026-08-30 16:20:05',0,0,0,0,'DA_DONG');
UNLOCK TABLES;

--
-- Table structure for table `cauhinhnhacnotudong`
--

DROP TABLE IF EXISTS `cauhinhnhacnotudong`;
CREATE TABLE `cauhinhnhacnotudong` (
                                       `MaCauHinh` int NOT NULL AUTO_INCREMENT,
                                       `NgayGioBatDau` datetime NOT NULL,
                                       `NgayGioKetThuc` datetime NOT NULL,
                                       `DangApDung` tinyint(1) NOT NULL DEFAULT '0',
                                       `NgayTao` datetime DEFAULT CURRENT_TIMESTAMP,
                                       `NguoiTao` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                       PRIMARY KEY (`MaCauHinh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `giaodichnganhang`
--

DROP TABLE IF EXISTS `giaodichnganhang`;
CREATE TABLE `giaodichnganhang` (
                                    `MaGiaoDich` int NOT NULL AUTO_INCREMENT,
                                    `MaThamChieu` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
                                    `ThoiGianGiaoDich` datetime DEFAULT NULL,
                                    `SoTien` decimal(15,0) NOT NULL,
                                    `NoiDung` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                    `TrangThaiDoiSoat` enum('CHUA_XU_LY','TU_DONG_KHOP','NGHI_VAN','DA_XAC_NHAN','BO_QUA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CHUA_XU_LY',
                                    `MaHoaDonKhop` int DEFAULT NULL,
                                    `DoTinCay` int DEFAULT NULL,
                                    `NguoiXacNhan` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                    `ThoiGianXacNhan` datetime DEFAULT NULL,
                                    `ThoiGianNhap` datetime DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`MaGiaoDich`),
                                    UNIQUE KEY `uq_giaodich_thamchieu` (`MaThamChieu`),
                                    KEY `MaHoaDonKhop` (`MaHoaDonKhop`),
                                    CONSTRAINT `giaodichnganhang_ibfk_1` FOREIGN KEY (`MaHoaDonKhop`) REFERENCES `hoadonhocphi` (`MaHoaDon`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `giaodichnganhang` WRITE;
INSERT INTO `giaodichnganhang` VALUES
                                   (1,'FT26241000001','2026-08-28 17:00:00',15000000,'SV 20231552 chuyen khoan hoc phi Ngo Quoc Tuan','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),
                                   (2,'FT26241000002','2026-08-28 17:00:00',16500000,'thanh toan hoc phi 20231475 Nguyen Huu Cat','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),
                                   (3,'FT26241000003','2026-08-27 17:00:00',5000000,'chuyen tien hoc phi SV001 Nguyen Van A','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),
                                   (4,'FT26241000004','2026-08-26 17:00:00',3000000,'noi dung khong ro sinh vien nao ca','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),
                                   (9,'FT26241000100','2026-08-28 17:00:00',20000000,'NGUYEN THU HUYEN chuyen khoan thanh toan hoc phi','DA_XAC_NHAN',11,100,'ngoquoctuan','2026-08-29 21:47:39','2026-08-29 21:29:01');
UNLOCK TABLES;

--
-- Table structure for table `lichsunapvi`
--

DROP TABLE IF EXISTS `lichsunapvi`;
CREATE TABLE `lichsunapvi` (
                               `MaGiaoDich` int NOT NULL AUTO_INCREMENT,
                               `MaSV` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
                               `SoTien` decimal(12,0) NOT NULL,
                               `NgayNap` datetime DEFAULT CURRENT_TIMESTAMP,
                               `HinhThuc` enum('VNPAY','MOMO','TIEN_MAT') COLLATE utf8mb4_unicode_ci NOT NULL,
                               `MaGiaoDichCong` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                               PRIMARY KEY (`MaGiaoDich`),
                               KEY `MaSV` (`MaSV`),
                               CONSTRAINT `lichsunapvi_ibfk_1` FOREIGN KEY (`MaSV`) REFERENCES `sinhvien` (`MaSV`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `lichsunapvi` WRITE;
INSERT INTO `lichsunapvi` VALUES
                              (1,'20231552',283980000,'2026-08-28 14:30:34','VNPAY','MOCK-BEF78EB1'),
                              (2,'20231552',3000000,'2026-08-28 15:12:55','VNPAY','15669852'),
                              (3,'20231552',40000000,'2026-08-28 15:13:50','VNPAY','15669855');
UNLOCK TABLES;

--
-- Table structure for table `lichthutudong`
--

DROP TABLE IF EXISTS `lichthutudong`;
CREATE TABLE `lichthutudong` (
                                 `MaLich` int NOT NULL AUTO_INCREMENT,
                                 `MaHoaDon` int NOT NULL,
                                 `NgayBatDauQuet` date NOT NULL,
                                 `NgayKetThucQuet` date NOT NULL,
                                 `TrangThai` enum('DANG_CHO','DA_THU','HET_HAN','DA_HUY') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DANG_CHO',
                                 `NgayTao` datetime DEFAULT CURRENT_TIMESTAMP,
                                 `NguoiTao` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `LanQuetGanNhat` datetime DEFAULT NULL,
                                 PRIMARY KEY (`MaLich`),
                                 KEY `MaHoaDon` (`MaHoaDon`),
                                 CONSTRAINT `lichthutudong_ibfk_1` FOREIGN KEY (`MaHoaDon`) REFERENCES `hoadonhocphi` (`MaHoaDon`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `lichthutudong` WRITE;
INSERT INTO `lichthutudong` VALUES
                                (2,10,'2026-08-29','2026-08-29','DA_THU','2026-08-29 00:18:24','ngoquoctuan','2026-08-29 16:33:22'),
                                (3,9,'2026-08-29','2026-08-29','DA_THU','2026-08-29 00:22:03','ngoquoctuan','2026-08-29 00:24:14');
UNLOCK TABLES;

--
-- Table structure for table `nhatkyhethong`
--

DROP TABLE IF EXISTS `nhatkyhethong`;
CREATE TABLE `nhatkyhethong` (
                                 `MaNhatKy` int NOT NULL AUTO_INCREMENT,
                                 `MaTK` int DEFAULT NULL,
                                 `TenDangNhap` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                                 `HanhDong` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                                 `DoiTuong` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `ChiTiet` text COLLATE utf8mb4_unicode_ci,
                                 `ThoiGian` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`MaNhatKy`),
                                 KEY `fk_nhatky_taikhoan` (`MaTK`),
                                 CONSTRAINT `fk_nhatky_taikhoan` FOREIGN KEY (`MaTK`) REFERENCES `taikhoan` (`MaTK`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `nhatkyhethong` WRITE;
INSERT INTO `nhatkyhethong` VALUES
                                (1,NULL,'he_thong','DANG_NHAP','TaiKhoan','Đăng nhập thành công: ngoquoctuan','2026-08-28 10:13:15'),
                                (2,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-28 03:35:08'),
                                (3,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 03:35:24'),
                                (4,8,'kt001','DANG_NHAP','KETOAN','Kế toán đăng nhập: kt001 (Ngô Quốc Tuấn)','2026-08-28 07:26:15');
UNLOCK TABLES;

--
-- Table structure for table `phieuthu`
--

DROP TABLE IF EXISTS `phieuthu`;
CREATE TABLE `phieuthu` (
                            `MaPhieuThu` int NOT NULL AUTO_INCREMENT,
                            `MaHoaDon` int NOT NULL,
                            `SoTienNop` decimal(12,0) NOT NULL,
                            `NgayNop` datetime DEFAULT CURRENT_TIMESTAMP,
                            `HinhThuc` enum('TIEN_MAT','CHUYEN_KHOAN','THANH_TOAN_ONLINE','VI_DIEN_TU') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TIEN_MAT',
                            `MaGiaoDich` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `NguoiThu` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `TrangThai` enum('THANH_CONG','THAT_BAI','DA_HUY') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'THANH_CONG',
                            PRIMARY KEY (`MaPhieuThu`),
                            KEY `MaHoaDon` (`MaHoaDon`),
                            CONSTRAINT `phieuthu_ibfk_1` FOREIGN KEY (`MaHoaDon`) REFERENCES `hoadonhocphi` (`MaHoaDon`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `phieuthu` WRITE;
INSERT INTO `phieuthu` VALUES
                           (2,3,3000000,'2026-08-13 16:57:00','TIEN_MAT',NULL,'Ke toan A','THANH_CONG'),
                           (3,4,5000000,'2026-08-21 14:06:30','CHUYEN_KHOAN',NULL,'Ke toan B','THANH_CONG'),
                           (4,5,4400000,'2026-08-21 14:06:30','THANH_TOAN_ONLINE','ONL-20260801-001','Cong thanh toan online','THANH_CONG'),
                           (5,6,8000000,'2026-08-21 14:06:30','TIEN_MAT',NULL,'Ke toan A','THANH_CONG'),
                           (6,4,3250000,'2026-08-22 17:13:45','THANH_TOAN_ONLINE','15665017','He thong (VNPAY)','THANH_CONG'),
                           (7,3,5250000,'2026-08-22 17:14:34','THANH_TOAN_ONLINE','15665020','He thong (VNPAY)','THANH_CONG'),
                           (14,9,15000000,'2026-08-29 00:24:14','VI_DIEN_TU',NULL,'He thong (tu dong tru vi)','THANH_CONG'),
                           (15,8,16500000,'2026-08-29 16:04:43','THANH_TOAN_ONLINE','15670240','He thong (VNPAY)','THANH_CONG'),
                           (16,10,15000000,'2026-08-29 16:22:18','THANH_TOAN_ONLINE','4805413183','He thong (MOMO)','THANH_CONG'),
                           (17,11,20000000,'2026-08-29 21:47:39','CHUYEN_KHOAN','FT26241000100','Quan tri vien (doi soat tu dong)','THANH_CONG'),
                           (18,12,7875000,'2026-09-02 22:58:11','VI_DIEN_TU',NULL,'Sinh vien 20231552 (tu vi hoc phi)','THANH_CONG');
UNLOCK TABLES;

--
-- Table structure for table `theqrdangnhap`
--

DROP TABLE IF EXISTS `theqrdangnhap`;
CREATE TABLE `theqrdangnhap` (
                                 `MaThe` int NOT NULL AUTO_INCREMENT,
                                 `MaTK` int NOT NULL,
                                 `Token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
                                 `ThoiGianTao` datetime DEFAULT CURRENT_TIMESTAMP,
                                 `TrangThai` tinyint NOT NULL DEFAULT '1',
                                 PRIMARY KEY (`MaThe`),
                                 UNIQUE KEY `Token` (`Token`),
                                 KEY `MaTK` (`MaTK`),
                                 CONSTRAINT `theqrdangnhap_ibfk_1` FOREIGN KEY (`MaTK`) REFERENCES `taikhoan` (`MaTK`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `thongbao`
--

DROP TABLE IF EXISTS `thongbao`;
CREATE TABLE `thongbao` (
                            `MaThongBao` int NOT NULL AUTO_INCREMENT,
                            `ManHinhKey` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `VaiTroNhan` enum('ADMIN','KETOAN','SINHVIEN','ALL') COLLATE utf8mb4_unicode_ci NOT NULL,
                            `MaSVNhan` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `TieuDe` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `NoiDung` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `ThoiGianTao` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`MaThongBao`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `thongbao` WRITE;
INSERT INTO `thongbao` VALUES
                           (1,'sinhvien','ALL',NULL,'Sinh viên mới','Đã thêm sinh viên mới','2026-08-30 17:31:11'),
                           (2,'hoadon_sv','SINHVIEN','20231552','Hóa đơn mới','Thông báo hóa đơn học phí học kỳ mới','2026-08-30 17:32:01'),
                           (3,'hocky','ALL',NULL,'Học kỳ mới','Đã tạo học kỳ mới năm học 2026-2027','2026-08-30 17:33:54'),
                           (4,'vidientu','SINHVIEN','20231552','Ví học phí','Số dư ví của bạn vừa được cập nhật','2026-08-30 17:33:54'),
                           (5,'tongquan','SINHVIEN','20231552','Cảnh báo công nợ','Bạn đang có hóa đơn quá hạn thanh toán','2026-08-30 17:33:54');
UNLOCK TABLES;

--
-- Table structure for table `thongbaodadoc`
--

DROP TABLE IF EXISTS `thongbaodadoc`;
CREATE TABLE `thongbaodadoc` (
                                 `MaThongBao` int NOT NULL,
                                 `MaTK` int NOT NULL,
                                 `ThoiGianDoc` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`MaThongBao`,`MaTK`),
                                 KEY `MaTK` (`MaTK`),
                                 CONSTRAINT `thongbaodadoc_ibfk_1` FOREIGN KEY (`MaThongBao`) REFERENCES `thongbao` (`MaThongBao`) ON DELETE CASCADE,
                                 CONSTRAINT `thongbaodadoc_ibfk_2` FOREIGN KEY (`MaTK`) REFERENCES `taikhoan` (`MaTK`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `vidientu`
--

DROP TABLE IF EXISTS `vidientu`;
CREATE TABLE `vidientu` (
                            `MaSV` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
                            `SoDu` decimal(12,0) NOT NULL DEFAULT '0',
                            `NgayCapNhat` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`MaSV`),
                            CONSTRAINT `vidientu_ibfk_1` FOREIGN KEY (`MaSV`) REFERENCES `sinhvien` (`MaSV`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `vidientu` WRITE;
INSERT INTO `vidientu` VALUES ('20231552',21125000,'2026-09-02 22:58:11');
UNLOCK TABLES;

--
-- View hỗ trợ ứng dụng Java truy vấn công nợ nhanh chóng
--

CREATE OR REPLACE VIEW `v_cong_no_sinh_vien` AS
SELECT
    sv.MaSV,
    sv.HoTen,
    sv.Lop,
    hd.MaHoaDon,
    hd.SoTien,
    hd.HanThanhToan,
    CASE
        WHEN hd.HanThanhToan < CURDATE() THEN 'QUÁ HẠN'
        ELSE 'CHƯA THANH TOÁN'
        END AS TrangThaiNo
FROM sinhvien sv
         JOIN hoadonhocphi hd ON sv.MaSV = hd.MaSV;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;