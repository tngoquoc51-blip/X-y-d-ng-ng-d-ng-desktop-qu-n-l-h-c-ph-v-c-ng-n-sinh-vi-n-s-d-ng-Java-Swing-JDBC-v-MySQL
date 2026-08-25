package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.OtpService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Dialog "Quen mat khau" danh cho cong Sinh vien - luong 2 buoc:
 *  1. Nhap dung Gmail da duoc Admin lien ket voi tai khoan -> bam "Gui ma xac nhan".
 *     He thong gui 1 email chua ma 6 so toi dung Gmail do (qua OtpService + EmailUtils).
 *  2. Nhap ma 6 so vua nhan duoc trong hop thu -> bam "Xac nhan". Neu dung va con han,
 *     tai khoan duoc dat 1 mat khau tam ngau nhien + bat co "bat buoc doi mat khau",
 *     dialog nay dong lai va goi callback de man hinh dang nhap mo tiep dialog
 *     DoiMatKhauDialog(batBuoc=true) - dung mat khau moi tu do, chua the vao he
 *     thong duoc cho toi khi dat xong.
 */
public class QuenMatKhauDialog extends JDialog {
    private final OtpService otpService = new OtpService();
    private final Consumer<TaiKhoan> khiThanhCong;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardBox = new JPanel(cardLayout);

    private JTextField txtGmail;
    private JLabel lblLoiB1;
    private JButton btnGuiMa;

    private JLabel lblGuiToi;
    private JTextField txtMa;
    private JLabel lblLoiB2;
    private JButton btnXacNhan;

    public QuenMatKhauDialog(Window chaMe, Consumer<TaiKhoan> khiThanhCong) {
        super(chaMe, "Quên mật khẩu", ModalityType.APPLICATION_MODAL);
        this.khiThanhCong = khiThanhCong;
        setSize(460, 480);
        setMinimumSize(new Dimension(420, 460));
        setLocationRelativeTo(chaMe);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildBanner(), BorderLayout.NORTH);

        cardBox.setOpaque(false);
        cardBox.add(buildBuoc1(), "b1");
        cardBox.add(buildBuoc2(), "b2");
        add(cardBox, BorderLayout.CENTER);
    }

    // ================== Banner ==================

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(thuIcon(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Quên mật khẩu");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 19));
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Xác minh qua Gmail đã liên kết với tài khoản");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);
        return banner;
    }

    /** Icon phong bi thu ve bang Graphics2D thuan vector - khong dung emoji. */
    private JComponent thuIcon() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawRoundRect(cx - 12, cy - 9, 24, 18, 4, 4);
                g2.drawLine(cx - 12, cy - 8, cx, cy + 2);
                g2.drawLine(cx + 12, cy - 8, cx, cy + 2);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(54, 54));
        badge.setOpaque(false);
        return badge;
    }

    // ================== Buoc 1: nhap Gmail ==================

    private JPanel buildBuoc1() {
        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(24, 28, 22, 28));

        JLabel moTa = new JLabel("<html>Nhập đúng Gmail đã được Admin liên kết với tài khoản sinh viên của bạn. Hệ thống sẽ gửi mã xác nhận 6 số tới Gmail này.</html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel nhan = nhanTruong("Gmail đã liên kết");
        txtGmail = oTruong();

        lblLoiB1 = new JLabel(" ");
        lblLoiB1.setFont(UITheme.FONT_BASE);
        lblLoiB1.setForeground(UITheme.DANGER);
        lblLoiB1.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblLoiB1.setBorder(new EmptyBorder(8, 0, 10, 0));

        btnGuiMa = UITheme.primaryButton("Gửi mã xác nhận");
        btnGuiMa.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuiMa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnGuiMa.addActionListener(e -> guiMa());

        JButton btnHuy = UITheme.secondaryButton("Hủy");
        btnHuy.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHuy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnHuy.addActionListener(e -> dispose());

        wrap.add(moTa);
        wrap.add(nhan);
        wrap.add(txtGmail);
        wrap.add(lblLoiB1);
        wrap.add(btnGuiMa);
        wrap.add(Box.createRigidArea(new Dimension(0, 8)));
        wrap.add(btnHuy);
        wrap.add(Box.createVerticalGlue());
        return wrap;
    }

    private void guiMa() {
        String email = txtGmail.getText().trim();
        if (email.isEmpty()) {
            baoLoiB1("Vui lòng nhập Gmail");
            return;
        }
        btnGuiMa.setEnabled(false);
        lblLoiB1.setForeground(UITheme.TEXT_MUTED);
        lblLoiB1.setText("Đang gửi mã...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String loi;

            @Override
            protected Void doInBackground() {
                try {
                    otpService.guiMaXacNhan(email);
                } catch (Exception ex) {
                    loi = rootMessage(ex);
                }
                return null;
            }

            @Override
            protected void done() {
                btnGuiMa.setEnabled(true);
                if (loi != null) {
                    baoLoiB1(loi);
                    return;
                }
                lblLoiB1.setText(" ");
                lblGuiToi.setText("<html>Đã gửi mã xác nhận tới<br><b>" + email + "</b></html>");
                txtMa.setText("");
                lblLoiB2.setText(" ");
                cardLayout.show(cardBox, "b2");
            }
        };
        worker.execute();
    }

    private void baoLoiB1(String text) {
        lblLoiB1.setForeground(UITheme.DANGER);
        lblLoiB1.setText("<html>" + text.replace("\n", "<br>") + "</html>");
    }

    // ================== Buoc 2: nhap ma xac nhan ==================

    private JPanel buildBuoc2() {
        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(24, 28, 22, 28));

        lblGuiToi = new JLabel(" ");
        lblGuiToi.setFont(UITheme.FONT_BASE);
        lblGuiToi.setForeground(UITheme.TEXT_MUTED);
        lblGuiToi.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGuiToi.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel nhan = nhanTruong("Mã xác nhận (6 số)");
        txtMa = oTruong();
        txtMa.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtMa.setHorizontalAlignment(SwingConstants.CENTER);

        lblLoiB2 = new JLabel(" ");
        lblLoiB2.setFont(UITheme.FONT_BASE);
        lblLoiB2.setForeground(UITheme.DANGER);
        lblLoiB2.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblLoiB2.setBorder(new EmptyBorder(8, 0, 10, 0));

        btnXacNhan = UITheme.primaryButton("Xác nhận và đăng nhập");
        btnXacNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnXacNhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnXacNhan.addActionListener(e -> xacNhan());

        JButton btnGuiLai = UITheme.secondaryButton("Gửi lại mã");
        btnGuiLai.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuiLai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnGuiLai.addActionListener(e -> guiMa());

        JButton btnDoiGmail = new JButton("Nhập lại Gmail khác");
        btnDoiGmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDoiGmail.setBorderPainted(false);
        btnDoiGmail.setContentAreaFilled(false);
        btnDoiGmail.setForeground(UITheme.PRIMARY);
        btnDoiGmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDoiGmail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDoiGmail.setHorizontalAlignment(SwingConstants.LEFT);
        btnDoiGmail.addActionListener(e -> cardLayout.show(cardBox, "b1"));

        wrap.add(lblGuiToi);
        wrap.add(nhan);
        wrap.add(txtMa);
        wrap.add(lblLoiB2);
        wrap.add(btnXacNhan);
        wrap.add(Box.createRigidArea(new Dimension(0, 8)));
        wrap.add(btnGuiLai);
        wrap.add(Box.createRigidArea(new Dimension(0, 6)));
        wrap.add(btnDoiGmail);
        wrap.add(Box.createVerticalGlue());
        return wrap;
    }

    private void xacNhan() {
        String email = txtGmail.getText().trim();
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()) {
            baoLoiB2("Vui lòng nhập mã xác nhận");
            return;
        }
        btnXacNhan.setEnabled(false);

        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<>() {
            private String loi;

            @Override
            protected TaiKhoan doInBackground() {
                try {
                    return otpService.xacNhanMaVaDatLaiMatKhau(email, ma);
                } catch (Exception ex) {
                    loi = rootMessage(ex);
                    return null;
                }
            }

            @Override
            protected void done() {
                btnXacNhan.setEnabled(true);
                TaiKhoan tk = get2();
                if (tk == null) {
                    baoLoiB2(loi != null ? loi : "Xác nhận thất bại");
                    return;
                }
                dispose();
                khiThanhCong.accept(tk);
            }

            private TaiKhoan get2() {
                try {
                    return get();
                } catch (Exception ex) {
                    return null;
                }
            }
        };
        worker.execute();
    }

    private void baoLoiB2(String text) {
        lblLoiB2.setForeground(UITheme.DANGER);
        lblLoiB2.setText("<html>" + text.replace("\n", "<br>") + "</html>");
    }

    // ================== Dung chung ==================

    private JLabel nhanTruong(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 2, 5, 0));
        return l;
    }

    private JTextField oTruong() {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_BASE);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}