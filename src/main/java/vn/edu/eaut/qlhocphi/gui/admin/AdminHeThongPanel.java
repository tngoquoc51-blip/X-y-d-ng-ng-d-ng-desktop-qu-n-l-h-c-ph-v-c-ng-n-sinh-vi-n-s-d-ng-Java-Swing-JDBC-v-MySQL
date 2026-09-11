package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Dashboard dành riêng cho vai trò ADMIN (bảo trì & cấu hình hệ thống).
 * Phong cách Soft Azure Thesis – card hiện đại, icon vector, hover mượt.
 * MVC: View (panel) – không chứa logic nghiệp vụ nặng.
 */
public class AdminHeThongPanel extends JPanel {

    private final TaiKhoan taiKhoan;
    private final Consumer<String> dieuHuong;

    public AdminHeThongPanel(TaiKhoan taiKhoan, Consumer<String> dieuHuong) {
        this.taiKhoan = taiKhoan;
        this.dieuHuong = dieuHuong;
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 4, 8, 4));

        add(buildTieuDe(), BorderLayout.NORTH);
        add(buildNoiDung(), BorderLayout.CENTER);
    }

    // ================== TIÊU ĐỀ ==================

    private JPanel buildTieuDe() {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);

        JLabel lblChao = new JLabel("Xin chào, " + taiKhoan.getHoTen());
        lblChao.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblChao.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblMoTa = new JLabel("Khu vực bảo trì & cấu hình hệ thống – chỉ dành cho Admin");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMoTa.setForeground(UITheme.TEXT_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(lblChao);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(lblMoTa);

        // Badge vai trò
        JLabel badge = new JLabel("  ADMIN HỆ THỐNG  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(UITheme.TEXT_VIOLET);
        badge.setOpaque(true);
        badge.setBackground(UITheme.TINT_VIOLET);
        badge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        badge.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        phai.setOpaque(false);
        phai.add(badge);

        box.add(text, BorderLayout.WEST);
        box.add(phai, BorderLayout.EAST);
        return box;
    }

    // ================== NỘI DUNG CHÍNH ==================

    private JPanel buildNoiDung() {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        // Hàng 3 card chức năng chính
        JPanel hangCard = new JPanel(new GridLayout(1, 3, 18, 0));
        hangCard.setOpaque(false);
        hangCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        hangCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        hangCard.add(taoTheChucNang(
                "Quản Lý Tài Khoản",
                "Tạo, sửa, khóa tài khoản.\nPhân quyền Admin / Phòng Đào Tạo / Kế toán / Sinh viên.",
                UITheme.TEXT_VIOLET,
                UITheme.TINT_VIOLET,
                "KEY",
                "taikhoan"
        ));
        hangCard.add(taoTheChucNang(
                "Sao Lưu / Phục Hồi",
                "Backup CSDL định kỳ.\nPhục hồi khi sự cố – bảo vệ dữ liệu học phí.",
                UITheme.PRIMARY,
                UITheme.TINT_BLUE,
                "DISK",
                "backup"
        ));
        hangCard.add(taoTheChucNang(
                "Nhật Ký Hệ Thống",
                "Theo dõi mọi thao tác quan trọng.\nAudit log đăng nhập, thay đổi dữ liệu.",
                UITheme.WARNING,
                new Color(0xFD, 0xF3, 0xDA),
                "LINE",
                "nhatky"
        ));

        root.add(hangCard);
        root.add(Box.createRigidArea(new Dimension(0, 28)));

        // Khối hướng dẫn nhanh
        root.add(buildHuongDanNhanh());
        root.add(Box.createVerticalGlue());

        return root;
    }

    /** Card chức năng bo góc, hover nổi, click chuyển màn hình. */
    private JPanel taoTheChucNang(String tieuDe, String moTa, Color mauChuDe,
                                  Color mauNenNhe, String loaiIcon, String keyManHinh) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            private boolean hover = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        hover = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        repaint();
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        hover = false;
                        setCursor(Cursor.getDefaultCursor());
                        repaint();
                    }
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

                // Bóng đổ nhẹ khi hover
                if (hover) {
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.fill(new RoundRectangle2D.Float(4, 6, w - 8, h - 8, 18, 18));
                }

                g2.setColor(UITheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 4, 16, 16));

                // Viền
                g2.setColor(hover ? mauChuDe : UITheme.BORDER);
                g2.setStroke(new BasicStroke(hover ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 3, h - 5, 16, 16));

                // Thanh màu trên cùng
                g2.setColor(mauChuDe);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, 5, 16, 16));
                g2.fillRect(0, 3, w - 2, 4);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 20, 18, 20));

        // Icon tròn
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
        icon.setPreferredSize(new Dimension(48, 48));
        icon.setMaximumSize(new Dimension(48, 48));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 16));
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

        JPanel tren = new JPanel(new BorderLayout(12, 0));
        tren.setOpaque(false);
        tren.add(icon, BorderLayout.WEST);
        tren.add(lblTieuDe, BorderLayout.CENTER);

        card.add(tren, BorderLayout.NORTH);
        card.add(txtMoTa, BorderLayout.CENTER);

        JLabel lblGo = new JLabel("Mở →  ");
        lblGo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGo.setForeground(mauChuDe);
        lblGo.setHorizontalAlignment(SwingConstants.RIGHT);
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

    // ================== HƯỚNG DẪN NHANH ==================

    private JPanel buildHuongDanNhanh() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.setColor(UITheme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel lbl = new JLabel("Hướng dẫn nhanh");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(UITheme.TEXT_PRIMARY);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(dongHuongDan("1.", "Dùng \"Quản Lý Tài Khoản\" để tạo tài khoản Phòng Đào Tạo / Kế toán / Sinh viên."));
        list.add(Box.createRigidArea(new Dimension(0, 6)));
        list.add(dongHuongDan("2.", "Sao lưu CSDL định kỳ (Backup) trước khi nâng cấp hoặc thay đổi lớn."));
        list.add(Box.createRigidArea(new Dimension(0, 6)));
        list.add(dongHuongDan("3.", "Kiểm tra \"Nhật Ký Hệ Thống\" khi cần truy vết thao tác hoặc sự cố."));

        card.add(lbl, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel dongHuongDan(String so, String noiDung) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSo.setForeground(UITheme.PRIMARY);
        JLabel lblNd = new JLabel(noiDung);
        lblNd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNd.setForeground(UITheme.TEXT_MUTED);
        p.add(lblSo);
        p.add(lblNd);
        return p;
    }
}