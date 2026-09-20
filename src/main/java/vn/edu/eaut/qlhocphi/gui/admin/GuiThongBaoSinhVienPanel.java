package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gửi thông báo cho sinh viên – layout đầy đủ cho hệ thống quy mô lớn:
 * KPI + form gửi + mẫu nhanh + lịch sử phiên làm việc.
 */
public class GuiThongBaoSinhVienPanel extends JPanel {
    private final TaiKhoan taiKhoan;
    private final ThongBaoService thongBaoService = new ThongBaoService();
    private final SinhVienService sinhVienService = new SinhVienService();

    private final JTextField txtTieuDe = new JTextField();
    private final JTextArea txtNoiDung = new JTextArea(5, 40);
    private final JCheckBox chkTatCa = new JCheckBox("Gửi tất cả sinh viên trong hệ thống", true);
    private final JTextField txtMaSV = new JTextField();
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng gửi thông báo");
    private final JLabel lblTongSV = new JLabel("…");
    private final JLabel lblDaGuiPhien = new JLabel("0");
    private final JPanel khoiLichSu = new JPanel();
    private final List<String[]> lichSu = new ArrayList<>();
    private int soDaGuiPhien = 0;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");

    public GuiThongBaoSinhVienPanel() {
        this(null);
    }

    public GuiThongBaoSinhVienPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 8, 4));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildBanner());
        north.add(Box.createRigidArea(new Dimension(0, 12)));
        north.add(buildHangKPI());
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setOpaque(false);
        center.add(buildFormCard());
        center.add(buildCotPhai());
        add(center, BorderLayout.CENTER);

        taiChiSo();
    }

    // ================== BANNER ==================

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        banner.setPreferredSize(new Dimension(10, 78));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel("Gửi thông báo cho sinh viên");
        t.setFont(UITheme.FONT_TITLE);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Thông báo xuất hiện trong mục \"Thông Báo\" trên cổng sinh viên · Có thể gửi toàn trường hoặc từng mã SV");
        s.setFont(UITheme.FONT_BASE);
        s.setForeground(new Color(255, 255, 255, 215));

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(t);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(s);
        banner.add(box, BorderLayout.CENTER);
        return banner;
    }

    // ================== KPI ==================

    private JPanel buildHangKPI() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        row.add(theKPI("Tổng sinh viên", lblTongSV, "Quy mô nhận thông báo",
                new Color(0xDB, 0xEA, 0xFE), new Color(0x1D, 0x4E, 0xD8)));
        row.add(theKPI("Đã gửi (phiên này)", lblDaGuiPhien, "Số lần gửi trong phiên đăng nhập",
                new Color(0xD1, 0xFA, 0xE5), new Color(0x04, 0x78, 0x57)));
        row.add(theKPI("Kênh hiển thị", new JLabel("Cổng SV"), "Mục Thông Báo trên app sinh viên",
                new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09)));
        return row;
    }

    private JPanel theKPI(String tieuDe, JLabel lblGiaTri, String phu, Color nen, Color chu) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(true);
        p.setBackground(nen);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel td = new JLabel(tieuDe);
        td.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        td.setForeground(chu);
        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGiaTri.setForeground(chu);
        JLabel sp = new JLabel(phu);
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sp.setForeground(new Color(chu.getRed(), chu.getGreen(), chu.getBlue(), 180));
        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(td);
        mid.add(lblGiaTri);
        mid.add(sp);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    // ================== FORM ==================

    private JPanel buildFormCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel td = new JLabel("Soạn thông báo");
        td.setFont(new Font("Segoe UI", Font.BOLD, 15));
        td.setForeground(UITheme.TEXT_PRIMARY);
        td.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(td);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(label("Tiêu đề *"));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        styleField(txtTieuDe);
        card.add(txtTieuDe);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(label("Nội dung *"));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        txtNoiDung.setFont(UITheme.FONT_BASE);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(txtNoiDung);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        sp.setPreferredSize(new Dimension(100, 120));
        card.add(sp);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        chkTatCa.setFont(UITheme.FONT_BASE);
        chkTatCa.setOpaque(false);
        chkTatCa.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkTatCa.addActionListener(e -> {
            txtMaSV.setEnabled(!chkTatCa.isSelected());
            if (chkTatCa.isSelected()) txtMaSV.setText("");
        });
        card.add(chkTatCa);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        card.add(label("Mã sinh viên (khi gửi 1 người)"));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        styleField(txtMaSV);
        txtMaSV.setEnabled(false);
        card.add(txtMaSV);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JButton btnGui = UITheme.primaryButton("Gửi thông báo");
        btnGui.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGui.setMaximumSize(new Dimension(220, 40));
        btnGui.addActionListener(e -> gui());
        card.add(btnGui);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTrangThai);
        card.add(Box.createVerticalGlue());
        return card;
    }

    // ================== CỘT PHẢI ==================

    private JPanel buildCotPhai() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(buildMauNhanh());
        col.add(Box.createRigidArea(new Dimension(0, 12)));
        col.add(buildLichSu());
        return col;
    }

    private JPanel buildMauNhanh() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel td = new JLabel("Mẫu nhanh (bấm để điền form)");
        td.setFont(new Font("Segoe UI", Font.BOLD, 14));
        td.setForeground(UITheme.TEXT_PRIMARY);
        td.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(td);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(nutMau("Nhắc đóng học phí đúng hạn",
                "Nhắc đóng học phí",
                "Nhà trường nhắc sinh viên hoàn thành nghĩa vụ học phí đúng hạn để tránh ảnh hưởng đăng ký học phần."));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(nutMau("Bảo trì hệ thống",
                "Thông báo bảo trì hệ thống",
                "Hệ thống quản lý học phí sẽ bảo trì trong khung giờ quy định. Vui lòng hoàn tất giao dịch trước thời điểm bảo trì."));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(nutMau("Hướng dẫn thanh toán online",
                "Hướng dẫn thanh toán học phí trực tuyến",
                "Sinh viên có thể thanh toán qua cổng sinh viên (chuyển khoản / ví / cổng online). Mọi thắc mắc liên hệ Phòng Kế toán."));
        return card;
    }

    private JButton nutMau(String nhan, String tieuDe, String noiDung) {
        JButton b = new JButton(nhan);
        b.setFont(UITheme.FONT_BASE);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            txtTieuDe.setText(tieuDe);
            txtNoiDung.setText(noiDung);
            chkTatCa.setSelected(true);
            txtMaSV.setEnabled(false);
            txtMaSV.setText("");
        });
        return b;
    }

    private JPanel buildLichSu() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel td = new JLabel("Lịch sử gửi (phiên này)");
        td.setFont(new Font("Segoe UI", Font.BOLD, 14));
        td.setForeground(UITheme.TEXT_PRIMARY);

        khoiLichSu.setOpaque(false);
        khoiLichSu.setLayout(new BoxLayout(khoiLichSu, BoxLayout.Y_AXIS));
        khoiLichSu.add(dongTrong());

        JScrollPane sp = new JScrollPane(khoiLichSu);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(100, 180));

        card.add(td, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JLabel dongTrong() {
        JLabel l = new JLabel("Chưa gửi thông báo nào trong phiên này");
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(6, 0, 6, 0));
        return l;
    }

    private void themLichSu(String tieuDe, String doiTuong) {
        String gio = LocalDateTime.now().format(FMT);
        String nguoi = taiKhoan != null ? taiKhoan.getTenDangNhap() : "admin";
        lichSu.add(0, new String[]{gio, tieuDe, doiTuong, nguoi});
        khoiLichSu.removeAll();
        int max = Math.min(lichSu.size(), 8);
        for (int i = 0; i < max; i++) {
            String[] d = lichSu.get(i);
            JLabel l = new JLabel("<html><b>" + d[0] + "</b> · " + d[1] + " → " + d[2] + "</html>");
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(UITheme.TEXT_PRIMARY);
            l.setBorder(new EmptyBorder(4, 0, 4, 0));
            khoiLichSu.add(l);
        }
        khoiLichSu.revalidate();
        khoiLichSu.repaint();
    }

    // ================== HELPERS ==================

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField tf) {
        tf.setFont(UITheme.FONT_BASE);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private void taiChiSo() {
        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                try {
                    List<?> ds = sinhVienService.layTatCa();
                    return ds != null ? ds.size() : 0;
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            protected void done() {
                try {
                    lblTongSV.setText(String.format("%,d", get()));
                } catch (Exception ignored) {
                    lblTongSV.setText("—");
                }
            }
        };
        w.execute();
    }

    private void gui() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            lblTrangThai.setForeground(UITheme.DANGER);
            lblTrangThai.setText("Vui lòng nhập đủ tiêu đề và nội dung.");
            return;
        }
        String maSV = null;
        if (!chkTatCa.isSelected()) {
            maSV = txtMaSV.getText().trim();
            if (maSV.isEmpty()) {
                lblTrangThai.setForeground(UITheme.DANGER);
                lblTrangThai.setText("Nhập mã sinh viên hoặc chọn gửi tất cả.");
                return;
            }
        }
        final String maSVFinal = maSV;
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setText("Đang gửi...");
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                thongBaoService.taoThongBao("thongbao", "SINHVIEN", maSVFinal, tieuDe, noiDung);
                return null;
            }

            @Override
            protected void done() {
                soDaGuiPhien++;
                lblDaGuiPhien.setText(String.valueOf(soDaGuiPhien));
                String doiTuong = maSVFinal == null ? "Tất cả SV" : maSVFinal;
                themLichSu(tieuDe, doiTuong);
                lblTrangThai.setForeground(UITheme.SUCCESS);
                lblTrangThai.setText(maSVFinal == null
                        ? "Đã gửi tới tất cả sinh viên."
                        : "Đã gửi tới sinh viên " + maSVFinal + ".");
                txtTieuDe.setText("");
                txtNoiDung.setText("");
            }
        };
        w.execute();
    }
}