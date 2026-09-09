package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.util.PDFExporter;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Trang "Thong tin ca nhan" danh cho cong Sinh vien.
 *
 * - Xem day du ho so: Ma SV, Ho ten, Lop, Khoa, Ngay sinh, Trang thai (chi doc,
 *   day la du lieu hoc vu do truong quan ly, sinh vien khong duoc tu sua).
 * - Rieng Email va So dien thoai duoc phep tu cap nhat (thong tin lien he ca
 *   nhan) thong qua nut "Chinh sua thong tin" -> "Luu thay doi" / "Huy".
 *
 * Thiet ke dong bo voi cac trang Sinh vien khac trong he thong: banner
 * gradient tren cung, avatar tron chu cai dau ten, 2 the noi dung (Hoc vu /
 * Lien he) canh nhau, pill trang thai mau xanh-la/do.
 */
public class ThongTinCaNhanPanel extends JPanel {
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");

    private final SinhVienService sinhVienService = new SinhVienService();
    private final TaiKhoan taiKhoan;
    private final String maSV;

    private SinhVien sinhVienHienTai;
    private boolean dangSua = false;

    private AvatarPanel avatarPanel;
    private JLabel lblTenBanner;
    private JLabel lblMaLopBanner;
    private JPanel khungNutBanner;

    private JLabel lblMaSV, lblHoTen, lblLop, lblKhoa, lblNgaySinh;
    private JLabel lblTrangThai;

    private JLabel lblEmailXem, lblSdtXem;
    private JTextField txtEmailSua, txtSdtSua;
    private JPanel khungEmailGiaTri, khungSdtGiaTri;
    private JLabel lblThongBaoLoi;

    public ThongTinCaNhanPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        this.maSV = taiKhoan.getMaSV();
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildBanner());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildHaiThe());

        JScrollPane scroll = new JScrollPane(bocNgoai(giua));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 30, this::taiDuLieu);
    }

    private JPanel bocNgoai(JPanel noiDung) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(noiDung, BorderLayout.NORTH);
        return wrap;
    }

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        banner.setPreferredSize(new Dimension(10, 104));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));

        JPanel trai = new JPanel(new BorderLayout(16, 0));
        trai.setOpaque(false);

        avatarPanel = new AvatarPanel(taiKhoan.getHoTen(), 60);
        trai.add(avatarPanel, BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel("Thông tin cá nhân");
        tieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tieuDe.setForeground(new Color(255, 255, 255, 200));
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTenBanner = new JLabel(taiKhoan.getHoTen());
        lblTenBanner.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTenBanner.setForeground(Color.WHITE);
        lblTenBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMaLopBanner = new JLabel("Đang tải...");
        lblMaLopBanner.setFont(UITheme.FONT_BASE);
        lblMaLopBanner.setForeground(new Color(255, 255, 255, 210));
        lblMaLopBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel goiYDoiAnh = new JLabel("Bấm vào ảnh để đổi ảnh đại diện");
        goiYDoiAnh.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        goiYDoiAnh.setForeground(new Color(255, 255, 255, 150));
        goiYDoiAnh.setAlignmentX(Component.LEFT_ALIGNMENT);

        chuText.add(tieuDe);
        chuText.add(lblTenBanner);
        chuText.add(lblMaLopBanner);
        chuText.add(goiYDoiAnh);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        khungNutBanner = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        khungNutBanner.setOpaque(false);
        banner.add(khungNutBanner, BorderLayout.EAST);
        capNhatNutBanner();

        return banner;
    }

    private void capNhatNutBanner() {
        khungNutBanner.removeAll();

        JButton btnXuatPDF = UITheme.secondaryButton("Tải hồ sơ PDF");
        btnXuatPDF.addActionListener(e -> xuatHoSoPDF());
        khungNutBanner.add(btnXuatPDF);

        if (!dangSua) {
            JButton btnSua = UITheme.secondaryButton("Chỉnh sửa thông tin");
            btnSua.addActionListener(e -> batCheDoSua(true));
            khungNutBanner.add(btnSua);
        } else {
            JButton btnHuy = UITheme.secondaryButton("Hủy");
            btnHuy.addActionListener(e -> batCheDoSua(false));
            JButton btnLuu = UITheme.primaryButton("Lưu thay đổi");
            btnLuu.addActionListener(e -> luuThayDoi());
            khungNutBanner.add(btnHuy);
            khungNutBanner.add(btnLuu);
        }
        khungNutBanner.revalidate();
        khungNutBanner.repaint();
    }

    private JPanel buildHaiThe() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(buildTheHocVu());
        row.add(buildTheLienHe());
        return row;
    }

    private JPanel buildTheHocVu() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel("Thông tin học vụ");
        tieuDe.setFont(UITheme.FONT_H2);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        tieuDe.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel ghiChu = new JLabel("Do phòng đào tạo quản lý - liên hệ Admin nếu cần chỉnh sửa");
        ghiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ghiChu.setForeground(UITheme.TEXT_MUTED);
        ghiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        ghiChu.setBorder(new EmptyBorder(0, 0, 14, 0));

        card.add(tieuDe);
        card.add(ghiChu);

        lblMaSV = dongChiDoc(card, "Mã sinh viên", "…");
        lblHoTen = dongChiDoc(card, "Họ và tên", "…");
        lblLop = dongChiDoc(card, "Lớp", "…");
        lblKhoa = dongChiDoc(card, "Khoa", "…");
        lblNgaySinh = dongChiDoc(card, "Ngày sinh", "…");

        JPanel dongTrangThai = new JPanel(new BorderLayout());
        dongTrangThai.setOpaque(false);
        dongTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        dongTrangThai.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel nhanTrangThai = new JLabel("Trạng thái");
        nhanTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nhanTrangThai.setForeground(UITheme.TEXT_MUTED);
        dongTrangThai.add(nhanTrangThai, BorderLayout.WEST);
        lblTrangThai = UITheme.pill("…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        JPanel wrapPill = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrapPill.setOpaque(false);
        wrapPill.add(lblTrangThai);
        dongTrangThai.add(wrapPill, BorderLayout.EAST);
        card.add(dongTrangThai);

        return card;
    }

    private JLabel dongChiDoc(JPanel card, String nhan, String giaTriBanDau) {
        JPanel dong = new JPanel(new BorderLayout());
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        dong.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(8, 0, 8, 0)));

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        dong.add(lblNhan, BorderLayout.WEST);

        JLabel lblGiaTri = new JLabel(giaTriBanDau);
        lblGiaTri.setFont(UITheme.FONT_BOLD);
        lblGiaTri.setForeground(UITheme.TEXT_PRIMARY);
        lblGiaTri.setHorizontalAlignment(SwingConstants.RIGHT);
        dong.add(lblGiaTri, BorderLayout.EAST);

        card.add(dong);
        return lblGiaTri;
    }

    private JPanel buildTheLienHe() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel("Thông tin liên hệ");
        tieuDe.setFont(UITheme.FONT_H2);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        tieuDe.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel ghiChu = new JLabel("Bạn có thể tự cập nhật Email và Số điện thoại của mình");
        ghiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ghiChu.setForeground(UITheme.TEXT_MUTED);
        ghiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        ghiChu.setBorder(new EmptyBorder(0, 0, 14, 0));

        card.add(tieuDe);
        card.add(ghiChu);

        card.add(nhanTruong("Email"));
        lblEmailXem = giaTriXem("…");
        txtEmailSua = UIUtils.textField(18);
        khungEmailGiaTri = khungGiaTriCoTheSua(lblEmailXem, txtEmailSua);
        card.add(khungEmailGiaTri);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        card.add(nhanTruong("Số điện thoại"));
        lblSdtXem = giaTriXem("…");
        txtSdtSua = UIUtils.textField(18);
        khungSdtGiaTri = khungGiaTriCoTheSua(lblSdtXem, txtSdtSua);
        card.add(khungSdtGiaTri);

        lblThongBaoLoi = new JLabel(" ");
        lblThongBaoLoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBaoLoi.setForeground(UITheme.DANGER);
        lblThongBaoLoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongBaoLoi.setBorder(new EmptyBorder(14, 0, 0, 0));
        card.add(lblThongBaoLoi);

        return card;
    }

    private JLabel nhanTruong(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JLabel giaTriXem(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JPanel khungGiaTriCoTheSua(JLabel lblXem, JTextField txtSua) {
        JPanel khung = new JPanel(new CardLayout());
        khung.setOpaque(false);
        khung.setAlignmentX(Component.LEFT_ALIGNMENT);
        khung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        khung.add(boc(lblXem), "xem");
        txtSua.setAlignmentX(Component.LEFT_ALIGNMENT);
        khung.add(txtSua, "sua");
        return khung;
    }

    private JPanel boc(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.WEST);
        return p;
    }

    private void batCheDoSua(boolean bat) {
        this.dangSua = bat;
        lblThongBaoLoi.setText(" ");

        if (bat) {
            txtEmailSua.setText(sinhVienHienTai != null && sinhVienHienTai.getEmail() != null ? sinhVienHienTai.getEmail() : "");
            txtSdtSua.setText(sinhVienHienTai != null && sinhVienHienTai.getSoDienThoai() != null ? sinhVienHienTai.getSoDienThoai() : "");
        }

        ((CardLayout) khungEmailGiaTri.getLayout()).show(khungEmailGiaTri, bat ? "sua" : "xem");
        ((CardLayout) khungSdtGiaTri.getLayout()).show(khungSdtGiaTri, bat ? "sua" : "xem");
        capNhatNutBanner();
    }

    private void luuThayDoi() {
        String email = txtEmailSua.getText().trim();
        String sdt = txtSdtSua.getText().trim();

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            lblThongBaoLoi.setText("Email không đúng định dạng (ví dụ: ten@example.com)");
            return;
        }
        if (!sdt.isEmpty() && !PHONE_PATTERN.matcher(sdt).matches()) {
            lblThongBaoLoi.setText("Số điện thoại không hợp lệ (bắt đầu bằng 0, đủ 10-11 số)");
            return;
        }
        if (sinhVienHienTai == null) {
            lblThongBaoLoi.setText("Chưa tải được dữ liệu sinh viên, vui lòng thử lại");
            return;
        }

        sinhVienHienTai.setEmail(email);
        sinhVienHienTai.setSoDienThoai(sdt);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sinhVienService.capNhat(sinhVienHienTai);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEmailXem.setText(email.isEmpty() ? "Chưa cập nhật" : email);
                    lblSdtXem.setText(sdt.isEmpty() ? "Chưa cập nhật" : sdt);
                    batCheDoSua(false);
                    UIUtils.thongBao(ThongTinCaNhanPanel.this, "Đã cập nhật thông tin liên hệ.");
                } catch (Exception ex) {
                    lblThongBaoLoi.setText("Lưu thất bại: " + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void taiDuLieu() {
        if (maSV == null || maSV.isBlank()) {
            lblMaLopBanner.setText("Chưa gán Mã sinh viên - liên hệ Admin");
            return;
        }

        SwingWorker<SinhVien, Void> worker = new SwingWorker<>() {
            @Override
            protected SinhVien doInBackground() throws Exception {
                return sinhVienService.timTheoMa(maSV);
            }

            @Override
            protected void done() {
                try {
                    SinhVien sv = get();
                    sinhVienHienTai = sv;
                    if (sv == null) {
                        lblMaLopBanner.setText("Không tìm thấy hồ sơ sinh viên");
                        return;
                    }
                    hienThiDuLieu(sv);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(ThongTinCaNhanPanel.this,
                            "Không thể tải thông tin cá nhân.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void hienThiDuLieu(SinhVien sv) {
        lblTenBanner.setText(sv.getHoTen());
        lblMaLopBanner.setText(sv.getMaSV() + "  ·  " + nullThanhChuoi(sv.getLop(), "Chưa có lớp"));

        lblMaSV.setText(sv.getMaSV());
        lblHoTen.setText(sv.getHoTen());
        lblLop.setText(nullThanhChuoi(sv.getLop(), "Chưa cập nhật"));
        lblKhoa.setText(nullThanhChuoi(sv.getKhoa(), "Chưa cập nhật"));
        lblNgaySinh.setText(sv.getNgaySinh() != null ? sv.getNgaySinh().format(DMY) : "Chưa cập nhật");

        lblTrangThai.setText(sv.isTrangThai() ? "Đang học" : "Đã nghỉ học");
        lblTrangThai.setBackground(sv.isTrangThai() ? UITheme.TINT_GREEN : UITheme.TINT_RED);
        lblTrangThai.setForeground(sv.isTrangThai() ? UITheme.TEXT_GREEN : UITheme.TEXT_RED);

        lblEmailXem.setText(nullThanhChuoi(sv.getEmail(), "Chưa cập nhật"));
        lblSdtXem.setText(nullThanhChuoi(sv.getSoDienThoai(), "Chưa cập nhật"));

        avatarPanel.capNhatTen(sv.getHoTen());
        avatarPanel.taiAnh(sv.getAnhDaiDien());
    }

    private String nullThanhChuoi(String s, String macDinh) {
        return (s == null || s.isBlank()) ? macDinh : s;
    }

    private void moChonAnh() {
        if (sinhVienHienTai == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (jpg, jpeg, png)", "jpg", "jpeg", "png"));
        int ketQua = chooser.showOpenDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;
        File fileGoc = chooser.getSelectedFile();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return luuAnhDaiDien(fileGoc);
            }

            @Override
            protected void done() {
                try {
                    String duongDanMoi = get();
                    sinhVienHienTai.setAnhDaiDien(duongDanMoi);
                    sinhVienService.capNhat(sinhVienHienTai);
                    avatarPanel.taiAnh(duongDanMoi);
                    UIUtils.thongBao(ThongTinCaNhanPanel.this, "Đã cập nhật ảnh đại diện.");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(ThongTinCaNhanPanel.this, "Đổi ảnh thất bại.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private String luuAnhDaiDien(File fileGoc) throws IOException {
        File thuMuc = new File("avatars");
        if (!thuMuc.exists()) {
            boolean daTao = thuMuc.mkdirs();
            if (!daTao && !thuMuc.exists()) {
                throw new IOException("Khong the tao thu muc avatars");
            }
        }
        String duoi = layDuoiFile(fileGoc.getName());
        File fileDich = new File(thuMuc, maSV + "." + duoi);
        Files.copy(fileGoc.toPath(), fileDich.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return "avatars/" + maSV + "." + duoi;
    }

    private String layDuoiFile(String tenFile) {
        int idx = tenFile.lastIndexOf('.');
        return idx >= 0 ? tenFile.substring(idx + 1).toLowerCase() : "png";
    }

    private void xuatHoSoPDF() {
        if (sinhVienHienTai == null) {
            UIUtils.thongBaoLoi(this, "Chưa tải được dữ liệu, vui lòng thử lại.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hồ sơ PDF");
        chooser.setSelectedFile(new File("HoSo_" + maSV + ".pdf"));
        int ketQua = chooser.showSaveDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;
        String duongDan = chooser.getSelectedFile().getAbsolutePath();
        if (!duongDan.toLowerCase().endsWith(".pdf")) duongDan += ".pdf";
        String duongDanCuoi = duongDan;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{"Ma sinh vien", sinhVienHienTai.getMaSV()});
                rows.add(new String[]{"Ho va ten", sinhVienHienTai.getHoTen()});
                rows.add(new String[]{"Lop", nullThanhChuoi(sinhVienHienTai.getLop(), "Chua cap nhat")});
                rows.add(new String[]{"Khoa", nullThanhChuoi(sinhVienHienTai.getKhoa(), "Chua cap nhat")});
                rows.add(new String[]{"Ngay sinh", sinhVienHienTai.getNgaySinh() != null
                        ? sinhVienHienTai.getNgaySinh().format(DMY) : "Chua cap nhat"});
                rows.add(new String[]{"Trang thai", sinhVienHienTai.isTrangThai() ? "Dang hoc" : "Da nghi hoc"});
                rows.add(new String[]{"Email", nullThanhChuoi(sinhVienHienTai.getEmail(), "Chua cap nhat")});
                rows.add(new String[]{"So dien thoai", nullThanhChuoi(sinhVienHienTai.getSoDienThoai(), "Chua cap nhat")});
                PDFExporter.exportBangDuLieu(duongDanCuoi, "HO SO SINH VIEN",
                        new String[]{"Truong thong tin", "Gia tri"}, rows);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(ThongTinCaNhanPanel.this, "Đã lưu hồ sơ:\n" + duongDanCuoi);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(ThongTinCaNhanPanel.this, "Xuất PDF thất bại.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /** Avatar tron: hien anh that neu co, khong thi hien chu cai dau ten. Bam vao de doi anh. */
    private class AvatarPanel extends JComponent {
        private String hoTen;
        private BufferedImage anh;
        private final int size;

        AvatarPanel(String hoTen, int size) {
            this.hoTen = hoTen;
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Bấm để đổi ảnh đại diện");
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) { moChonAnh(); }
            });
        }

        void capNhatTen(String hoTenMoi) {
            this.hoTen = hoTenMoi;
            repaint();
        }

        void taiAnh(String duongDan) {
            anh = null;
            if (duongDan != null && !duongDan.isBlank()) {
                try {
                    File f = new File(duongDan);
                    if (f.exists()) anh = ImageIO.read(f);
                } catch (IOException ignored) {
                    // Neu doc anh loi, cu de anh = null de fallback ve chu cai dau ten
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Ellipse2D clip = new Ellipse2D.Float(0, 0, getWidth(), getHeight());

            if (anh != null) {
                g2.setClip(clip);
                g2.drawImage(anh, 0, 0, getWidth(), getHeight(), null);
                g2.setClip(null);
            } else {
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fill(clip);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, size / 2 - 4)));
                String chu = (hoTen == null || hoTen.isBlank()) ? "?" : hoTen.trim().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(chu)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(chu, x, y);
            }

            g2.setColor(new Color(255, 255, 255, 160));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Float(1, 1, getWidth() - 2, getHeight() - 2));
            g2.dispose();
        }
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}