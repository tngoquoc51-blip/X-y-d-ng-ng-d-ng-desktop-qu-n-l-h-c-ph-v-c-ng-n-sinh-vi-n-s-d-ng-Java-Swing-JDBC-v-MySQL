package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.TaiKhoanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.PasswordUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hop thoai doi mat khau – dung chung moi vai tro.
 * Giu nguyen 2 che do BINH THUONG / BAT BUOC va toan bo logic.
 * UI: sky gradient, Segoe UI, o nhap focus sky, dong bo he thong truong.
 */
public class DoiMatKhauDialog extends JDialog {

    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final TaiKhoan taiKhoan;
    private final boolean batBuoc;
    private Runnable khiDoiThanhCong;

    private JPasswordField txtMatKhauCu;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtXacNhan;
    private JLabel lblThongBao;
    private JButton btnLuu;

    private static final Color SKY_DAM     = new Color(0x02, 0x6A, 0xA8);
    private static final Color SKY_GIUA    = new Color(0x0E, 0xA5, 0xE9);
    private static final Color SKY_PRIMARY = new Color(0x02, 0x84, 0xC7);
    private static final Color BORDER      = new Color(0xE2, 0xE8, 0xF0);
    private static final Color BORDER_FOCUS = new Color(0x7D, 0xD3, 0xFC);
    private static final Color MUTED       = new Color(0x64, 0x74, 0x8B);
    private static final Color TEXT        = new Color(0x0F, 0x17, 0x2A);
    private static final Color BG_SOFT     = new Color(0xF8, 0xFA, 0xFC);

    private static final Font FONT       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    public DoiMatKhauDialog(Window chaMe, TaiKhoan taiKhoan) {
        this(chaMe, taiKhoan, false, null);
    }

    public DoiMatKhauDialog(Window chaMe, TaiKhoan taiKhoan, boolean batBuoc, Runnable khiDoiThanhCong) {
        super(chaMe, batBuoc ? "Bắt buộc đổi mật khẩu" : "Đổi mật khẩu",
                ModalityType.APPLICATION_MODAL);
        this.taiKhoan = taiKhoan;
        this.batBuoc = batBuoc;
        this.khiDoiThanhCong = khiDoiThanhCong;

        setSize(440, batBuoc ? 480 : 540);
        setMinimumSize(new Dimension(400, batBuoc ? 450 : 500));
        setLocationRelativeTo(chaMe);
        setResizable(false);
        setDefaultCloseOperation(batBuoc ? JDialog.DO_NOTHING_ON_CLOSE : JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(buildBanner(), BorderLayout.NORTH);
        add(buildNoiDung(), BorderLayout.CENTER);
    }

    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, SKY_DAM, getWidth(), 0, SKY_GIUA));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 88));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(khoaIcon(), BorderLayout.WEST);

        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel(batBuoc ? "Cần đặt mật khẩu mới" : "Đổi mật khẩu");
        tieuDe.setFont(FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        String phuText = batBuoc
                ? "Xác thực Google thành công — hãy đặt mật khẩu mới"
                : "Tài khoản: " + (taiKhoan.getTenDangNhap() != null ? taiKhoan.getTenDangNhap() : "");
        JLabel phu = new JLabel(phuText);
        phu.setFont(FONT_SMALL);
        phu.setForeground(new Color(255, 255, 255, 220));
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);

        chu.add(tieuDe);
        chu.add(Box.createRigidArea(new Dimension(0, 4)));
        chu.add(phu);
        trai.add(chu, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);
        return banner;
    }

    private JComponent khoaIcon() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawRoundRect(cx - 9, cy - 1, 18, 14, 4, 4);
                g2.drawArc(cx - 6, cy - 13, 12, 15, 0, 180);
                g2.fillOval(cx - 2, cy + 4, 4, 4);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    private JPanel buildNoiDung() {
        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(22, 26, 20, 26));

        if (batBuoc) {
            JPanel canhBao = canhBaoBatBuoc();
            canhBao.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.add(canhBao);
            wrap.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        txtMatKhauCu = oMatKhau();
        txtMatKhauMoi = oMatKhau();
        txtXacNhan = oMatKhau();

        lblThongBao = new JLabel(" ");
        lblThongBao.setFont(FONT_SMALL);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongBao.setBorder(new EmptyBorder(4, 2, 10, 0));

        btnLuu = taoNutChinh(batBuoc ? "Đặt mật khẩu mới và tiếp tục" : "Lưu mật khẩu mới");
        btnLuu.addActionListener(e -> thucHienDoiMatKhau());

        if (!batBuoc) {
            wrap.add(nhanTruong("Mật khẩu hiện tại"));
            wrap.add(txtMatKhauCu);
            wrap.add(Box.createRigidArea(new Dimension(0, 14)));
        }
        wrap.add(nhanTruong("Mật khẩu mới (tối thiểu 6 ký tự)"));
        wrap.add(txtMatKhauMoi);
        wrap.add(Box.createRigidArea(new Dimension(0, 14)));
        wrap.add(nhanTruong("Xác nhận mật khẩu mới"));
        wrap.add(txtXacNhan);
        wrap.add(lblThongBao);
        wrap.add(btnLuu);

        if (!batBuoc) {
            wrap.add(Box.createRigidArea(new Dimension(0, 8)));
            JButton btnHuy = taoNutPhu("Hủy");
            btnHuy.addActionListener(e -> dispose());
            wrap.add(btnHuy);
        }

        wrap.add(Box.createVerticalGlue());
        return wrap;
    }

    private JPanel canhBaoBatBuoc() {
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE0, 0xF2, 0xFE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0x7D, 0xD3, 0xFC));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel text = new JLabel("<html>Đây là lần đầu bạn đăng nhập qua Google (hoặc vừa khôi phục mật khẩu)."
                + "<br>Vì lý do bảo mật, bạn cần đặt mật khẩu mới trước khi tiếp tục.</html>");
        text.setFont(FONT_SMALL);
        text.setForeground(TEXT);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JLabel nhanTruong(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 2, 6, 0));
        return l;
    }

    private JPasswordField oMatKhau() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT);
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pf.setPreferredSize(new Dimension(10, 42));
        pf.setBackground(BG_SOFT);
        pf.setForeground(TEXT);
        pf.setCaretColor(SKY_PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_FOCUS, 2, true),
                        BorderFactory.createEmptyBorder(9, 13, 9, 13)));
                pf.setBackground(Color.WHITE);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
                pf.setBackground(BG_SOFT);
            }
        });
        return pf;
    }

    private JButton taoNutChinh(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(SKY_PRIMARY);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setPreferredSize(new Dimension(10, 44));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) b.setBackground(SKY_DAM);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) b.setBackground(SKY_PRIMARY);
            }
        });
        return b;
    }

    private JButton taoNutPhu(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(Color.WHITE);
        b.setForeground(SKY_PRIMARY);
        b.setOpaque(true);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(10, 42));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBA, 0xE6, 0xFD), 1, true),
                new EmptyBorder(9, 16, 9, 16)));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(0xF0, 0xF9, 0xFF));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(Color.WHITE);
            }
        });
        return b;
    }

    // ===== LOGIC GIU NGUYEN =====

    private void thucHienDoiMatKhau() {
        String matKhauCu = new String(txtMatKhauCu.getPassword());
        String matKhauMoi = new String(txtMatKhauMoi.getPassword());
        String xacNhan = new String(txtXacNhan.getPassword());

        if (!batBuoc && matKhauCu.isEmpty()) {
            baoLoi("Vui lòng nhập mật khẩu hiện tại");
            return;
        }
        if (matKhauMoi.isEmpty() || xacNhan.isEmpty()) {
            baoLoi("Vui lòng nhập đầy đủ mật khẩu mới và xác nhận");
            return;
        }
        if (!batBuoc && !PasswordUtils.matches(matKhauCu, taiKhoan.getMatKhauHash())) {
            baoLoi("Mật khẩu hiện tại không đúng");
            return;
        }
        if (matKhauMoi.length() < 6) {
            baoLoi("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }
        if (!matKhauMoi.equals(xacNhan)) {
            baoLoi("Xác nhận mật khẩu mới không khớp");
            return;
        }
        if (!batBuoc && matKhauMoi.equals(matKhauCu)) {
            baoLoi("Mật khẩu mới phải khác mật khẩu hiện tại");
            return;
        }

        btnLuu.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                taiKhoanService.doiMatKhauVaBoCoBatBuoc(taiKhoan.getMaTK(), matKhauMoi);
                return null;
            }

            @Override
            protected void done() {
                btnLuu.setEnabled(true);
                try {
                    get();
                    taiKhoan.setMatKhauHash(PasswordUtils.hash(matKhauMoi));
                    taiKhoan.setBatBuocDoiMatKhau(false);
                    UIUtils.thongBao(DoiMatKhauDialog.this, "Đã đổi mật khẩu thành công.");
                    dispose();
                    if (khiDoiThanhCong != null) khiDoiThanhCong.run();
                } catch (Exception ex) {
                    baoLoi("Đổi mật khẩu thất bại: " + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void baoLoi(String text) {
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setText(text);
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}