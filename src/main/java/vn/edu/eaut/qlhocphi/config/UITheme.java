package vn.edu.eaut.qlhocphi.config;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

/**
 * Bộ màu và font chung cho toàn bộ giao diện - tông màu sáng, hiện đại.
 */
public class UITheme {
    // ===== Bảng màu chính (tông sáng) =====
    public static final Color BG_MAIN      = new Color(0xF4, 0xF7, 0xFC); // Nền chính
    public static final Color BG_CARD      = Color.WHITE;                  // Nền thẻ/panel nội dung
    public static final Color BG_SIDEBAR   = new Color(0x1F, 0x2A, 0x44);  // Sidebar tối để tương phản
    public static final Color PRIMARY      = new Color(0x2F, 0x6F, 0xED);  // Xanh dương chính
    public static final Color PRIMARY_DARK = new Color(0x1E, 0x4F, 0xC4);
    public static final Color ACCENT_TEAL  = new Color(0x14, 0xB8, 0xA6);
    public static final Color SUCCESS      = new Color(0x22, 0xA0, 0x6B);
    public static final Color WARNING      = new Color(0xE8, 0xA5, 0x0E);
    public static final Color DANGER       = new Color(0xE1, 0x4B, 0x4B);
    public static final Color TEXT_PRIMARY = new Color(0x1F, 0x29, 0x37);
    public static final Color TEXT_MUTED   = new Color(0x6B, 0x74, 0x80);
    public static final Color BORDER       = new Color(0xE2, 0xE6, 0xEC);

    // ===== Bảng màu bổ sung =====
    public static final Color TINT_VIOLET  = new Color(0xED, 0xEA, 0xFB); // "Học Phí"
    public static final Color TEXT_VIOLET  = new Color(0x5B, 0x4D, 0xC9);
    public static final Color TINT_GREEN   = new Color(0xE4, 0xF6, 0xE9); // "Đã Đóng"
    public static final Color TEXT_GREEN   = new Color(0x1E, 0x8A, 0x4C);
    public static final Color TINT_RED     = new Color(0xFB, 0xE7, 0xE7); // "Còn Nợ"
    public static final Color TEXT_RED     = new Color(0xC6, 0x39, 0x39);
    public static final Color TINT_BLUE    = new Color(0xE3, 0xEF, 0xFD); // "Tiến Độ Đóng"
    public static final Color TEXT_BLUE    = new Color(0x1D, 0x5B, 0xC7);

    // Màu khối vuông nhỏ cạnh mục menu sidebar
    public static final Color SIDEBAR_BLUE   = new Color(0x3B, 0x82, 0xF6);
    public static final Color SIDEBAR_ORANGE = new Color(0xF5, 0x9E, 0x0B);
    public static final Color SIDEBAR_GREEN  = new Color(0x22, 0xC5, 0x5E);
    public static final Color SIDEBAR_PURPLE = new Color(0x8B, 0x5C, 0xF6);
    public static final Color SIDEBAR_GRAY   = new Color(0x9C, 0xA3, 0xAF);
    public static final Color SIDEBAR_ACTIVE = new Color(0x2A, 0x38, 0x58);

    // Gradient đài tiêu đề trên cùng ứng dụng (teal)
    public static final Color HEADER_TEAL_1 = new Color(0x0D, 0x9C, 0x8A);
    public static final Color HEADER_TEAL_2 = new Color(0x14, 0xB8, 0xA6);

    public static final Font FONT_BASE  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_H2    = new Font("Segoe UI", Font.BOLD, 16);

    /** Gọi 1 lần trong main() trước khi tạo bất kỳ JFrame nào. */
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UIManager.put("control", BG_MAIN);
        UIManager.put("info", BG_CARD);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusBlueGrey", BG_MAIN);
        UIManager.put("text", TEXT_PRIMARY);

        UIManager.put("Panel.background", new ColorUIResource(BG_MAIN));
        UIManager.put("OptionPane.background", new ColorUIResource(BG_CARD));
        UIManager.put("Button.font", new FontUIResource(FONT_BASE));
        UIManager.put("Label.font", new FontUIResource(FONT_BASE));
        UIManager.put("TextField.font", new FontUIResource(FONT_BASE));
        UIManager.put("Table.font", new FontUIResource(FONT_BASE));
        UIManager.put("TableHeader.font", new FontUIResource(FONT_BOLD));
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        // Đã sửa Segoe UI Emoji -> Segoe UI
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(Color.WHITE);
        b.setForeground(PRIMARY);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        // Đã sửa Segoe UI Emoji -> Segoe UI
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
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
        return new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), getHeight(), PRIMARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
    }

    public static JButton quickActionCard(String iconText, String nhanDanhMuc, String tieuDe, Color mauIcon) {
        JButton card = new JButton();
        card.setLayout(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        card.setFocusPainted(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(mauIcon);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        iconLabel.setPreferredSize(new Dimension(44, 44));
        iconLabel.setBorder(BorderFactory.createEmptyBorder());

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
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, PRIMARY),
                        BorderFactory.createLineBorder(BORDER, 1)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel title = new JLabel(tieuDe);
        title.setFont(FONT_H2);
        title.setForeground(TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);
        return card;
    }

    public static JPanel statCard(String nhan, String giaTri, Color nenNhat, Color chuDam) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(nenNhat);
        the.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNhan.setForeground(chuDam);
        lblNhan.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 20));
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
                g2.setColor(PRIMARY);
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
}