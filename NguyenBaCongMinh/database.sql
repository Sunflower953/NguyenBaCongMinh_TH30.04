-- =====================================================
--  CSDL: QUẢN LÝ CỬA HÀNG BÁN QUẦN ÁO (MySQL)
-- =====================================================
CREATE DATABASE IF NOT EXISTS quanlycuahang
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlycuahang;

DROP TABLE IF EXISTS HoaDon;
DROP TABLE IF EXISTS SanPham;
DROP TABLE IF EXISTS KhachHang;
DROP TABLE IF EXISTS TaiKhoan;

CREATE TABLE TaiKhoan (
    username VARCHAR(50)  PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    hoTen    VARCHAR(100)
);

CREATE TABLE SanPham (
    maSP    VARCHAR(10)  PRIMARY KEY,
    tenSP   VARCHAR(100) NOT NULL,
    loai    VARCHAR(50),
    size    VARCHAR(10),
    mau     VARCHAR(30),
    giaBan  DOUBLE NOT NULL,
    soLuong INT    NOT NULL DEFAULT 0
);

CREATE TABLE KhachHang (
    maKH   VARCHAR(10)  PRIMARY KEY,
    tenKH  VARCHAR(100) NOT NULL,
    sdt    VARCHAR(15),
    diaChi VARCHAR(200)
);

CREATE TABLE HoaDon (
    maHD     INT AUTO_INCREMENT PRIMARY KEY,
    maKH     VARCHAR(10) NOT NULL,
    maSP     VARCHAR(10) NOT NULL,
    soLuong  INT    NOT NULL,
    tongTien DOUBLE NOT NULL,
    ngayLap  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    FOREIGN KEY (maSP) REFERENCES SanPham(maSP)
);

-- ---------- DỮ LIỆU MẪU ----------
INSERT INTO TaiKhoan VALUES ('admin', '123456', 'Quản trị viên');

INSERT INTO SanPham VALUES
('SP001', 'Áo thun cotton basic',  'Áo thun',    'L',  'Đen',   150000, 50),
('SP002', 'Áo sơ mi Oxford',       'Áo sơ mi',   'M',  'Trắng', 320000, 30),
('SP003', 'Áo khoác bomber',       'Áo khoác',   'XL', 'Xanh navy', 550000, 20),
('SP004', 'Quần jean slimfit',     'Quần jean',  'L',  'Xanh đậm', 450000, 40),
('SP005', 'Quần short kaki',       'Quần short', 'M',  'Be',    250000, 35),
('SP006', 'Quần tây công sở',      'Quần tây',   'L',  'Xám',   400000, 25);

INSERT INTO KhachHang VALUES
('KH001', 'Nguyễn Văn Nam',  '0901234567', 'Cầu Giấy, Hà Nội'),
('KH002', 'Trần Quốc Huy',   '0912345678', 'Đống Đa, Hà Nội'),
('KH003', 'Lê Minh Đức',     '0923456789', 'Thanh Xuân, Hà Nội');
