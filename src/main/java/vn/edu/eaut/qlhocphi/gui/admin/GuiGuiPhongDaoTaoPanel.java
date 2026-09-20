package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Phòng Kế toán → gửi yêu cầu / thông tin nội bộ về Phòng Đào tạo.
 * ManHinhKey = "hopthu_pdt"
 */
public class GuiGuiPhongDaoTaoPanel extends JPanel {

    public static final String MAN_HINH_KEY = "hopthu_pdt";

    private final TaiKhoan taiKhoan;
    private final ThongBaoService service = new ThongBaoService();

    private final JComboBox<String> cboLoai = new JComboBox<>(new String[]{
            "Yêu cầu xử lý hồ sơ SV",
            "Báo cáo công nợ / quá hạn",
            "Đề nghị điều chỉnh học phí",
            "Xác nhận miễn giảm / học bổng",
            "Đối soát dữ liệu đăng ký",
            "Khác"
    });
    private final JTextField txtTieuDe = new JTextField();
    private final JTextArea txtNoiDung = new JTextArea(8, 40);
    private final JLabel lblTrangThai = new JLabel(" ");

    public GuiGuiPhongDaoTaoPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(0, 4, 8, 4));
        wrap.add(buildBanner());
        wrap.add(Box.createVerticalStrut(16));
        wrap.add(buildFormCard());
        wrap.add(Box.createVerticalStrut(12));
        wrap.add(buildHuongDan());

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, getWidth(), getHeight(), UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        banner.setPreferredSize(new Dimension(10, 96));

        JLabel icon = new JLabel("✉", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(48, 48));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("Gửi thông tin về Phòng Đào Tạo");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Liên thông nội bộ · Kế toán → Đào tạo · Theo quy trình nhà trường");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t2.setForeground(new Color(255, 255, 255, 210));
        text.add(t1);
        text.add(Box.createVerticalStrut(4));
        text.add(t2);

        banner.add(icon, BorderLayout.WEST);
        banner.add(text, BorderLayout.CENTER);
        return banner;
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 26, 22, 26));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        int y = 0;
        c.gridy = y++;
        card.add(sectionLabel("LOẠI THÔNG TIN"), c);
        c.gridy = y++;
        styleField(cboLoai);
        card.add(cboLoai, c);

        c.gridy = y++;
        c.insets = new Insets(14, 0, 4, 0);
        card.add(sectionLabel("TIÊU ĐỀ *"), c);
        c.gridy = y++;
        c.insets = new Insets(4, 0, 4, 0);
        styleField(txtTieuDe);
        card.add(txtTieuDe, c);

        c.gridy = y++;
        c.insets = new Insets(14, 0, 4, 0);
        card.add(sectionLabel("NỘI DUNG *"), c);
        c.gridy = y++;
        c.insets = new Insets(4, 0, 4, 0);
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        txtNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        txtNoiDung.setBackground(UITheme.BG_CARD);
        txtNoiDung.setForeground(UITheme.TEXT_PRIMARY);
        txtNoiDung.setCaretColor(UITheme.TEXT_PRIMARY);
        txtNoiDung.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane sp = new JScrollPane(txtNoiDung);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        sp.setPreferredSize(new Dimension(10, 180));
        card.add(sp, c);

        c.gridy = y++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(18, 0, 6, 0);
        JButton btnGui = new JButton("  Gửi đến Phòng Đào Tạo  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? UITheme.PRIMARY_DARK : UITheme.PRIMARY;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnGui.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGui.setForeground(Color.WHITE);
        btnGui.setContentAreaFilled(false);
        btnGui.setBorderPainted(false);
        btnGui.setFocusPainted(false);
        btnGui.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGui.setPreferredSize(new Dimension(10, 44));
        btnGui.addActionListener(e -> guiDi());
        card.add(btnGui, c);

        c.gridy = y;
        c.insets = new Insets(4, 0, 0, 0);
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        card.add(lblTrangThai, c);

        return card;
    }

    private JPanel buildHuongDan() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(4, 8, 8, 8));
        JLabel h = new JLabel("Hướng dẫn sử dụng");
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(UITheme.TEXT_PRIMARY);
        JLabel d1 = new JLabel("• Chọn đúng loại thông tin để Phòng Đào tạo ưu tiên xử lý.");
        JLabel d2 = new JLabel("• Ghi rõ mã SV, lớp, học kỳ trong tiêu đề/nội dung khi liên quan hồ sơ.");
        JLabel d3 = new JLabel("• Nội dung sẽ xuất hiện trong mục \"Hộp thư từ Kế toán\" bên phía Đào tạo.");
        for (JLabel l : new JLabel[]{d1, d2, d3}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(UITheme.TEXT_MUTED);
        }
        p.add(h);
        p.add(Box.createVerticalStrut(6));
        p.add(d1);
        p.add(d2);
        p.add(d3);
        return p;
    }

    private JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private void styleField(JComponent f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(UITheme.BG_CARD);
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)));
        if (f instanceof JTextField tf) {
            tf.setCaretColor(UITheme.TEXT_PRIMARY);
        }
    }

    private void guiDi() {
        String tieuDe = txtTieuDe.getText() != null ? txtTieuDe.getText().trim() : "";
        String noiDung = txtNoiDung.getText() != null ? txtNoiDung.getText().trim() : "";
        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            UIUtils.thongBaoLoi(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung.");
            return;
        }
        String loai = String.valueOf(cboLoai.getSelectedItem());
        String nguoiGui = taiKhoan.getHoTen() != null ? taiKhoan.getHoTen() : taiKhoan.getTenDangNhap();
        String tieuDeDayDu = "[Kế toán · " + loai + "] " + tieuDe;
        String noiDungDayDu = noiDung
                + "\n\n────────────────────\n"
                + "Người gửi: " + nguoiGui + " (Phòng Kế toán)\n"
                + "Loại: " + loai;

        service.taoThongBao(MAN_HINH_KEY, "PHONGDAOTAO", null, tieuDeDayDu, noiDungDayDu);

        lblTrangThai.setForeground(UITheme.TEXT_GREEN);
        lblTrangThai.setText("✓ Đã gửi thành công. Phòng Đào tạo sẽ thấy trong Hộp thư từ Kế toán.");
        txtTieuDe.setText("");
        txtNoiDung.setText("");
    }
}