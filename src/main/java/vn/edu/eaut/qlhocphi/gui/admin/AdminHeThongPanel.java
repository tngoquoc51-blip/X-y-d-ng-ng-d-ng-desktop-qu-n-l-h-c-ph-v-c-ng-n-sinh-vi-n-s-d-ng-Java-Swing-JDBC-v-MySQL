package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.bus.TaiKhoanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard Admin – KPI vận hành + lối tắt cấu hình.
 * Chuẩn giám sát hệ thống trường ĐH (quy mô ~5000 SV).
 */
public class AdminHeThongPanel extends JPanel {

    private final TaiKhoan taiKhoan;
    private final Consumer<String> dieuHuong;
    private final SinhVienService sinhVienService = new SinhVienService();
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    private JLabel lblTongSV, lblTaiKhoan, lblAdmin, lblPDT, lblKT, lblSVTK;
    private JLabel lblCapNhat;

    public AdminHeThongPanel(TaiKhoan taiKhoan, Consumer<String> dieuHuong) {
        this.taiKhoan = taiKhoan;
        this.dieuHuong = dieuHuong;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 4, 8, 4));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildTieuDe());
        north.add(Box.createRigidArea(new Dimension(0, 14)));
        north.add(buildHangKPI());
        north.add(Box.createRigidArea(new Dimension(0, 10)));
        north.add(buildHangVaiTro());
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(buildHangCardChucNang());
        center.add(Box.createRigidArea(new Dimension(0, 14)));
        center.add(buildHuongDanNhanh());
        add(center, BorderLayout.CENTER);

        taiChiSo();
    }

    // ================== TIÊU ĐỀ ==================

    private JPanel buildTieuDe() {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);

        JLabel lblChao = new JLabel("Xin chào, " + (taiKhoan.getHoTen() != null ? taiKhoan.getHoTen() : "Admin"));
        lblChao.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblChao.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblMoTa = new JLabel("Trung tâm giám sát & cấu hình hệ thống – Quản lý học phí / công nợ quy mô lớn");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMoTa.setForeground(UITheme.TEXT_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(lblChao);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(lblMoTa);

        JLabel badge = new JLabel("  ADMIN HỆ THỐNG  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(UITheme.TEXT_VIOLET != null ? UITheme.TEXT_VIOLET : new Color(0x7C, 0x3A, 0xED));
        badge.setOpaque(true);
        badge.setBackground(UITheme.TINT_VIOLET != null ? UITheme.TINT_VIOLET : new Color(0xED, 0xE9, 0xFE));
        badge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        lblCapNhat = new JLabel("Đang tải chỉ số...");
        lblCapNhat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCapNhat.setForeground(UITheme.TEXT_MUTED);

        JPanel phai = new JPanel();
        phai.setOpaque(false);
        phai.setLayout(new BoxLayout(phai, BoxLayout.Y_AXIS));
        badge.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblCapNhat.setAlignmentX(Component.RIGHT_ALIGNMENT);
        phai.add(badge);
        phai.add(Box.createRigidArea(new Dimension(0, 6)));
        phai.add(lblCapNhat);

        box.add(text, BorderLayout.WEST);
        box.add(phai, BorderLayout.EAST);
        return box;
    }

    // ================== KPI CHÍNH ==================

    private JPanel buildHangKPI() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel c1 = theKPI("Tổng sinh viên trong hệ thống", "0", "Quy mô quản lý học phí / công nợ",
                UITheme.TINT_BLUE != null ? UITheme.TINT_BLUE : new Color(0xDB, 0xEA, 0xFE),
                UITheme.TEXT_BLUE != null ? UITheme.TEXT_BLUE : new Color(0x1D, 0x4E, 0xD8));
        JPanel c2 = theKPI("Tổng tài khoản đăng nhập", "0", "Admin · PDT · Kế toán · Sinh viên",
                UITheme.TINT_GREEN != null ? UITheme.TINT_GREEN : new Color(0xD1, 0xFA, 0xE5),
                UITheme.TEXT_GREEN != null ? UITheme.TEXT_GREEN : new Color(0x04, 0x78, 0x57));

        lblTongSV = timGiaTri(c1);
        lblTaiKhoan = timGiaTri(c2);

        row.add(c1);
        row.add(c2);
        return row;
    }

    private JPanel buildHangVaiTro() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        JPanel a = theKPINho("Admin", "0", new Color(0xEE, 0xE6, 0xFF), new Color(0x6D, 0x28, 0xD9));
        JPanel b = theKPINho("Phòng Đào tạo", "0", new Color(0xDB, 0xEA, 0xFE), new Color(0x1D, 0x4E, 0xD8));
        JPanel c = theKPINho("Kế toán", "0", new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09));
        JPanel d = theKPINho("Sinh viên (TK)", "0", new Color(0xD1, 0xFA, 0xE5), new Color(0x04, 0x78, 0x57));

        lblAdmin = timGiaTri(a);
        lblPDT = timGiaTri(b);
        lblKT = timGiaTri(c);
        lblSVTK = timGiaTri(d);

        row.add(a);
        row.add(b);
        row.add(c);
        row.add(d);
        return row;
    }

    private JPanel theKPI(String tieuDe, String giaTri, String phu, Color nen, Color chu) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(true);
        card.setBackground(nen);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lblTd = new JLabel(tieuDe);
        lblTd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTd.setForeground(chu);

        JLabel lblGt = new JLabel(giaTri);
        lblGt.setName("giaTri");
        lblGt.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblGt.setForeground(chu);

        JLabel lblPhu = new JLabel(phu);
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPhu.setForeground(new Color(chu.getRed(), chu.getGreen(), chu.getBlue(), 180));

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(lblTd);
        giua.add(Box.createRigidArea(new Dimension(0, 4)));
        giua.add(lblGt);
        giua.add(Box.createRigidArea(new Dimension(0, 2)));
        giua.add(lblPhu);

        card.add(giua, BorderLayout.CENTER);
        return card;
    }

    private JPanel theKPINho(String tieuDe, String giaTri, Color nen, Color chu) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(nen);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTd = new JLabel(tieuDe);
        lblTd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTd.setForeground(chu);

        JLabel lblGt = new JLabel(giaTri);
        lblGt.setName("giaTri");
        lblGt.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGt.setForeground(chu);

        card.add(lblTd, BorderLayout.NORTH);
        card.add(lblGt, BorderLayout.CENTER);
        return card;
    }

    private JLabel timGiaTri(JPanel the) {
        return timLabel(the, "giaTri");
    }

    private JLabel timLabel(Container c, String name) {
        for (Component x : c.getComponents()) {
            if (x instanceof JLabel && name.equals(x.getName())) return (JLabel) x;
            if (x instanceof Container) {
                JLabel f = timLabel((Container) x, name);
                if (f != null) return f;
            }
        }
        return null;
    }

    // ================== CARD CHỨC NĂNG ==================

    private JPanel buildHangCardChucNang() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(cardChucNang("Quản Lý Tài Khoản",
                "Tạo, sửa, khóa tài khoản.\nPhân quyền Admin / PDT / Kế toán / SV.",
                "KEY", UITheme.PRIMARY, new Color(0xDB, 0xEA, 0xFE), "taikhoan"));
        row.add(cardChucNang("Sao Lưu / Phục Hồi",
                "Backup CSDL định kỳ.\nPhục hồi khi sự cố – bảo vệ dữ liệu học phí.",
                "DISK", new Color(0x0E, 0xA5, 0xE9), new Color(0xE0, 0xF2, 0xFE), "backup"));
        row.add(cardChucNang("Nhật Ký Hệ Thống",
                "Theo dõi thao tác quan trọng.\nAudit log đăng nhập, thay đổi dữ liệu.",
                "LINE", new Color(0xF5, 0x9E, 0x0B), new Color(0xFE, 0xF3, 0xC7), "nhatky"));
        return row;
    }

    private JPanel cardChucNang(String tieuDe, String moTa, String loaiIcon,
                                Color mauChuDe, Color mauNenNhe, String keyManHinh) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            boolean hover = false;
            {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (dieuHuong != null) dieuHuong.accept(keyManHinh);
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (hover) {
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.fill(new RoundRectangle2D.Float(4, 6, w - 8, h - 8, 18, 18));
                }
                g2.setColor(UITheme.BG_CARD != null ? UITheme.BG_CARD : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 4, 16, 16));
                g2.setColor(hover ? mauChuDe : UITheme.BORDER);
                g2.setStroke(new BasicStroke(hover ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 3, h - 5, 16, 16));
                g2.setColor(mauChuDe);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, 5, 16, 16));
                g2.fillRect(0, 3, w - 2, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 18, 16, 18));

        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNenNhe);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(mauChuDe);
                veIconTrong(g2, loaiIcon, getWidth(), getHeight());
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(44, 44));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTieuDe.setForeground(UITheme.TEXT_PRIMARY);

        JTextArea txtMoTa = new JTextArea(moTa);
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMoTa.setForeground(UITheme.TEXT_MUTED);
        txtMoTa.setOpaque(false);
        txtMoTa.setEditable(false);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(null);
        txtMoTa.setFocusable(false);

        JPanel tren = new JPanel(new BorderLayout(10, 0));
        tren.setOpaque(false);
        tren.add(icon, BorderLayout.WEST);
        tren.add(lblTieuDe, BorderLayout.CENTER);

        JLabel lblGo = new JLabel("Mở →  ");
        lblGo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGo.setForeground(mauChuDe);
        lblGo.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(tren, BorderLayout.NORTH);
        card.add(txtMoTa, BorderLayout.CENTER);
        card.add(lblGo, BorderLayout.SOUTH);
        return card;
    }

    private void veIconTrong(Graphics2D g2, String loai, int w, int h) {
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = w / 2, cy = h / 2;
        switch (loai) {
            case "KEY" -> {
                g2.drawOval(cx - 7, cy - 9, 10, 10);
                g2.drawLine(cx + 2, cy, cx + 9, cy + 7);
                g2.drawLine(cx + 6, cy + 4, cx + 9, cy + 4);
            }
            case "DISK" -> {
                g2.drawRoundRect(cx - 9, cy - 9, 18, 18, 3, 3);
                g2.fillRect(cx - 4, cy - 9, 8, 6);
                g2.drawOval(cx - 3, cy + 2, 6, 6);
            }
            case "LINE" -> {
                g2.drawLine(cx - 8, cy + 6, cx - 3, cy - 2);
                g2.drawLine(cx - 3, cy - 2, cx + 2, cy + 3);
                g2.drawLine(cx + 2, cy + 3, cx + 8, cy - 6);
            }
            default -> g2.fillOval(cx - 4, cy - 4, 8, 8);
        }
    }

    // ================== HƯỚNG DẪN ==================

    private JPanel buildHuongDanNhanh() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD != null ? UITheme.BG_CARD : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.setColor(UITheme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel lbl = new JLabel("Hướng dẫn vận hành nhanh");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UITheme.TEXT_PRIMARY);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(dongHD("1.", "Theo dõi KPI sinh viên & tài khoản phía trên để nắm quy mô hệ thống (~5000 SV)."));
        list.add(Box.createRigidArea(new Dimension(0, 5)));
        list.add(dongHD("2.", "Dùng \"Quản Lý Tài Khoản\" để tạo / khóa tài khoản PDT, Kế toán, Sinh viên."));
        list.add(Box.createRigidArea(new Dimension(0, 5)));
        list.add(dongHD("3.", "Sao lưu CSDL định kỳ trước khi nâng cấp; kiểm tra Nhật ký khi có sự cố."));

        card.add(lbl, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel dongHD(String so, String nd) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSo.setForeground(UITheme.PRIMARY);
        JLabel lblNd = new JLabel(nd);
        lblNd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNd.setForeground(UITheme.TEXT_MUTED);
        p.add(lblSo);
        p.add(lblNd);
        return p;
    }

    // ================== TẢI CHỈ SỐ ==================

    private void taiChiSo() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int tongSV = 0, tongTK = 0, admin = 0, pdt = 0, kt = 0, sv = 0;
                try {
                    List<?> dsSV = sinhVienService.layTatCa();
                    tongSV = dsSV != null ? dsSV.size() : 0;
                } catch (Exception ignored) {}
                try {
                    List<TaiKhoan> ds = taiKhoanService.layTatCa();
                    if (ds != null) {
                        tongTK = ds.size();
                        for (TaiKhoan tk : ds) {
                            if (tk.getVaiTro() == null) continue;
                            switch (tk.getVaiTro()) {
                                case ADMIN -> admin++;
                                case PHONGDAOTAO -> pdt++;
                                case KETOAN -> kt++;
                                case SINHVIEN -> sv++;
                            }
                        }
                    }
                } catch (Exception ignored) {}
                return new int[]{tongSV, tongTK, admin, pdt, kt, sv};
            }

            @Override
            protected void done() {
                try {
                    int[] a = get();
                    if (lblTongSV != null) lblTongSV.setText(String.format("%,d", a[0]));
                    if (lblTaiKhoan != null) lblTaiKhoan.setText(String.format("%,d", a[1]));
                    if (lblAdmin != null) lblAdmin.setText(String.valueOf(a[2]));
                    if (lblPDT != null) lblPDT.setText(String.valueOf(a[3]));
                    if (lblKT != null) lblKT.setText(String.valueOf(a[4]));
                    if (lblSVTK != null) lblSVTK.setText(String.valueOf(a[5]));
                    if (lblCapNhat != null) {
                        lblCapNhat.setText("Đã cập nhật chỉ số hệ thống");
                    }
                } catch (Exception ex) {
                    if (lblCapNhat != null) lblCapNhat.setText("Không tải được chỉ số");
                }
            }
        };
        worker.execute();
    }
}