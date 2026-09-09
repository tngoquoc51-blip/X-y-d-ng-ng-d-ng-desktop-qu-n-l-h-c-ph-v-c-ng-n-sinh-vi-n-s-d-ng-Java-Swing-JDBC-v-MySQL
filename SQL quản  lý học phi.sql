-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: qlhocphi
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `calamviec`
--

DROP TABLE IF EXISTS `calamviec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calamviec`
--

LOCK TABLES `calamviec` WRITE;
/*!40000 ALTER TABLE `calamviec` DISABLE KEYS */;
INSERT INTO `calamviec` VALUES (1,8,'NGÔ QUỐC TUẤN','2026-08-30 16:19:45','2026-08-30 16:20:05',0,0,0,0,'DA_DONG');
/*!40000 ALTER TABLE `calamviec` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cauhinhnhacnotudong`
--

DROP TABLE IF EXISTS `cauhinhnhacnotudong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cauhinhnhacnotudong` (
  `MaCauHinh` int NOT NULL AUTO_INCREMENT,
  `NgayGioBatDau` datetime NOT NULL,
  `NgayGioKetThuc` datetime NOT NULL,
  `DangApDung` tinyint(1) NOT NULL DEFAULT '0',
  `NgayTao` datetime DEFAULT CURRENT_TIMESTAMP,
  `NguoiTao` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`MaCauHinh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cauhinhnhacnotudong`
--

LOCK TABLES `cauhinhnhacnotudong` WRITE;
/*!40000 ALTER TABLE `cauhinhnhacnotudong` DISABLE KEYS */;
/*!40000 ALTER TABLE `cauhinhnhacnotudong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `giaodichnganhang`
--

DROP TABLE IF EXISTS `giaodichnganhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `giaodichnganhang`
--

LOCK TABLES `giaodichnganhang` WRITE;
/*!40000 ALTER TABLE `giaodichnganhang` DISABLE KEYS */;
INSERT INTO `giaodichnganhang` VALUES (1,'FT26241000001','2026-08-28 17:00:00',15000000,'SV 20231552 chuyen khoan hoc phi Ngo Quoc Tuan','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),(2,'FT26241000002','2026-08-28 17:00:00',16500000,'thanh toan hoc phi 20231475 Nguyen Huu Cat','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),(3,'FT26241000003','2026-08-27 17:00:00',5000000,'chuyen tien hoc phi SV001 Nguyen Van A','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),(4,'FT26241000004','2026-08-26 17:00:00',3000000,'noi dung khong ro sinh vien nao ca','CHUA_XU_LY',NULL,NULL,NULL,NULL,'2026-08-29 21:22:33'),(9,'FT26241000100','2026-08-28 17:00:00',20000000,'NGUYEN THU HUYEN chuyen khoan thanh toan hoc phi','DA_XAC_NHAN',11,100,'ngoquoctuan','2026-08-29 21:47:39','2026-08-29 21:29:01');
/*!40000 ALTER TABLE `giaodichnganhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hoadonhocphi`
--

DROP TABLE IF EXISTS `hoadonhocphi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoadonhocphi`
--

LOCK TABLES `hoadonhocphi` WRITE;
/*!40000 ALTER TABLE `hoadonhocphi` DISABLE KEYS */;
INSERT INTO `hoadonhocphi` VALUES (3,'SV001',1,15,8250000,'2025-10-30','2026-08-13 16:57:00',NULL,0.00,NULL,NULL,NULL),(4,'SV001',8,15,8250000,'2026-03-15','2026-08-21 14:06:30',NULL,0.00,NULL,NULL,NULL),(5,'SV001',9,8,4400000,'2026-08-30','2026-08-21 14:06:30',NULL,0.00,NULL,NULL,NULL),(6,'SV001',10,16,8000000,'2024-10-30','2026-08-21 14:06:30',NULL,0.00,NULL,NULL,NULL),(8,'20231552',8,30,16500000,'2026-08-30','2026-08-28 15:08:35',NULL,0.00,NULL,NULL,NULL),(9,'20231552',13,15,15000000,'2029-08-29','2026-08-29 00:10:08',NULL,0.00,NULL,NULL,NULL),(10,'sv001',13,15,15000000,'2026-08-29','2026-08-29 00:10:35',NULL,0.00,NULL,NULL,NULL),(11,'sv001',13,20,20000000,'2026-09-09','2026-08-29 21:09:14',NULL,0.00,NULL,NULL,NULL),(12,'20231552',15,30,10500000,'2026-08-24','2026-09-01 14:18:22','2026-09-02',25.00,'học bổng',NULL,NULL);
/*!40000 ALTER TABLE `hoadonhocphi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hocky`
--

DROP TABLE IF EXISTS `hocky`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hocky` (
  `MaHocKy` int NOT NULL AUTO_INCREMENT,
  `TenHocKy` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NamHoc` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DonGiaTinChi` decimal(12,0) NOT NULL DEFAULT '0',
  `NgayBatDau` date DEFAULT NULL,
  `NgayKetThuc` date DEFAULT NULL,
  PRIMARY KEY (`MaHocKy`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hocky`
--

LOCK TABLES `hocky` WRITE;
/*!40000 ALTER TABLE `hocky` DISABLE KEYS */;
INSERT INTO `hocky` VALUES (1,'Hoc ky 1','2025-2026',550000,'2025-09-01','2026-01-15'),(4,'Hoc ky 1','2025-2026',550000,'2025-09-01','2026-01-15'),(5,'Hoc ky 1','2025-2026',550000,'2025-09-01','2026-01-15'),(6,'Hoc ky 1','2025-2026',550000,'2025-09-01','2026-01-15'),(7,'Hoc ky 1','2025-2026',550000,'2025-09-01','2026-01-15'),(8,'Hoc ky 2','2025-2026',550000,'2026-01-16','2026-05-30'),(9,'Hoc ky He','2025-2026',550000,'2026-06-01','2026-08-15'),(10,'Hoc ky 1','2024-2025',500000,'2024-09-01','2025-01-15'),(12,'Học kỳ 2','2026-2027',50000,'2027-01-09','2027-05-25'),(13,'Hoc ky He 2026','2026-2027',1000000,'2027-02-09','2027-09-30'),(14,'Hoc ky hoc lai 2026','2025-2026',850000,'2026-09-09','2026-10-10'),(15,'Hoc ky He 2026','2026-2027',350000,'2026-09-01','2026-09-29');
/*!40000 ALTER TABLE `hocky` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lichsunapvi`
--

DROP TABLE IF EXISTS `lichsunapvi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichsunapvi`
--

LOCK TABLES `lichsunapvi` WRITE;
/*!40000 ALTER TABLE `lichsunapvi` DISABLE KEYS */;
INSERT INTO `lichsunapvi` VALUES (1,'20231552',283980000,'2026-08-28 14:30:34','VNPAY','MOCK-BEF78EB1'),(2,'20231552',3000000,'2026-08-28 15:12:55','VNPAY','15669852'),(3,'20231552',40000000,'2026-08-28 15:13:50','VNPAY','15669855');
/*!40000 ALTER TABLE `lichsunapvi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lichthutudong`
--

DROP TABLE IF EXISTS `lichthutudong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichthutudong`
--

LOCK TABLES `lichthutudong` WRITE;
/*!40000 ALTER TABLE `lichthutudong` DISABLE KEYS */;
INSERT INTO `lichthutudong` VALUES (2,10,'2026-08-29','2026-08-29','DA_THU','2026-08-29 00:18:24','ngoquoctuan','2026-08-29 16:33:22'),(3,9,'2026-08-29','2026-08-29','DA_THU','2026-08-29 00:22:03','ngoquoctuan','2026-08-29 00:24:14');
/*!40000 ALTER TABLE `lichthutudong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhatkyhethong`
--

DROP TABLE IF EXISTS `nhatkyhethong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhatkyhethong`
--

LOCK TABLES `nhatkyhethong` WRITE;
/*!40000 ALTER TABLE `nhatkyhethong` DISABLE KEYS */;
INSERT INTO `nhatkyhethong` VALUES (1,NULL,'he_thong','DANG_NHAP','TaiKhoan','Đăng nhập thành công: ngoquoctuan','2026-08-28 10:13:15'),(2,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-28 03:35:08'),(3,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 03:35:24'),(4,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-28 04:23:19'),(5,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 04:23:41'),(6,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 04:27:25'),(7,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 06:45:06'),(8,8,'kt001','DANG_NHAP','KETOAN','Sinh viên đăng nhập: kt001 (NGÔ QUỐC TUẤN)','2026-08-28 07:26:15'),(9,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 07:26:27'),(10,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 07:26:36'),(11,3,'ngoquoctuan','TAO_LICH_THU_TU_DONG','HoaDon #7','Dang ky quet tu dong tu 2026-08-28 den 2026-08-29','2026-08-28 07:28:00'),(12,3,'ngoquoctuan','NAP_VI','ViDienTu 20231552','Nap 283980000 qua VNPAY vao vi hoc phi','2026-08-28 07:30:34'),(13,NULL,'he_thong','TU_DONG_THU_HOC_PHI','HoaDon #7','Da tu dong tru 282980000 tu Vi hoc phi cua SV 20231552','2026-08-28 07:30:47'),(14,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 07:40:51'),(15,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 07:41:03'),(16,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 07:45:13'),(17,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 08:04:52'),(18,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 08:09:11'),(19,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 08:09:20'),(20,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 08:09:37'),(21,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 08:10:02'),(22,7,'sv002','NAP_VI','ViDienTu 20231552','Nap 3000000 qua VNPAY vao vi hoc phi','2026-08-28 08:12:56'),(23,7,'sv002','NAP_VI','ViDienTu 20231552','Nap 40000000 qua VNPAY vao vi hoc phi','2026-08-28 08:13:50'),(24,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 09:00:05'),(25,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 17:05:47'),(26,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 17:11:20'),(27,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-28 17:12:19'),(28,3,'ngoquoctuan','TAO_LICH_THU_TU_DONG','HoaDon #10','Dang ky quet tu dong tu 2026-08-29 den 2026-08-29','2026-08-28 17:18:24'),(29,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-28 17:19:11'),(30,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-28 17:20:51'),(31,7,'sv002','TAO_LICH_THU_TU_DONG','HoaDon #9','Dang ky quet tu dong tu 2026-08-29 den 2026-08-29','2026-08-28 17:22:03'),(32,NULL,'he_thong','TU_DONG_THU_HOC_PHI','HoaDon #9','Da tu dong tru 15000000 tu Vi hoc phi cua SV 20231552','2026-08-28 17:24:14'),(33,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 08:43:24'),(34,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 08:44:40'),(35,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 08:45:09'),(36,8,'kt001','DANG_NHAP','KETOAN','Sinh viên đăng nhập: kt001 (NGÔ QUỐC TUẤN)','2026-08-29 08:45:31'),(37,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 08:49:45'),(38,8,'kt001','DANG_NHAP','KETOAN','Sinh viên đăng nhập: kt001 (NGÔ QUỐC TUẤN)','2026-08-29 08:50:18'),(39,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 08:50:51'),(40,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 08:54:17'),(41,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-08-29 09:01:54'),(42,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 09:05:16'),(43,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 09:21:06'),(44,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 09:22:53'),(45,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 09:33:35'),(46,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 09:33:46'),(47,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 14:06:06'),(48,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 14:06:24'),(49,8,'kt001','DANG_NHAP','KETOAN','Sinh viên đăng nhập: kt001 (NGÔ QUỐC TUẤN)','2026-08-29 14:18:32'),(50,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 14:27:16'),(51,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 14:36:27'),(52,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 14:36:57'),(53,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 14:42:42'),(54,3,'ngoquoctuan','THANH_TOAN','HoaDon #11','Doi soat ngan hang tu dong: SV sv001 - 20.000.000 d (do tin cay 100%)','2026-08-29 14:47:40'),(55,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 14:50:42'),(56,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 14:55:22'),(57,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 14:57:42'),(58,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 15:15:12'),(59,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-29 15:15:59'),(60,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 15:16:16'),(61,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 15:33:02'),(62,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-29 15:47:15'),(63,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 03:35:28'),(64,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 08:55:00'),(65,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 09:07:38'),(66,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 09:21:27'),(67,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 09:44:42'),(68,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 09:44:59'),(69,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 10:01:05'),(70,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 10:16:19'),(71,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 10:31:40'),(72,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 10:32:19'),(73,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 10:34:42'),(74,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 14:49:09'),(75,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 14:52:21'),(76,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 15:07:04'),(77,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 15:16:40'),(78,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 15:26:09'),(79,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 15:59:10'),(80,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 16:09:35'),(81,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-30 16:16:00'),(82,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-30 16:16:29'),(83,8,'kt001','DANG_NHAP','KETOAN','Sinh viên đăng nhập: kt001 (NGÔ QUỐC TUẤN)','2026-08-30 16:19:25'),(84,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-30 16:23:15'),(85,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-30 16:27:36'),(86,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-31 08:16:59'),(87,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-08-31 08:20:10'),(88,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-31 08:23:04'),(89,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-31 15:15:10'),(90,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-08-31 15:18:36'),(91,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 07:13:25'),(92,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-01 07:30:19'),(93,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 07:30:30'),(94,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-01 07:33:34'),(95,NULL,'he_thong','TU_DONG_NHAC_NO_PHU_HUYNH','HoaDon #12','Tu dong gui SMS nhac no cho PHU HUYNH cua SV 20231552 (qua han 8 ngay)','2026-09-01 07:39:16'),(96,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-01 07:39:54'),(97,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 07:40:05'),(98,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 07:45:20'),(99,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 07:47:08'),(100,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 07:52:31'),(101,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 08:28:04'),(102,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 16:43:22'),(103,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 16:47:56'),(104,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-01 17:09:49'),(105,NULL,'he_thong','TU_DONG_NHAC_NO_PHU_HUYNH','HoaDon #12','Tu dong gui SMS nhac no cho PHU HUYNH cua SV 20231552 (qua han 9 ngay)','2026-09-02 03:10:20'),(106,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-02 03:10:49'),(107,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-02 06:23:37'),(108,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 06:23:49'),(109,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 06:27:36'),(110,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-02 06:28:28'),(111,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 06:48:41'),(112,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 08:55:39'),(113,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 09:00:04'),(114,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 09:12:28'),(115,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-02 09:13:20'),(116,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 09:13:31'),(117,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 09:16:54'),(118,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-02 15:45:14'),(119,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-02 15:56:41'),(120,7,'sv002','THANH_TOAN_BANG_VI','HoaDon #12','SV 20231552 tu thanh toan 7875000 bang Vi hoc phi dien tu','2026-09-02 15:58:12'),(121,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-03 02:02:05'),(122,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-03 02:30:14'),(123,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-03 13:09:09'),(124,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-03 13:38:47'),(125,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-03 15:28:45'),(126,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-09-03 15:29:25'),(127,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-03 15:30:17'),(128,7,'sv002','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv002 (Ngô Quốc Tuấn)','2026-09-04 03:23:56'),(129,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-04 03:24:10'),(130,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-04 03:42:48'),(131,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-04 05:54:43'),(132,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-04 09:07:53'),(133,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-08 08:01:42'),(134,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-08 12:52:00'),(135,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-08 15:56:30'),(136,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-09-08 16:21:10'),(137,3,'ngoquoctuan','DANG_NHAP','ADMIN','Đăng nhập thành công: ngoquoctuan (Quan tri vien)','2026-09-08 16:21:21'),(138,6,'sv001','DANG_NHAP','SINHVIEN','Sinh viên đăng nhập: sv001 (Nguyễn Thu Huyền)','2026-09-08 16:37:06');
/*!40000 ALTER TABLE `nhatkyhethong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuthu`
--

DROP TABLE IF EXISTS `phieuthu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuthu`
--

LOCK TABLES `phieuthu` WRITE;
/*!40000 ALTER TABLE `phieuthu` DISABLE KEYS */;
INSERT INTO `phieuthu` VALUES (2,3,3000000,'2026-08-13 16:57:00','TIEN_MAT',NULL,'Ke toan A','THANH_CONG'),(3,4,5000000,'2026-08-21 14:06:30','CHUYEN_KHOAN',NULL,'Ke toan B','THANH_CONG'),(4,5,4400000,'2026-08-21 14:06:30','THANH_TOAN_ONLINE','ONL-20260801-001','Cong thanh toan online','THANH_CONG'),(5,6,8000000,'2026-08-21 14:06:30','TIEN_MAT',NULL,'Ke toan A','THANH_CONG'),(6,4,3250000,'2026-08-22 17:13:45','THANH_TOAN_ONLINE','15665017','He thong (VNPAY)','THANH_CONG'),(7,3,5250000,'2026-08-22 17:14:34','THANH_TOAN_ONLINE','15665020','He thong (VNPAY)','THANH_CONG'),(14,9,15000000,'2026-08-29 00:24:14','VI_DIEN_TU',NULL,'He thong (tu dong tru vi)','THANH_CONG'),(15,8,16500000,'2026-08-29 16:04:43','THANH_TOAN_ONLINE','15670240','He thong (VNPAY)','THANH_CONG'),(16,10,15000000,'2026-08-29 16:22:18','THANH_TOAN_ONLINE','4805413183','He thong (MOMO)','THANH_CONG'),(17,11,20000000,'2026-08-29 21:47:39','CHUYEN_KHOAN','FT26241000100','Quan tri vien (doi soat tu dong)','THANH_CONG'),(18,12,7875000,'2026-09-02 22:58:11','VI_DIEN_TU',NULL,'Sinh vien 20231552 (tu vi hoc phi)','THANH_CONG');
/*!40000 ALTER TABLE `phieuthu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sinhvien`
--

DROP TABLE IF EXISTS `sinhvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sinhvien`
--

LOCK TABLES `sinhvien` WRITE;
/*!40000 ALTER TABLE `sinhvien` DISABLE KEYS */;
INSERT INTO `sinhvien` VALUES ('09093003','NGUYỄN THU  HUYỀN','dl14.5','du lịch','2003-09-09','ngoquoctuanabc29@gmail.com','0962817574',1,NULL,'0962817574','hà  nội','hà nội'),('20230101','Nguyen Van An','CNTT14.1','Cong nghe thong tin','2005-03-12','20230101@eaut.edu.vn','0901000101',1,NULL,'0962817574',NULL,NULL),('20230102','Tran Thi Bich','CNTT14.1','Cong nghe thong tin','2005-07-21','20230102@eaut.edu.vn','0901000102',1,NULL,'0962817574',NULL,NULL),('20230103','Le Hoang Cuong','CNTT14.1','Cong nghe thong tin','2005-01-05','20230103@eaut.edu.vn','0901000103',1,NULL,'0962817574',NULL,NULL),('20230104','Pham Thi Dung','CNTT14.1','Cong nghe thong tin','2004-11-30','20230104@eaut.edu.vn','0901000104',1,NULL,'0962817574',NULL,NULL),('20230105','Hoang Van Duc','CNTT14.2','Cong nghe thong tin','2005-05-18','20230105@eaut.edu.vn','0901000105',1,NULL,'0962817574',NULL,NULL),('20230106','Vu Thi Giang','CNTT14.2','Cong nghe thong tin','2005-09-09','20230106@eaut.edu.vn','0901000106',1,NULL,'0962817574',NULL,NULL),('20230107','Dang Van Hai','CNTT14.2','Cong nghe thong tin','2004-12-14','20230107@eaut.edu.vn','0901000107',1,NULL,'0962817574',NULL,NULL),('20230108','Bui Thi Hoa','CNTT14.2','Cong nghe thong tin','2005-02-27','20230108@eaut.edu.vn','0901000108',1,NULL,'0962817574',NULL,NULL),('20230201','Ngo Quoc Khanh','KT15.1','Kinh te','2005-04-16','20230201@eaut.edu.vn','0901000201',1,NULL,'0962817574',NULL,NULL),('20230202','Do Thi Lan','KT15.1','Kinh te','2005-08-08','20230202@eaut.edu.vn','0901000202',1,NULL,'0962817574',NULL,NULL),('20230203','Nguyen Van Minh','KT15.1','Kinh te','2004-10-22','20230203@eaut.edu.vn','0901000203',1,NULL,'0962817574',NULL,NULL),('20230204','Tran Thi Ngoc','KT15.1','Kinh te','2005-06-03','20230204@eaut.edu.vn','0901000204',1,NULL,'0962817574',NULL,NULL),('20230205','Le Van Phuc','KT15.2','Kinh te','2005-01-19','20230205@eaut.edu.vn','0901000205',1,NULL,'0962817574',NULL,NULL),('20230206','Pham Thi Quyen','KT15.2','Kinh te','2004-09-25','20230206@eaut.edu.vn','0901000206',1,NULL,'0962817574',NULL,NULL),('20230207','Hoang Van Son','KT15.2','Kinh te','2005-03-30','20230207@eaut.edu.vn','0901000207',1,NULL,'0962817574',NULL,NULL),('20230208','Vu Thi Thao','KT15.2','Kinh te','2005-11-11','20230208@eaut.edu.vn','0901000208',1,NULL,'0962817574',NULL,NULL),('20230301','Dang Van Thanh','CK12.1','Co khi','2004-05-05','20230301@eaut.edu.vn','0901000301',1,NULL,'0962817574',NULL,NULL),('20230302','Bui Thi Thu','CK12.1','Co khi','2005-07-17','20230302@eaut.edu.vn','0901000302',1,NULL,'0962817574',NULL,NULL),('20230303','Ngo Van Tien','CK12.1','Co khi','2005-02-14','20230303@eaut.edu.vn','0901000303',1,NULL,'0962817574',NULL,NULL),('20230304','Do Thi Trang','CK12.1','Co khi','2004-08-29','20230304@eaut.edu.vn','0901000304',1,NULL,'0962817574',NULL,NULL),('20230305','Nguyen Van Tuan','CK12.2','Co khi','2005-04-06','20230305@eaut.edu.vn','0901000305',1,NULL,'0962817574',NULL,NULL),('20230306','Tran Thi Uyen','CK12.2','Co khi','2005-10-10','20230306@eaut.edu.vn','0901000306',1,NULL,'0962817574',NULL,NULL),('20230307','Le Van Viet','CK12.2','Co khi','2004-12-01','20230307@eaut.edu.vn','0901000307',1,NULL,'0962817574',NULL,NULL),('20230308','Pham Thi Xuan','CK12.2','Co khi','2005-06-23','20230308@eaut.edu.vn','0901000308',1,NULL,'0962817574',NULL,NULL),('20230401','Hoang Van Anh','DDT13.1','Dien - Dien tu','2005-01-08','20230401@eaut.edu.vn','0901000401',1,NULL,'0962817574',NULL,NULL),('20230402','Vu Thi Bao','DDT13.1','Dien - Dien tu','2004-11-15','20230402@eaut.edu.vn','0901000402',1,NULL,'0962817574',NULL,NULL),('20230403','Dang Van Chinh','DDT13.1','Dien - Dien tu','2005-09-19','20230403@eaut.edu.vn','0901000403',1,NULL,'0962817574',NULL,NULL),('20230404','Bui Thi Diem','DDT13.1','Dien - Dien tu','2005-03-27','20230404@eaut.edu.vn','0901000404',1,NULL,'0962817574',NULL,NULL),('20230405','Ngo Van Duong','DDT13.2','Dien - Dien tu','2004-07-02','20230405@eaut.edu.vn','0901000405',1,NULL,'0962817574',NULL,NULL),('20230406','Do Thi Hanh','DDT13.2','Dien - Dien tu','2005-05-13','20230406@eaut.edu.vn','0901000406',1,NULL,'0962817574',NULL,NULL),('20230407','Nguyen Van Hieu','DDT13.2','Dien - Dien tu','2005-02-20','20230407@eaut.edu.vn','0901000407',1,NULL,'0962817574',NULL,NULL),('20230408','Tran Thi Huong','DDT13.2','Dien - Dien tu','2004-10-04','20230408@eaut.edu.vn','0901000408',1,NULL,'0962817574',NULL,NULL),('20230501','Le Van Khoa','NN16.1','Ngoai ngu','2005-08-16','20230501@eaut.edu.vn','0901000501',1,NULL,'0962817574',NULL,NULL),('20230502','Pham Thi Lien','NN16.1','Ngoai ngu','2005-04-24','20230502@eaut.edu.vn','0901000502',1,NULL,'0962817574',NULL,NULL),('20230503','Hoang Van Long','NN16.1','Ngoai ngu','2004-12-09','20230503@eaut.edu.vn','0901000503',1,NULL,'0962817574',NULL,NULL),('20230504','Vu Thi Mai','NN16.1','Ngoai ngu','2005-06-30','20230504@eaut.edu.vn','0901000504',1,NULL,'0962817574',NULL,NULL),('20230505','Dang Van Nam','NN16.2','Ngoai ngu','2005-01-27','20230505@eaut.edu.vn','0901000505',1,NULL,'0962817574',NULL,NULL),('20230506','Bui Thi Oanh','NN16.2','Ngoai ngu','2004-09-12','20230506@eaut.edu.vn','0901000506',1,NULL,'0962817574',NULL,NULL),('20230507','Ngo Van Phong','NN16.2','Ngoai ngu','2005-03-05','20230507@eaut.edu.vn','0901000507',1,NULL,'0962817574',NULL,NULL),('20230508','Do Thi Que','NN16.2','Ngoai ngu','2005-11-28','20230508@eaut.edu.vn','0901000508',1,NULL,'0962817574',NULL,NULL),('20230601','Nguyen Van Sang','XD11.1','Xay dung','2004-06-19','20230601@eaut.edu.vn','0901000601',1,NULL,'0962817574',NULL,NULL),('20230602','Tran Thi Tam','XD11.1','Xay dung','2005-02-08','20230602@eaut.edu.vn','0901000602',1,NULL,'0962817574',NULL,NULL),('20230603','Le Van Thang','XD11.1','Xay dung','2005-10-17','20230603@eaut.edu.vn','0901000603',1,NULL,'0962817574',NULL,NULL),('20230604','Pham Thi Thuy','XD11.1','Xay dung','2004-08-03','20230604@eaut.edu.vn','0901000604',1,NULL,'0962817574',NULL,NULL),('20230605','Hoang Van Tung','XD11.2','Xay dung','2005-05-26','20230605@eaut.edu.vn','0901000605',1,NULL,'0962817574',NULL,NULL),('20230606','Vu Thi Van','XD11.2','Xay dung','2005-01-14','20230606@eaut.edu.vn','0901000606',1,NULL,'0962817574',NULL,NULL),('20230607','Dang Van Vinh','XD11.2','Xay dung','2004-07-31','20230607@eaut.edu.vn','0901000607',1,NULL,'0962817574',NULL,NULL),('20230608','Bui Thi Yen','XD11.2','Xay dung','2005-09-22','20230608@eaut.edu.vn','0901000608',1,NULL,'0962817574',NULL,NULL),('20231475','Nguyễn hữu  cát','Dcntt 14.4','Công  nghệ  thông  tin',NULL,'20231475@eaut.edu.vn','',1,NULL,'0962817574',NULL,NULL),('20231552','ngô quốc tuấn','dccntt 14.4','Công  nghệ  thông  tin',NULL,'ngoquoctuanabc29@gmail.com','0123456789',1,NULL,'0962817574',NULL,NULL),('20231553','Ngô Quốc tuấn','dccntt 14.4','Công  nghệ  thông  tin','2005-03-07','ngoquoctuanabc29@gmail.com','0962817574',1,NULL,'0962817574','hà  nội','Xuân Mai  Hà  Nội'),('SV001','Nguyễn Thu Huyền','CNTT01','Cong nghe thong tin',NULL,'tngoquoc@51gmail.con','0962817574',1,'avatars/SV001.png','0962817574',NULL,NULL),('SV002','Tran Thi B','CNTT01','Cong nghe thong tin',NULL,'b@example.com',NULL,1,NULL,'0962817574',NULL,NULL),('TEST999','Nguyen Van Test','dcoto14.4','o to','2004-01-01','ngoquoctuanabc29@gmail.com','0962817574',1,NULL,'0962817574','hà  nội','Ha Noi');
/*!40000 ALTER TABLE `sinhvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taikhoan`
--

DROP TABLE IF EXISTS `taikhoan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taikhoan`
--

LOCK TABLES `taikhoan` WRITE;
/*!40000 ALTER TABLE `taikhoan` DISABLE KEYS */;
INSERT INTO `taikhoan` VALUES (2,'20231552@eaut.edu.vn','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','ngô quốc tuấn','SINHVIEN','20231552',NULL,1,0,'2026-08-12 10:28:08'),(3,'ngoquoctuan','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Quan tri vien','ADMIN',NULL,NULL,1,0,'2026-08-13 09:23:49'),(6,'sv001','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Nguyễn Thu Huyền','SINHVIEN','SV001','ngoquoctuanabc29@gmail.com',1,0,'2026-08-13 16:50:06'),(7,'sv002','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Ngô Quốc Tuấn','SINHVIEN','20231552','tngoquoc51@gmail.com',1,0,'2026-08-23 12:36:26'),(8,'kt001','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','NGÔ QUỐC TUẤN','KETOAN',NULL,'ngoquoctuanabc29@gmail.com',1,0,'2026-08-25 12:39:33');
/*!40000 ALTER TABLE `taikhoan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `theqrdangnhap`
--

DROP TABLE IF EXISTS `theqrdangnhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `theqrdangnhap`
--

LOCK TABLES `theqrdangnhap` WRITE;
/*!40000 ALTER TABLE `theqrdangnhap` DISABLE KEYS */;
/*!40000 ALTER TABLE `theqrdangnhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thongbao`
--

DROP TABLE IF EXISTS `thongbao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thongbao` (
  `MaThongBao` int NOT NULL AUTO_INCREMENT,
  `ManHinhKey` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `VaiTroNhan` enum('ADMIN','KETOAN','SINHVIEN','ALL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `MaSVNhan` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TieuDe` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NoiDung` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ThoiGianTao` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MaThongBao`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thongbao`
--

LOCK TABLES `thongbao` WRITE;
/*!40000 ALTER TABLE `thongbao` DISABLE KEYS */;
INSERT INTO `thongbao` VALUES (1,'sinhvien','ALL',NULL,'Sinh viên mới','Đã thêm sinh viên 20231999 - Nguyễn Văn Test','2026-08-30 17:31:11'),(2,'congno','ALL',NULL,'Cảnh báo nợ #1','Test','2026-08-30 17:31:11'),(3,'congno','ALL',NULL,'Cảnh báo nợ #2','Test','2026-08-30 17:31:11'),(4,'congno','ALL',NULL,'Cảnh báo nợ #3','Test','2026-08-30 17:31:11'),(5,'congno','ALL',NULL,'Cảnh báo nợ #4','Test','2026-08-30 17:31:11'),(6,'congno','ALL',NULL,'Cảnh báo nợ #5','Test','2026-08-30 17:31:11'),(7,'congno','ALL',NULL,'Cảnh báo nợ #6','Test','2026-08-30 17:31:11'),(8,'congno','ALL',NULL,'Cảnh báo nợ #7','Test','2026-08-30 17:31:11'),(9,'congno','ALL',NULL,'Cảnh báo nợ #8','Test','2026-08-30 17:31:11'),(10,'congno','ALL',NULL,'Cảnh báo nợ #9','Test','2026-08-30 17:31:11'),(11,'congno','ALL',NULL,'Cảnh báo nợ #10','Test','2026-08-30 17:31:11'),(12,'congno','ALL',NULL,'Cảnh báo nợ #11','Test','2026-08-30 17:31:11'),(13,'congno','ALL',NULL,'Cảnh báo nợ #12','Test','2026-08-30 17:31:11'),(17,'hoadon_sv','SINHVIEN','20231552','Hóa đơn mới','Test hóa đơn cho SV 20231552','2026-08-30 17:32:01'),(18,'sinhvien','ALL',NULL,'Sinh viên mới','Đã thêm sinh viên 20231999 - Nguyễn Văn Test','2026-08-30 17:33:54'),(19,'hocky','ALL',NULL,'Học kỳ mới','Đã tạo học kỳ 2 năm học 2026-2027','2026-08-30 17:33:54'),(20,'hocphi','ALL',NULL,'Hóa đơn mới','Đã sinh 15 hóa đơn học phí cho học kỳ 1','2026-08-30 17:33:54'),(21,'thanhtoan','ALL',NULL,'Thanh toán mới','Sinh viên 20231475 vừa nộp 8.250.000đ','2026-08-30 17:33:54'),(22,'doisoat','ALL',NULL,'Cần xác nhận','Có 2 giao dịch chuyển khoản đang chờ đối soát thủ công','2026-08-30 17:33:54'),(23,'lichthutudong','ALL',NULL,'Lịch thu chạy xong','Đã tự động thu thành công 3 hóa đơn theo lịch','2026-08-30 17:33:54'),(24,'baocao','ALL',NULL,'Báo cáo mới','Báo cáo tháng 8/2026 đã sẵn sàng xem','2026-08-30 17:33:54'),(25,'chatbot','ALL',NULL,'Chatbot có câu hỏi mới','Có 1 câu hỏi từ sinh viên chưa được trả lời','2026-08-30 17:33:54'),(26,'calamviec','KETOAN',NULL,'Ca làm việc','Nhắc nhở: chưa mở ca làm việc hôm nay','2026-08-30 17:33:54'),(27,'taikhoan','ADMIN',NULL,'Tài khoản mới','Vừa tạo tài khoản kế toán mới: ketoan02','2026-08-30 17:33:54'),(28,'backup','ADMIN',NULL,'Nhắc sao lưu','Đã 7 ngày chưa sao lưu dữ liệu, nên sao lưu ngay','2026-08-30 17:33:54'),(29,'nhatky','ADMIN',NULL,'Hoạt động bất thường','Có 5 lượt đăng nhập thất bại liên tiếp vào tài khoản admin','2026-08-30 17:33:54'),(30,'congno','ALL',NULL,'Cảnh báo nợ #1','Test hien thi 9+','2026-08-30 17:33:54'),(31,'congno','ALL',NULL,'Cảnh báo nợ #2','Test hien thi 9+','2026-08-30 17:33:54'),(32,'congno','ALL',NULL,'Cảnh báo nợ #3','Test hien thi 9+','2026-08-30 17:33:54'),(33,'congno','ALL',NULL,'Cảnh báo nợ #4','Test hien thi 9+','2026-08-30 17:33:54'),(34,'congno','ALL',NULL,'Cảnh báo nợ #5','Test hien thi 9+','2026-08-30 17:33:54'),(35,'congno','ALL',NULL,'Cảnh báo nợ #6','Test hien thi 9+','2026-08-30 17:33:54'),(36,'congno','ALL',NULL,'Cảnh báo nợ #7','Test hien thi 9+','2026-08-30 17:33:54'),(37,'congno','ALL',NULL,'Cảnh báo nợ #8','Test hien thi 9+','2026-08-30 17:33:54'),(38,'congno','ALL',NULL,'Cảnh báo nợ #9','Test hien thi 9+','2026-08-30 17:33:54'),(39,'congno','ALL',NULL,'Cảnh báo nợ #10','Test hien thi 9+','2026-08-30 17:33:54'),(40,'congno','ALL',NULL,'Cảnh báo nợ #11','Test hien thi 9+','2026-08-30 17:33:54'),(45,'tongquan','SINHVIEN','20231552','Cảnh báo công nợ','Bạn đang có hóa đơn quá hạn thanh toán','2026-08-30 17:33:54'),(46,'hoadon_sv','SINHVIEN','20231552','Hóa đơn mới','Bạn có hóa đơn học kỳ 1 mới trị giá 8.250.000đ','2026-08-30 17:33:54'),(47,'lichsu_sv','SINHVIEN','20231552','Thanh toán thành công','Đã ghi nhận khoản nộp 5.000.000đ của bạn','2026-08-30 17:33:54'),(48,'vidientu','SINHVIEN','20231552','Ví học phí','Số dư ví của bạn vừa được cộng thêm 2.000.000đ','2026-08-30 17:33:54'),(49,'thongtin','SINHVIEN','20231552','Cập nhật thông tin','Email liên hệ của bạn vừa được Admin cập nhật','2026-08-30 17:33:54'),(50,'chatbot','SINHVIEN','20231552','Chatbot đã trả lời','Câu hỏi của bạn về học phí đã có phản hồi','2026-08-30 17:33:54');
/*!40000 ALTER TABLE `thongbao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thongbaodadoc`
--

DROP TABLE IF EXISTS `thongbaodadoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thongbaodadoc` (
  `MaThongBao` int NOT NULL,
  `MaTK` int NOT NULL,
  `ThoiGianDoc` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MaThongBao`,`MaTK`),
  KEY `MaTK` (`MaTK`),
  CONSTRAINT `thongbaodadoc_ibfk_1` FOREIGN KEY (`MaThongBao`) REFERENCES `thongbao` (`MaThongBao`) ON DELETE CASCADE,
  CONSTRAINT `thongbaodadoc_ibfk_2` FOREIGN KEY (`MaTK`) REFERENCES `taikhoan` (`MaTK`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thongbaodadoc`
--

LOCK TABLES `thongbaodadoc` WRITE;
/*!40000 ALTER TABLE `thongbaodadoc` DISABLE KEYS */;
INSERT INTO `thongbaodadoc` VALUES (1,3,'2026-08-30 17:31:43'),(1,8,'2026-08-30 23:19:26'),(2,3,'2026-08-30 17:31:42'),(2,8,'2026-08-30 23:19:30'),(3,3,'2026-08-30 17:31:42'),(3,8,'2026-08-30 23:19:30'),(4,3,'2026-08-30 17:31:42'),(4,8,'2026-08-30 23:19:30'),(5,3,'2026-08-30 17:31:42'),(5,8,'2026-08-30 23:19:30'),(6,3,'2026-08-30 17:31:42'),(6,8,'2026-08-30 23:19:30'),(7,3,'2026-08-30 17:31:42'),(7,8,'2026-08-30 23:19:30'),(8,3,'2026-08-30 17:31:42'),(8,8,'2026-08-30 23:19:30'),(9,3,'2026-08-30 17:31:42'),(9,8,'2026-08-30 23:19:30'),(10,3,'2026-08-30 17:31:42'),(10,8,'2026-08-30 23:19:30'),(11,3,'2026-08-30 17:31:42'),(11,8,'2026-08-30 23:19:30'),(12,3,'2026-08-30 17:31:42'),(12,8,'2026-08-30 23:19:30'),(13,3,'2026-08-30 17:31:42'),(13,8,'2026-08-30 23:19:30'),(17,7,'2026-09-01 14:30:22'),(18,3,'2026-08-30 21:49:12'),(18,8,'2026-08-30 23:19:26'),(19,3,'2026-08-30 21:49:13'),(19,8,'2026-08-30 23:19:27'),(20,3,'2026-08-30 21:49:14'),(20,8,'2026-08-30 23:19:27'),(21,3,'2026-08-30 21:49:14'),(21,8,'2026-08-30 23:19:29'),(22,3,'2026-08-30 21:49:15'),(22,8,'2026-08-30 23:19:28'),(23,3,'2026-08-30 21:49:16'),(23,8,'2026-08-30 23:19:29'),(24,3,'2026-08-30 21:49:19'),(24,8,'2026-08-30 23:19:31'),(25,3,'2026-08-30 21:49:17'),(25,6,'2026-08-30 23:16:31'),(25,7,'2026-09-04 10:23:58'),(25,8,'2026-08-30 23:19:33'),(26,8,'2026-08-30 23:19:26'),(27,3,'2026-08-30 21:49:18'),(28,3,'2026-08-30 21:49:20'),(29,3,'2026-08-30 21:49:21'),(30,3,'2026-08-30 17:35:13'),(30,8,'2026-08-30 23:19:30'),(31,3,'2026-08-30 17:35:13'),(31,8,'2026-08-30 23:19:30'),(32,3,'2026-08-30 17:35:13'),(32,8,'2026-08-30 23:19:30'),(33,3,'2026-08-30 17:35:13'),(33,8,'2026-08-30 23:19:30'),(34,3,'2026-08-30 17:35:13'),(34,8,'2026-08-30 23:19:30'),(35,3,'2026-08-30 17:35:13'),(35,8,'2026-08-30 23:19:30'),(36,3,'2026-08-30 17:35:13'),(36,8,'2026-08-30 23:19:30'),(37,3,'2026-08-30 17:35:13'),(37,8,'2026-08-30 23:19:30'),(38,3,'2026-08-30 17:35:13'),(38,8,'2026-08-30 23:19:30'),(39,3,'2026-08-30 17:35:13'),(39,8,'2026-08-30 23:19:30'),(40,3,'2026-08-30 17:35:13'),(40,8,'2026-08-30 23:19:30'),(45,7,'2026-09-01 14:30:18'),(46,7,'2026-09-01 14:30:22'),(47,7,'2026-09-02 13:28:29'),(48,7,'2026-09-02 13:55:57'),(49,7,'2026-09-04 10:23:57'),(50,7,'2026-09-04 10:23:58');
/*!40000 ALTER TABLE `thongbaodadoc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vidientu`
--

DROP TABLE IF EXISTS `vidientu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vidientu` (
  `MaSV` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SoDu` decimal(12,0) NOT NULL DEFAULT '0',
  `NgayCapNhat` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MaSV`),
  CONSTRAINT `vidientu_ibfk_1` FOREIGN KEY (`MaSV`) REFERENCES `sinhvien` (`MaSV`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vidientu`
--

LOCK TABLES `vidientu` WRITE;
/*!40000 ALTER TABLE `vidientu` DISABLE KEYS */;
INSERT INTO `vidientu` VALUES ('20231552',21125000,'2026-09-02 22:58:11');
/*!40000 ALTER TABLE `vidientu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'qlhocphi'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-09  0:02:41
