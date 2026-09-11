package vn.edu.eaut.qlhocphi.config;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.AlphaComposite;

/**
 * Bộ màu & font chung toàn hệ thống – phong cách "Soft Azure Thesis"
 *
 * Giao diện SÁNG sạch: sidebar trắng, primary xanh azure tinh tế,
 * card trắng, nền xám-xanh rất nhạt. Phối màu hài hòa, hiện đại,
 * phù hợp đồ án tốt nghiệp (không dùng sidebar tối).
 *
 * Hỗ trợ 2 chế độ SANG / TỐI. Field màu static (không final) để đổi runtime.
 *
 * Màn đăng nhập (LoginFrame / StudentLoginFrame) KHÔNG sửa – giữ nguyên.
 * Chỉ trang sau đăng nhập (Admin / Sinh viên / Kế toán) nhận palette này.
 */
public class UITheme {
    // ===================================================================
    // ===== FIELD MÀU ĐANG DÙNG – static, đổi được runtime =====
    // ===================================================================
    public static Color BG_MAIN;
    public static Color BG_CARD;
    public static Color BG_SIDEBAR;
    public static Color PRIMARY;
    public static Color PRIMARY_DARK;
    public static Color ACCENT_TEAL;
    public static Color SUCCESS;
    public static Color WARNING;
    public static Color DANGER;
    public static Color TEXT_PRIMARY;
    public static Color TEXT_MUTED;
    public static Color BORDER;
    public static Color SHADOW_TONE;

    public static Color TINT_VIOLET;
    public static Color TEXT_VIOLET;
    public static Color TINT_GREEN;
    public static Color TEXT_GREEN;
    public static Color TINT_RED;
    public static Color TEXT_RED;
    public static Color TINT_BLUE;
    public static Color TEXT_BLUE;

    public static Color SIDEBAR_BLUE;
    public static Color SIDEBAR_ORANGE;
    public static Color SIDEBAR_GREEN;
    public static Color SIDEBAR_PURPLE;
    public static Color SIDEBAR_GRAY;
    public static Color SIDEBAR_ACTIVE;

    public static Color HEADER_TEAL_1;
    public static Color HEADER_TEAL_2;

    public static final Font FONT_BASE  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_H2    = new Font("Segoe UI", Font.BOLD, 16);

    // ===================================================================
    // ===== BẢNG MÀU CHẾ ĐỘ SÁNG – Soft Azure Thesis (sạch, hiện đại) =====
    // ===================================================================
    private static final Color L_BG_MAIN      = new Color(0xF0, 0xF4, 0xF8); // nền xám-xanh rất nhạt
    private static final Color L_BG_CARD      = Color.WHITE;
    private static final Color L_BG_SIDEBAR   = Color.WHITE;                 // sidebar trắng sạch
    private static final Color L_PRIMARY      = new Color(0x25, 0x63, 0xEB); // blue-600 – azure tinh tế
    private static final Color L_PRIMARY_DARK = new Color(0x1D, 0x4E, 0xD8); // blue-700
    private static final Color L_ACCENT_TEAL  = new Color(0xF5, 0x9E, 0x0B); // amber CTA
    private static final Color L_SUCCESS      = new Color(0x05, 0x96, 0x69); // emerald-600
    private static final Color L_WARNING      = new Color(0xD9, 0x77, 0x06);
    private static final Color L_DANGER       = new Color(0xDC, 0x26, 0x26);
    private static final Color L_TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A); // slate-900
    private static final Color L_TEXT_MUTED   = new Color(0x64, 0x74, 0x8B); // slate-500
    private static final Color L_BORDER       = new Color(0xE2, 0xE8, 0xF0); // slate-200
    private static final Color L_SHADOW_TONE  = new Color(0xCB, 0xD5, 0xE1); // slate-300

    // Stat cards – pastel nhẹ nhàng
    private static final Color L_TINT_VIOLET  = new Color(0xDB, 0xEA, 0xFE); // blue-100
    private static final Color L_TEXT_VIOLET  = new Color(0x25, 0x63, 0xEB);
    private static final Color L_TINT_GREEN   = new Color(0xD1, 0xFA, 0xE5); // emerald-100
    private static final Color L_TEXT_GREEN   = new Color(0x05, 0x96, 0x69);
    private static final Color L_TINT_RED     = new Color(0xFE, 0xE2, 0xE2); // red-100
    private static final Color L_TEXT_RED     = new Color(0xDC, 0x26, 0x26);
    private static final Color L_TINT_BLUE    = new Color(0xE0, 0xE7, 0xFF); // indigo-100
    private static final Color L_TEXT_BLUE    = new Color(0x4F, 0x46, 0xE5);

    // Icon menu sidebar (nền trắng → dùng màu đậm vừa phải)
    private static final Color L_SIDEBAR_BLUE   = new Color(0x3B, 0x82, 0xF6); // blue-500
    private static final Color L_SIDEBAR_ORANGE = new Color(0xF5, 0x9E, 0x0B); // amber-500
    private static final Color L_SIDEBAR_GREEN  = new Color(0x10, 0xB9, 0x81); // emerald-500
    private static final Color L_SIDEBAR_PURPLE = new Color(0x8B, 0x5C, 0xF6); // violet-500
    private static final Color L_SIDEBAR_GRAY   = new Color(0x64, 0x74, 0x8B); // slate-500
    private static final Color L_SIDEBAR_ACTIVE = new Color(0xEF, 0xF6, 0xFF); // blue-50 – nền active nhẹ

    // Header / banner gradient – azure dịu, không quá chói
    private static final Color L_HEADER_TEAL_1 = new Color(0x1D, 0x4E, 0xD8); // blue-700
    private static final Color L_HEADER_TEAL_2 = new Color(0x3B, 0x82, 0xF6); // blue-500

    // ===================================================================
    // ===== BẢNG MÀU CHẾ ĐỘ TỐI – Soft Night =====
    // ===================================================================
    private static final Color D_BG_MAIN      = new Color(0x0F, 0x17, 0x2A);
    private static final Color D_BG_CARD      = new Color(0x1E, 0x29, 0x3B);
    private static final Color D_BG_SIDEBAR   = new Color(0x1E, 0x29, 0x3B);
    private static final Color D_PRIMARY      = new Color(0x60, 0xA5, 0xFA);
    private static final Color D_PRIMARY_DARK = new Color(0x3B, 0x82, 0xF6);
    private static final Color D_ACCENT_TEAL  = new Color(0xFB, 0xBF, 0x24);
    private static final Color D_SUCCESS      = new Color(0x34, 0xD3, 0x99);
    private static final Color D_WARNING      = new Color(0xFB, 0xBF, 0x24);
    private static final Color D_DANGER       = new Color(0xF8, 0x71, 0x71);
    private static final Color D_TEXT_PRIMARY = new Color(0xF1, 0xF5, 0xF9);
    private static final Color D_TEXT_MUTED   = new Color(0x94, 0xA3, 0xB8);
    private static final Color D_BORDER       = new Color(0x33, 0x41, 0x55);
    private static final Color D_SHADOW_TONE  = new Color(0x02, 0x04, 0x08);

    private static final Color D_TINT_VIOLET  = new Color(0x1E, 0x3A, 0x5F);
    private static final Color D_TEXT_VIOLET  = new Color(0x93, 0xC5, 0xFD);
    private static final Color D_TINT_GREEN   = new Color(0x0A, 0x2E, 0x22);
    private static final Color D_TEXT_GREEN   = new Color(0x6E, 0xE7, 0xB7);
    private static final Color D_TINT_RED     = new Color(0x3B, 0x12, 0x12);
    private static final Color D_TEXT_RED     = new Color(0xF8, 0x71, 0x71);
    private static final Color D_TINT_BLUE    = new Color(0x1E, 0x1B, 0x4B);
    private static final Color D_TEXT_BLUE    = new Color(0xA5, 0xB4, 0xFC);

    private static final Color D_SIDEBAR_BLUE   = new Color(0x60, 0xA5, 0xFA);
    private static final Color D_SIDEBAR_ORANGE = new Color(0xFB, 0xBF, 0x24);
    private static final Color D_SIDEBAR_GREEN  = new Color(0x34, 0xD3, 0x99);
    private static final Color D_SIDEBAR_PURPLE = new Color(0xA7, 0x8B, 0xFA);
    private static final Color D_SIDEBAR_GRAY   = new Color(0x94, 0xA3, 0xB8);
    private static final Color D_SIDEBAR_ACTIVE = new Color(0x1E, 0x3A, 0x8A);

    private static final Color D_HEADER_TEAL_1 = new Color(0x1D, 0x4E, 0xD8);
    private static final Color D_HEADER_TEAL_2 = new Color(0x3B, 0x82, 0xF6);

    static {
        apDungTheoCheDo();
    }

    public static void apDungTheoCheDo() {
        boolean toi = ThemeMode.layHienTai().laToi();

        BG_MAIN      = toi ? D_BG_MAIN      : L_BG_MAIN;
        BG_CARD      = toi ? D_BG_CARD      : L_BG_CARD;
        BG_SIDEBAR   = toi ? D_BG_SIDEBAR   : L_BG_SIDEBAR;
        PRIMARY      = toi ? D_PRIMARY      : L_PRIMARY;
        PRIMARY_DARK = toi ? D_PRIMARY_DARK : L_PRIMARY_DARK;
        ACCENT_TEAL  = toi ? D_ACCENT_TEAL  : L_ACCENT_TEAL;
        SUCCESS      = toi ? D_SUCCESS      : L_SUCCESS;
        WARNING      = toi ? D_WARNING      : L_WARNING;
        DANGER       = toi ? D_DANGER       : L_DANGER;
        TEXT_PRIMARY = toi ? D_TEXT_PRIMARY : L_TEXT_PRIMARY;
        TEXT_MUTED   = toi ? D_TEXT_MUTED   : L_TEXT_MUTED;
        BORDER       = toi ? D_BORDER       : L_BORDER;
        SHADOW_TONE  = toi ? D_SHADOW_TONE  : L_SHADOW_TONE;

        TINT_VIOLET  = toi ? D_TINT_VIOLET  : L_TINT_VIOLET;
        TEXT_VIOLET  = toi ? D_TEXT_VIOLET  : L_TEXT_VIOLET;
        TINT_GREEN   = toi ? D_TINT_GREEN   : L_TINT_GREEN;
        TEXT_GREEN   = toi ? D_TEXT_GREEN   : L_TEXT_GREEN;
        TINT_RED     = toi ? D_TINT_RED     : L_TINT_RED;
        TEXT_RED     = toi ? D_TEXT_RED     : L_TEXT_RED;
        TINT_BLUE    = toi ? D_TINT_BLUE    : L_TINT_BLUE;
        TEXT_BLUE    = toi ? D_TEXT_BLUE    : L_TEXT_BLUE;

        SIDEBAR_BLUE   = toi ? D_SIDEBAR_BLUE   : L_SIDEBAR_BLUE;
        SIDEBAR_ORANGE = toi ? D_SIDEBAR_ORANGE : L_SIDEBAR_ORANGE;
        SIDEBAR_GREEN  = toi ? D_SIDEBAR_GREEN  : L_SIDEBAR_GREEN;
        SIDEBAR_PURPLE = toi ? D_SIDEBAR_PURPLE : L_SIDEBAR_PURPLE;
        SIDEBAR_GRAY   = toi ? D_SIDEBAR_GRAY   : L_SIDEBAR_GRAY;
        SIDEBAR_ACTIVE = toi ? D_SIDEBAR_ACTIVE : L_SIDEBAR_ACTIVE;

        HEADER_TEAL_1 = toi ? D_HEADER_TEAL_1 : L_HEADER_TEAL_1;
        HEADER_TEAL_2 = toi ? D_HEADER_TEAL_2 : L_HEADER_TEAL_2;

        capNhatUIManager();
    }

    private static void capNhatUIManager() {
        UIManager.put("control", BG_MAIN);
        UIManager.put("info", BG_CARD);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusBlueGrey", BG_MAIN);
        UIManager.put("text", TEXT_PRIMARY);

        UIManager.put("Panel.background", new ColorUIResource(BG_MAIN));
        UIManager.put("OptionPane.background", new ColorUIResource(BG_CARD));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT_PRIMARY));
    }

    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UIManager.put("Button.font", new FontUIResource(FONT_BASE));
        UIManager.put("Label.font", new FontUIResource(FONT_BASE));
        UIManager.put("TextField.font", new FontUIResource(FONT_BASE));
        UIManager.put("Table.font", new FontUIResource(FONT_BASE));
        UIManager.put("TableHeader.font", new FontUIResource(FONT_BOLD));

        ThemeAutoFixer.kichHoat();
        apDungTheoCheDo();
    }

    public static JButton primaryButton(String text) {
        return buildFlatButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(BG_CARD);
        b.setForeground(PRIMARY);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton dangerButton(String text) {
        return buildFlatButton(text, DANGER, Color.WHITE);
    }

    public static JButton accentButton(String text) {
        return buildFlatButton(text, ACCENT_TEAL, new Color(0x1A, 0x14, 0x02));
    }

    private static JButton buildFlatButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getParent() != null ? getParent().getBackground() : BG_MAIN);
                g2.fillRect(0, 0, w, h);
                g2.setColor(SHADOW_TONE);
                g2.fillRoundRect(3, 4, w - 6, h - 6, 14, 14);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, w - 6, h - 6, 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, w - 7, h - 7, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(true);
        p.setBackground(BG_MAIN);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 20));
        return p;
    }

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_H2);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JPanel gradientBanner() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getParent() != null ? getParent().getBackground() : BG_MAIN);
                g2.fillRect(0, 0, w, h);

                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, w, h, PRIMARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 16, 16);

                GradientPaint glow = new GradientPaint(0, 0, new Color(0x93, 0xC5, 0xFD), w * 0.5f, h * 0.9f, PRIMARY);
                g2.setPaint(glow);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                g2.fillOval(-w / 4, -h, w, h * 2);

                g2.setClip(null);
                g2.dispose();
            }
        };
        p.setOpaque(true);
        p.setBackground(BG_MAIN);
        return p;
    }

    public static JButton quickActionCard(String iconText, String nhanDanhMuc, String tieuDe, Color mauIcon) {
        JButton card = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getParent() != null ? getParent().getBackground() : BG_MAIN);
                g2.fillRect(0, 0, w, h);
                g2.setColor(SHADOW_TONE);
                g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, w - 4, h - 4, 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, w - 5, h - 5, 12, 12);
                g2.dispose();
            }
        };
        card.setContentAreaFilled(false);
        card.setOpaque(true);
        card.setBackground(BG_MAIN);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 18, 18));
        card.setFocusPainted(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, mauIcon.brighter(), getWidth(), getHeight(), mauIcon);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        iconLabel.setOpaque(false);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        iconLabel.setPreferredSize(new Dimension(44, 44));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel nhanLabel = new JLabel(nhanDanhMuc.toUpperCase());
        nhanLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nhanLabel.setForeground(TEXT_MUTED);
        JLabel tieuDeLabel = new JLabel(tieuDe);
        tieuDeLabel.setFont(FONT_BOLD);
        tieuDeLabel.setForeground(TEXT_PRIMARY);
        textPanel.add(nhanLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(tieuDeLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    public static JPanel infoListCard(String tieuDe) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getParent() != null ? getParent().getBackground() : BG_MAIN);
                g2.fillRect(0, 0, w, h);
                g2.setColor(SHADOW_TONE);
                g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, w - 4, h - 4, 12, 12);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, 4, h - 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(BG_MAIN);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));

        JLabel title = new JLabel(tieuDe);
        title.setFont(FONT_H2);
        title.setForeground(TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);
        return card;
    }

    public static JPanel statCard(String nhan, String giaTri, Color nenNhat, Color chuDam) {
        JPanel the = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getParent() != null ? getParent().getBackground() : BG_MAIN);
                g2.fillRect(0, 0, w, h);
                g2.setColor(SHADOW_TONE);
                g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);
                g2.setColor(nenNhat);
                g2.fillRoundRect(0, 0, w - 4, h - 4, 12, 12);
                g2.setColor(chuDam);
                g2.fillRoundRect(0, 0, 5, h - 4, 5, 5);
                g2.dispose();
            }
        };
        the.setOpaque(true);
        the.setBackground(BG_MAIN);
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBorder(BorderFactory.createEmptyBorder(16, 22, 18, 20));

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNhan.setForeground(chuDam);
        lblNhan.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGiaTri.setForeground(chuDam);
        lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGiaTri.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        lblGiaTri.setName("giaTri");

        the.add(lblNhan);
        the.add(lblGiaTri);
        return the;
    }

    public static JLabel pill(String text, Color nenNhat, Color chuDam) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setBackground(nenNhat);
        l.setForeground(chuDam);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return l;
    }

    public static JLabel avatarTron(String hoTen) {
        String chuCai = (hoTen == null || hoTen.isBlank()) ? "?" : hoTen.trim().substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(chuCai, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        avatar.setPreferredSize(new Dimension(34, 34));
        return avatar;
    }

    public static JPanel infoListRow(String noiDung, String phuChu) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JLabel cham = new JLabel("●");
        cham.setForeground(PRIMARY);
        cham.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JLabel noiDungLabel = new JLabel(noiDung);
        noiDungLabel.setFont(FONT_BASE);
        noiDungLabel.setForeground(TEXT_PRIMARY);

        JPanel trai = new JPanel(new BorderLayout(10, 0));
        trai.setOpaque(false);
        trai.add(cham, BorderLayout.WEST);
        trai.add(noiDungLabel, BorderLayout.CENTER);
        row.add(trai, BorderLayout.CENTER);

        if (phuChu != null) {
            JLabel phuLabel = new JLabel(phuChu);
            phuLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            phuLabel.setForeground(TEXT_MUTED);
            row.add(phuLabel, BorderLayout.EAST);
        }
        return row;
    }

    private static final String[] HOA_TIET_HOC_TAP = {
            "\uD83C\uDF93", "\uD83D\uDCDA", "\u270F\uFE0F", "\uD83D\uDCD6", "\uD83D\uDD8A\uFE0F",
            "\uD83D\uDCD0", "\uD83E\uDDEE", "\uD83D\uDCDD", "\uD83D\uDCBB", "\uD83C\uDFAF",
            "\uD83D\uDCC8", "\uD83D\uDD0D", "\u2696\uFE0F", "\uD83D\uDCC5"
    };

    private static final Color[] MAU_HOA_TIET = {
            new Color(0xFF, 0x9F, 0x1C),
            new Color(0x38, 0xBD, 0xF8),
            new Color(0x34, 0xD3, 0x99),
            new Color(0xF4, 0x72, 0xB6),
            new Color(0xA7, 0x8B, 0xFA),
            new Color(0xFB, 0x71, 0x85),
            new Color(0xFA, 0xCC, 0x15),
            new Color(0x94, 0xE2, 0x64),
            new Color(0x60, 0xA5, 0xFA),
            new Color(0xF0, 0xAB, 0xFC),
            new Color(0x2D, 0xD4, 0xBF),
            new Color(0xFD, 0xBA, 0x74),
            new Color(0xC4, 0xB5, 0xFD),
            new Color(0x7D, 0xD3, 0xFC)
    };

    public static void veHoaTietHocTap(Graphics2D goc, int width, int height) {
        Graphics2D g = (Graphics2D) goc.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        java.util.Random rd = new java.util.Random(2026);
        int soLuong = Math.max(16, (width * height) / 34000);

        int chiSoTruoc = -1;
        for (int i = 0; i < soLuong; i++) {
            int chiSo;
            do {
                chiSo = rd.nextInt(HOA_TIET_HOC_TAP.length);
            } while (chiSo == chiSoTruoc && HOA_TIET_HOC_TAP.length > 1);
            chiSoTruoc = chiSo;

            String icon = HOA_TIET_HOC_TAP[chiSo];
            Color mauIcon = MAU_HOA_TIET[chiSo % MAU_HOA_TIET.length];

            int kichThuoc = 42 + rd.nextInt(56);
            int x = rd.nextInt(Math.max(width, 1));
            int y = rd.nextInt(Math.max(height, 1));
            double goXoay = (rd.nextDouble() - 0.5) * (Math.PI / 6);
            float doDam = 0.30f + rd.nextFloat() * 0.20f;

            g.setFont(new Font("Segoe UI Symbol", Font.PLAIN, kichThuoc));
            java.awt.geom.AffineTransform cu = g.getTransform();
            g.translate(x, y);
            g.rotate(goXoay);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, doDam + 0.15f)));
            g.setColor(new Color(0, 0, 0));
            g.drawString(icon, 1, 1);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, doDam));
            g.setColor(mauIcon);
            g.drawString(icon, 0, 0);

            g.setTransform(cu);
        }
        g.dispose();
    }

    public static JButton lienKetChu(String text) {
        JButton b = new JButton("<html><u>" + text + "</u></html>");
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(PRIMARY);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}