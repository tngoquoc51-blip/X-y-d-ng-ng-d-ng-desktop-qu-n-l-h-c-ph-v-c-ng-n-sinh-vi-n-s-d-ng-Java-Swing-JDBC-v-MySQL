package vn.edu.eaut.qlhocphi.gui.dashboard;

import vn.edu.eaut.qlhocphi.bus.CaLamViecService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.CaLamViec;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.PDFExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Trang "Ca lam viec" danh cho Ke toan: mo ca -> theo doi so lieu thu tien
 * truc tiep trong ca -> dong ca (chot so, khong sua duoc nua) -> xuat bao cao
 * PDF cuoi ngay. Thiet ke dong bo voi cac trang Dashboard khac (banner
 * gradient, the so lieu mau, bang lich su).
 */
public class CaLamViecPanel extends JPanel {
    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CaLamViecService caLamViecService = new CaLamViecService();
    private final TaiKhoan taiKhoan;

    private CaLamViec caHienTai;
    private JPanel theSoLieu;
    private JLabel lblTrangThaiCa;
    private JButton btnMoCa, btnDongCa, btnXuatPDF;
    private DefaultTableModel modelLichSu;

    public CaLamViecPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(buildBanner(), BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildTheSoLieu());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildLichSuCard());

        add(giua, BorderLayout.CENTER);

        taiCaHienTai();
        AutoRefreshTimer.gan(this, 30, this::taiCaHienTai);
    }

    // ================== BANNER ==================
    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setPreferredSize(new Dimension(10, 96));
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel("Ca làm việc");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tieuDe.setForeground(Color.WHITE);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTrangThaiCa = new JLabel("Đang tải trạng thái ca...");
        lblTrangThaiCa.setFont(UITheme.FONT_BASE);
        lblTrangThaiCa.setForeground(new Color(255, 255, 255, 210));
        lblTrangThaiCa.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTrangThaiCa.setBorder(new EmptyBorder(6, 0, 0, 0));

        trai.add(tieuDe);
        trai.add(lblTrangThaiCa);
        banner.add(trai, BorderLayout.WEST);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        phai.setOpaque(false);

        btnMoCa = nutTron("▶  Bắt đầu ca làm việc", new Color(0x16, 0xA3, 0x4A));
        btnMoCa.addActionListener(e -> moCa());

        btnDongCa = nutTron("■  Đóng ca (chốt sổ)", UITheme.DANGER);
        btnDongCa.addActionListener(e -> dongCa());

        btnXuatPDF = nutTron("⬇  Xuất báo cáo PDF", new Color(255, 255, 255, 45));
        btnXuatPDF.addActionListener(e -> xuatBaoCaoPDF());

        phai.add(btnXuatPDF);
        phai.add(btnMoCa);
        phai.add(btnDongCa);
        banner.add(phai, BorderLayout.EAST);

        return banner;
    }

    private JButton nutTron(String text, Color mauNen) {
        JButton b = new JButton(text);
        b.setFont(UITheme.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setBackground(mauNen);
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ================== 4 THE SO LIEU ==================
    private JPanel buildTheSoLieu() {
        theSoLieu = new JPanel(new GridLayout(1, 4, 16, 0));
        theSoLieu.setOpaque(false);
        theSoLieu.add(UITheme.statCard("Tiền mặt", "0 đ", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theSoLieu.add(UITheme.statCard("Chuyển khoản", "0 đ", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        theSoLieu.add(UITheme.statCard("Online (VNPay/MoMo)", "0 đ", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        theSoLieu.add(UITheme.statCard("Tổng số giao dịch", "0", UITheme.TINT_RED, UITheme.TEXT_RED));
        return theSoLieu;
    }

    // ================== BANG LICH SU CAC CA ==================
    private JPanel buildLichSuCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel tieuDe = new JLabel("Lịch sử các ca gần đây");
        tieuDe.setFont(UITheme.FONT_H2);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        card.add(tieuDe, BorderLayout.NORTH);

        modelLichSu = new DefaultTableModel(
                new Object[]{"Mở ca", "Đóng ca", "Tiền mặt", "Chuyển khoản", "Online", "Tổng", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelLichSu);
        UIUtils.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(10, 220));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ================== LOGIC CA LAM VIEC ==================
    private void taiCaHienTai() {
        SwingWorker<CaLamViec, Void> worker = new SwingWorker<>() {
            @Override
            protected CaLamViec doInBackground() throws Exception {
                CaLamViec ca = caLamViecService.layCaDangMo(taiKhoan);
                if (ca != null) caLamViecService.tinhTongHienTai(ca);
                return ca;
            }

            @Override
            protected void done() {
                try {
                    caHienTai = get();
                    capNhatGiaoDien();
                    taiLichSu();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CaLamViecPanel.this, "Không thể tải trạng thái ca.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void moCa() {
        SwingWorker<CaLamViec, Void> worker = new SwingWorker<>() {
            @Override
            protected CaLamViec doInBackground() throws Exception {
                return caLamViecService.moCaMoi(taiKhoan);
            }

            @Override
            protected void done() {
                try {
                    caHienTai = get();
                    capNhatGiaoDien();
                    UIUtils.thongBao(CaLamViecPanel.this, "Đã mở ca làm việc lúc " + java.time.LocalDateTime.now().format(DMY_HM));
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CaLamViecPanel.this, "Không thể mở ca.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void dongCa() {
        if (caHienTai == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Đóng ca sẽ CHỐT SỔ số liệu hiện tại, không thể sửa lại sau khi đóng.\nBạn có chắc chắn muốn đóng ca?",
                "Xác nhận đóng ca", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                caLamViecService.dongCa(caHienTai);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CaLamViecPanel.this, "Đã chốt sổ ca làm việc thành công.");
                    caHienTai = null;
                    capNhatGiaoDien();
                    taiLichSu();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CaLamViecPanel.this, "Không thể đóng ca.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void taiLichSu() {
        SwingWorker<List<CaLamViec>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CaLamViec> doInBackground() throws Exception {
                return caLamViecService.layLichSu(taiKhoan, 10);
            }

            @Override
            protected void done() {
                try {
                    List<CaLamViec> ds = get();
                    modelLichSu.setRowCount(0);
                    for (CaLamViec ca : ds) {
                        modelLichSu.addRow(new Object[]{
                                ca.getThoiGianMoCa() != null ? ca.getThoiGianMoCa().format(DMY_HM) : "",
                                ca.getThoiGianDongCa() != null ? ca.getThoiGianDongCa().format(DMY_HM) : "Đang mở",
                                MoneyUtils.format(ca.getTongTienMat()),
                                MoneyUtils.format(ca.getTongTienChuyenKhoan()),
                                MoneyUtils.format(ca.getTongTienOnline()),
                                MoneyUtils.format(ca.tongTatCa()),
                                ca.dangMo() ? "Đang mở" : "Đã chốt"
                        });
                    }
                } catch (Exception ignored) { }
            }
        };
        worker.execute();
    }

    private void capNhatGiaoDien() {
        boolean dangMo = caHienTai != null && caHienTai.dangMo();
        btnMoCa.setVisible(!dangMo);
        btnDongCa.setVisible(dangMo);
        btnXuatPDF.setVisible(dangMo);

        if (dangMo) {
            lblTrangThaiCa.setText("Đang mở ca từ " + caHienTai.getThoiGianMoCa().format(DMY_HM)
                    + "  ·  Nhân viên: " + caHienTai.getTenNhanVien());
            capNhatGiaTriThe(0, MoneyUtils.format(caHienTai.getTongTienMat()));
            capNhatGiaTriThe(1, MoneyUtils.format(caHienTai.getTongTienChuyenKhoan()));
            capNhatGiaTriThe(2, MoneyUtils.format(caHienTai.getTongTienOnline()));
            capNhatGiaTriThe(3, String.valueOf(caHienTai.getSoGiaoDich()));
        } else {
            lblTrangThaiCa.setText("Chưa mở ca làm việc nào. Bấm \"Bắt đầu ca làm việc\" để bắt đầu.");
            for (int i = 0; i < 3; i++) capNhatGiaTriThe(i, "0 đ");
            capNhatGiaTriThe(3, "0");
        }
    }

    private void capNhatGiaTriThe(int index, String giaTri) {
        JPanel the = (JPanel) theSoLieu.getComponent(index);
        JLabel lbl = timNhanTheoTen(the, "giaTri");
        if (lbl != null) lbl.setText(giaTri);
    }

    private JLabel timNhanTheoTen(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName()) && c instanceof JLabel) return (JLabel) c;
            if (c instanceof Container) {
                JLabel ket = timNhanTheoTen((Container) c, name);
                if (ket != null) return ket;
            }
        }
        return null;
    }

    // ================== XUAT PDF BAO CAO CUOI NGAY ==================
    private void xuatBaoCaoPDF() {
        if (caHienTai == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo thu ngân");
        chooser.setSelectedFile(new File("BaoCaoThuNgan_" + java.time.LocalDate.now() + ".pdf"));
        int ketQua = chooser.showSaveDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;
        String duongDan = chooser.getSelectedFile().getAbsolutePath();
        if (!duongDan.toLowerCase().endsWith(".pdf")) duongDan += ".pdf";
        String duongDanCuoi = duongDan;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                caLamViecService.tinhTongHienTai(caHienTai);
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{"Nhan vien", caHienTai.getTenNhanVien()});
                rows.add(new String[]{"Thoi gian mo ca", caHienTai.getThoiGianMoCa().format(DMY_HM)});
                rows.add(new String[]{"Thoi gian xuat bao cao", java.time.LocalDateTime.now().format(DMY_HM)});
                rows.add(new String[]{"Tien mat", MoneyUtils.format(caHienTai.getTongTienMat())});
                rows.add(new String[]{"Chuyen khoan", MoneyUtils.format(caHienTai.getTongTienChuyenKhoan())});
                rows.add(new String[]{"Online (VNPay/MoMo)", MoneyUtils.format(caHienTai.getTongTienOnline())});
                rows.add(new String[]{"TONG CONG", MoneyUtils.format(caHienTai.tongTatCa())});
                rows.add(new String[]{"So giao dich", String.valueOf(caHienTai.getSoGiaoDich())});
                PDFExporter.exportBangDuLieu(duongDanCuoi, "BAO CAO THU NGAN CA LAM VIEC",
                        new String[]{"Thong tin", "Gia tri"}, rows);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CaLamViecPanel.this, "Đã lưu báo cáo:\n" + duongDanCuoi);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CaLamViecPanel.this, "Xuất PDF thất bại.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}