package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Form them/sua tai khoan he thong - thiet ke lai theo phong cach dashboard hien dai:
 *  banner gradient, o nhap bo goc co icon, vai tro chon bang 3 "chip" mau rieng biet
 *  (thay vi combo box tho). Khi them moi: co o nhap mat khau (an/hien duoc). Khi sua:
 *  khong doi mat khau o day (dung nut "Doi mat khau" rieng ngoai bang). */
public class TaiKhoanFormDialog extends JDialog {

    public interface ThemListener {
        void onThem(String tenDangNhap, String matKhau, String hoTen, VaiTro vaiTro, String maSV, String googleEmail);
    }

    public interface SuaListener {
        void onSua(int maTK, String hoTen, VaiTro vaiTro, String maSV, String googleEmail, boolean trangThai);
    }

    private VaiTro vaiTroDangChon = VaiTro.PHONGDAOTAO;
    private JPanel[] chipVaiTro = new JPanel[4];
    private JLabel[] chuChipVaiTro = new JLabel[4];
    private JPanel oMaSV;
    private boolean matKhauDangHien = false;
    private JPasswordField txtMatKhau;
    private JButton btnMatKhauEye;

    public TaiKhoanFormDialog(Frame owner, TaiKhoan suaTK, ThemListener themListener, SuaListener suaListener) {
        super(owner, "", true);
        boolean laSua = suaTK != null;
        setUndecorated(false);
        setSize(480, laSua ? 620 : 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(buildBanner(laSua ? "Sửa tài khoản" : "Thêm tài khoản mới",
                laSua ? "Cập nhật thông tin tài khoản " + suaTK.getTenDangNhap() : "Tạo tài khoản đăng nhập mới cho hệ thống",
                laSua ? "\u270F\uFE0F" : "\u2795"), BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(22, 26, 10, 26));

        JTextField txtTenDangNhap = oTextField();
        txtMatKhau = new JPasswordField();
        styleField(txtMatKhau);
        JTextField txtHoTen = oTextField();
        JTextField txtMaSV = oTextField();
        JTextField txtGmail = oTextField();
        JCheckBox chkHoatDong = new JCheckBox("Tài khoản đang hoạt động");
        chkHoatDong.setOpaque(false);
        chkHoatDong.setFont(UITheme.FONT_BASE);
        chkHoatDong.setSelected(true);
        chkHoatDong.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (laSua) {
            txtTenDangNhap.setText(suaTK.getTenDangNhap());
            txtTenDangNhap.setEditable(false);
            txtTenDangNhap.setBackground(new Color(0xF3, 0xF4, 0xFB));
            txtHoTen.setText(suaTK.getHoTen());
            txtMaSV.setText(suaTK.getMaSV());
            txtGmail.setText(suaTK.getGoogleEmail());
            chkHoatDong.setSelected(suaTK.isTrangThai());
            vaiTroDangChon = suaTK.getVaiTro();
        }

        themDong(form, "Tên đăng nhập", txtTenDangNhap);
        if (!laSua) {
            JPanel oMK = new JPanel(new BorderLayout(6, 0));
            oMK.setOpaque(false);
            oMK.setAlignmentX(Component.LEFT_ALIGNMENT);
            oMK.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btnMatKhauEye = new JButton("\uD83D\uDC41");
            btnMatKhauEye.setBorderPainted(false);
            btnMatKhauEye.setContentAreaFilled(false);
            btnMatKhauEye.setFocusPainted(false);
            btnMatKhauEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnMatKhauEye.addActionListener(e -> toggleHienMatKhau());
            oMK.add(txtMatKhau, BorderLayout.CENTER);
            oMK.add(btnMatKhauEye, BorderLayout.EAST);
            themDong(form, "Mật khẩu (tối thiểu 6 ký tự)", oMK);
        }
        themDong(form, "Họ tên", txtHoTen);

        // ===== Vai tro: 4 "chip" mau rieng biet, thay cho combo box =====
        JLabel lblVaiTro = nhanForm("Vai trò");
        lblVaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblVaiTro);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel hangChip = new JPanel(new GridLayout(2, 2, 8, 8));
        hangChip.setOpaque(false);
        hangChip.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangChip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        hangChip.add(taoChipVaiTro(0, "Admin Hệ Thống", VaiTro.ADMIN, UITheme.TEXT_VIOLET));
        hangChip.add(taoChipVaiTro(1, "Phòng Đào Tạo", VaiTro.PHONGDAOTAO, UITheme.PRIMARY));
        hangChip.add(taoChipVaiTro(2, "Kế Toán", VaiTro.KETOAN, UITheme.WARNING));
        hangChip.add(taoChipVaiTro(3, "Sinh Viên", VaiTro.SINHVIEN, UITheme.TEXT_GREEN));
        form.add(hangChip);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        capNhatChipVaiTro();

        oMaSV = themDong(form, "Mã sinh viên (chỉ cần nếu Vai trò = Sinh viên)", txtMaSV);
        themDong(form, "Gmail liên kết (không bắt buộc)", txtGmail);
        oMaSV.setVisible(vaiTroDangChon == VaiTro.SINHVIEN);

        if (laSua) {
            form.add(chkHoatDong);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setOpaque(true);
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnHuy = UITheme.secondaryButton("Hủy");
        JButton btnLuu = UITheme.primaryButton(laSua ? "Lưu thay đổi" : "Tạo tài khoản");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            String maSV = txtMaSV.getText().trim();
            String gmail = txtGmail.getText().trim();

            if (laSua) {
                suaListener.onSua(suaTK.getMaTK(), hoTen, vaiTroDangChon,
                        maSV.isEmpty() ? null : maSV, gmail.isEmpty() ? null : gmail, chkHoatDong.isSelected());
            } else {
                String tenDangNhap = txtTenDangNhap.getText().trim();
                String matKhau = new String(txtMatKhau.getPassword());
                themListener.onThem(tenDangNhap, matKhau, hoTen, vaiTroDangChon,
                        maSV.isEmpty() ? null : maSV, gmail.isEmpty() ? null : gmail);
            }
            dispose();
        });
        actions.add(btnHuy);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);
    }

    private void toggleHienMatKhau() {
        matKhauDangHien = !matKhauDangHien;
        txtMatKhau.setEchoChar(matKhauDangHien ? (char) 0 : '\u2022');
        btnMatKhauEye.setText(matKhauDangHien ? "\uD83D\uDE48" : "\uD83D\uDC41");
    }

    /** Banner gradient tren dau dialog, dong bo voi cac man hinh chinh (icon tron + tieu de + mo ta). */
    private JPanel buildBanner(String tieuDe, String moTa, String icon) {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 84));

        JLabel iconTron = new JLabel(icon, SwingConstants.CENTER) {
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
        iconTron.setPreferredSize(new Dimension(46, 46));

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTieuDe.setForeground(Color.WHITE);
        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMoTa.setForeground(new Color(255, 255, 255, 200));
        chuText.add(lblTieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 2)));
        chuText.add(lblMoTa);

        banner.add(iconTron, BorderLayout.WEST);
        banner.add(chuText, BorderLayout.CENTER);
        return banner;
    }

    /** 1 "chip" vai tro: bo goc, doi mau nen dam/nhat theo trang thai dang chon/khong. Bam vao de chon. */
    private JPanel taoChipVaiTro(int index, String nhan, VaiTro vaiTro, Color mauRieng) {
        JPanel chip = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean dangChon = vaiTroDangChon == vaiTro;
                g2.setColor(dangChon ? mauRieng : new Color(0xF3, 0xF4, 0xFB));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                if (!dangChon) {
                    g2.setColor(UITheme.BORDER);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                }
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel chu = new JLabel(nhan);
        chu.setFont(UITheme.FONT_BOLD);
        chip.add(chu);
        chip.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                vaiTroDangChon = vaiTro;
                oMaSV.setVisible(vaiTro == VaiTro.SINHVIEN);
                capNhatChipVaiTro();
            }
        });

        chipVaiTro[index] = chip;
        chuChipVaiTro[index] = chu;
        return chip;
    }

    private void capNhatChipVaiTro() {
        VaiTro[] thuTu = {VaiTro.ADMIN, VaiTro.PHONGDAOTAO, VaiTro.KETOAN, VaiTro.SINHVIEN};
        for (int i = 0; i < 4; i++) {
            boolean dangChon = vaiTroDangChon == thuTu[i];
            chuChipVaiTro[i].setForeground(dangChon ? Color.WHITE : UITheme.TEXT_MUTED);
            chipVaiTro[i].repaint();
        }
    }

    private JTextField oTextField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(UITheme.FONT_BASE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private JLabel nhanForm(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JPanel themDong(JPanel form, String label, JComponent field) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = nhanForm(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        wrap.add(l);
        wrap.add(Box.createRigidArea(new Dimension(0, 5)));
        wrap.add(field);

        form.add(wrap);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        return wrap;
    }
}