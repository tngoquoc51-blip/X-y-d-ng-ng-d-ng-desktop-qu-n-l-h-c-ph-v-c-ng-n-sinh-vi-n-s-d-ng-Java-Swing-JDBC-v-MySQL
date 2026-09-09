package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/** Dialog doi mat khau cho 1 tai khoan (Admin thao tac ho, khac voi dialog tu doi mat khau
 *  cua chinh nguoi dung o package sinhvien). Co xac nhan lai mat khau + an/hien mat khau. */
public class AdminDoiMatKhauDialog extends JDialog {

    public AdminDoiMatKhauDialog(Frame owner, String tenDangNhap, Consumer<String> khiXacNhan) {
        super(owner, "", true);
        setSize(400, 340);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 80));
        JLabel iconTron = new JLabel("\uD83D\uDD11", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconTron.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconTron.setForeground(Color.WHITE);
        iconTron.setPreferredSize(new Dimension(44, 44));
        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("Đổi mật khẩu");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Tài khoản: " + tenDangNhap);
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t2.setForeground(new Color(255, 255, 255, 200));
        chuText.add(t1);
        chuText.add(t2);
        banner.add(iconTron, BorderLayout.WEST);
        banner.add(chuText, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(22, 26, 10, 26));

        JPasswordField txtMK1 = oPasswordField();
        JPasswordField txtMK2 = oPasswordField();
        JLabel lblLoi = new JLabel(" ");
        lblLoi.setForeground(UITheme.DANGER);
        lblLoi.setFont(UITheme.FONT_BASE);
        lblLoi.setAlignmentX(Component.LEFT_ALIGNMENT);

        themDong(form, "Mật khẩu mới (tối thiểu 6 ký tự)", txtMK1);
        themDong(form, "Nhập lại mật khẩu mới", txtMK2);
        form.add(lblLoi);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnHuy = UITheme.secondaryButton("Hủy");
        JButton btnXacNhan = UITheme.primaryButton("Xác nhận đổi mật khẩu");
        btnHuy.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> {
            String mk1 = new String(txtMK1.getPassword());
            String mk2 = new String(txtMK2.getPassword());
            if (mk1.length() < 6) {
                lblLoi.setText("Mật khẩu phải có ít nhất 6 ký tự.");
                return;
            }
            if (!mk1.equals(mk2)) {
                lblLoi.setText("Mật khẩu nhập lại không khớp.");
                return;
            }
            khiXacNhan.accept(mk1);
            dispose();
        });
        actions.add(btnHuy);
        actions.add(btnXacNhan);
        add(actions, BorderLayout.SOUTH);
    }

    private JPasswordField oPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(UITheme.FONT_BASE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return f;
    }

    private void themDong(JPanel form, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(l);
        form.add(Box.createRigidArea(new Dimension(0, 5)));
        form.add(field);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
    }
}