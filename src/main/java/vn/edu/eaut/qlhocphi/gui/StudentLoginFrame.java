package vn.edu.eaut.qlhocphi.gui;

import vn.edu.eaut.qlhocphi.bus.AuthService;
import vn.edu.eaut.qlhocphi.bus.TaiKhoanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.google.GoogleAuthService;
import vn.edu.eaut.qlhocphi.google.GoogleOAuthConfig;
import vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog;
import vn.edu.eaut.qlhocphi.gui.sinhvien.QuenMatKhauDialog;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.Random;

/**
 * Trang dang nhap RIENG cho Sinh vien - nen gradient xanh navy dam, co diem sang
 * trang tri, 2 the trang: trai la gioi thieu + link tra cuu khong can tai khoan,
 * phai la form dang nhap day du (icon o nhap, an/hien mat khau, quen mat khau,
 * tro giup, dang nhap Google).
 */
public class StudentLoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap, btnMatKhauEye;
    private JLabel lblThongBao;
    private boolean matKhauDangHien = false;

    public StudentLoginFrame(JFrame chaMe) {
        setTitle("Dang nhap Sinh vien - He thong quan ly hoc phi");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(chaMe);
        setLayout(new BorderLayout());
        setContentPane(buildNen());
    }

    private JPanel buildNen() {
        JPanel nen = new JPanel(new BorderLayout()) {
            private final Random rd = new Random(7);
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0A, 0x18, 0x40),
                        getWidth(), getHeight(), new Color(0x1E, 0x3A, 0x8A));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Cham sang trang tri
                g2.setColor(new Color(255, 255, 255, 60));
                for (int i = 0; i < 26; i++) {
                    int x = rd.nextInt(Math.max(getWidth(), 1));
                    int y = rd.nextInt(Math.max(getHeight(), 1));
                    int s = 2 + rd.nextInt(3);
                    g2.fillOval(x, y, s, s);
                }
                g2.dispose();
            }
        };
        nen.setOpaque(true);

        JPanel giua = new JPanel(new GridBagLayout());
        giua.setOpaque(false);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.add(buildHang2The());
        noiDung.add(Box.createRigidArea(new Dimension(0, 24)));
        noiDung.add(buildHangTinhNang());

        giua.add(noiDung, new GridBagConstraints());
        nen.add(giua, BorderLayout.CENTER);
        return nen;
    }

    // ================== 2 the ==================

    private JPanel buildHang2The() {
        JPanel hang = new JPanel();
        hang.setOpaque(false);
        hang.setLayout(new BoxLayout(hang, BoxLayout.X_AXIS));

        JPanel theTrai = theTrang(430);
        theTrai.setLayout(new BoxLayout(theTrai, BoxLayout.Y_AXIS));
        dienNoiDungGioiThieu(theTrai);

        JPanel thePhai = theTrang(380);
        thePhai.setLayout(new BoxLayout(thePhai, BoxLayout.Y_AXIS));
        dienNoiDungDangNhap(thePhai);

        hang.add(theTrai);
        hang.add(Box.createRigidArea(new Dimension(24, 0)));
        hang.add(thePhai);
        return hang;
    }

    private JPanel theTrang(int rong) {
        JPanel the = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.dispose();
            }
        };
        the.setOpaque(false);
        the.setBorder(new EmptyBorder(30, 32, 30, 32));
        the.setPreferredSize(new Dimension(rong, 460));
        the.setMaximumSize(new Dimension(rong, 460));
        return the;
    }

    // ================== The trai: gioi thieu ==================

    private void dienNoiDungGioiThieu(JPanel the) {
        JPanel dongIcon = new JPanel(new BorderLayout(12, 0));
        dongIcon.setOpaque(false);
        dongIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongIcon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        dongIcon.add(iconTronMau("\uD83C\uDF93", UITheme.PRIMARY, 44), BorderLayout.WEST);
        JLabel tieuDe = new JLabel("DANH CHO SINH VIEN");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dongIcon.add(tieuDe, BorderLayout.CENTER);

        JLabel moTa = new JLabel("<html>Dang nhap bang tai khoan duoc cap de xem cong no,<br>hoa don hoc phi va thanh toan truc tuyen.</html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(16, 0, 20, 0));

        the.add(dongIcon);
        the.add(moTa);

        the.add(dongBuoc("1", "Dang nhap bang tai khoan sinh vien"));
        the.add(Box.createRigidArea(new Dimension(0, 12)));
        the.add(dongBuoc("2", "Xem danh sach hoa don va cong no"));
        the.add(Box.createRigidArea(new Dimension(0, 12)));
        the.add(dongBuoc("3", "Thanh toan hoc phi truc tuyen"));

        the.add(Box.createVerticalGlue());

        JButton btnTraCuu = UITheme.secondaryButton("Tra cuu khong can dang nhap");
        btnTraCuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTraCuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnTraCuu.addActionListener(e -> new TraCuuFrame(this).setVisible(true));
        the.add(btnTraCuu);
    }

    private JPanel dongBuoc(String so, String noiDung) {
        JPanel dong = new JPanel(new BorderLayout(12, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        dong.add(iconTronMau(so, UITheme.PRIMARY, 26), BorderLayout.WEST);
        JLabel lblText = new JLabel(noiDung);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblText.setForeground(UITheme.TEXT_PRIMARY);
        dong.add(lblText, BorderLayout.CENTER);
        return dong;
    }

    // ================== The phai: form dang nhap ==================

    private void dienNoiDungDangNhap(JPanel the) {
        JLabel iconTron = iconTronMau("\uD83D\uDD12", UITheme.PRIMARY, 52);
        iconTron.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("DANG NHAP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(12, 0, 20, 0));

        // O ten dang nhap co icon nguoi dung
        txtTenDangNhap = new JTextField();
        txtTenDangNhap.setFont(UITheme.FONT_BASE);
        txtTenDangNhap.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        JPanel oTenDangNhap = oNhapCoIcon("\uD83D\uDC64", txtTenDangNhap, null);
        oTenDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        oTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // O mat khau co icon khoa + nut mat/an
        txtMatKhau = new JPasswordField();
        txtMatKhau.setFont(UITheme.FONT_BASE);
        txtMatKhau.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        btnMatKhauEye = new JButton("\uD83D\uDC41");
        btnMatKhauEye.setBorderPainted(false);
        btnMatKhauEye.setContentAreaFilled(false);
        btnMatKhauEye.setFocusPainted(false);
        btnMatKhauEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMatKhauEye.addActionListener(e -> toggleHienMatKhau());
        JPanel oMatKhau = oNhapCoIcon("\uD83D\uDD12", txtMatKhau, btnMatKhauEye);
        oMatKhau.setAlignmentX(Component.CENTER_ALIGNMENT);
        oMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Hang: Quen mat khau - Tro giup
        JPanel hangLink = new JPanel(new BorderLayout());
        hangLink.setOpaque(false);
        hangLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        hangLink.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JButton lnkQuenMK = lienKet("Quen mat khau?");
        lnkQuenMK.addActionListener(e ->
                new QuenMatKhauDialog(this, this::moTiepSauKhiXacThuc).setVisible(true));
        JButton lnkTroGiup = lienKet("Tro giup");
        lnkTroGiup.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Hotline ho tro: 024 3204 5867\nEmail: hotro@eaut.edu.vn",
                "Tro giup", JOptionPane.INFORMATION_MESSAGE));
        hangLink.add(lnkQuenMK, BorderLayout.WEST);
        hangLink.add(lnkTroGiup, BorderLayout.EAST);

        lblThongBao = new JLabel(" ", SwingConstants.CENTER);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setFont(UITheme.FONT_BASE);
        lblThongBao.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnDangNhap = new JButton("DANG NHAP");
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setBackground(new Color(0x1E, 0x3A, 0x8A));
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setOpaque(true);
        btnDangNhap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnDangNhap.addActionListener(e -> thucHienDangNhap());

        JLabel lblChiaDoi = new JLabel("hoac dang nhap", SwingConstants.CENTER);
        lblChiaDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChiaDoi.setForeground(UITheme.TEXT_MUTED);
        lblChiaDoi.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblChiaDoi.setBorder(new EmptyBorder(14, 0, 10, 0));

        JButton btnGoogle = new JButton("G   Dang nhap voi Google");
        btnGoogle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGoogle.setForeground(Color.WHITE);
        btnGoogle.setBackground(new Color(0xE0, 0x5A, 0x2B));
        btnGoogle.setBorderPainted(false);
        btnGoogle.setFocusPainted(false);
        btnGoogle.setOpaque(true);
        btnGoogle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoogle.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoogle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnGoogle.addActionListener(e -> dangNhapQuaGoogle(btnGoogle));

        txtTenDangNhap.addActionListener(e -> txtMatKhau.requestFocusInWindow());
        getRootPane().setDefaultButton(btnDangNhap);

        the.add(iconTron);
        the.add(lblTitle);
        the.add(oTenDangNhap);
        the.add(Box.createRigidArea(new Dimension(0, 14)));
        the.add(oMatKhau);
        the.add(Box.createRigidArea(new Dimension(0, 8)));
        the.add(hangLink);
        the.add(Box.createRigidArea(new Dimension(0, 8)));
        the.add(lblThongBao);
        the.add(Box.createRigidArea(new Dimension(0, 4)));
        the.add(btnDangNhap);
        the.add(lblChiaDoi);
        the.add(btnGoogle);
    }

    private void toggleHienMatKhau() {
        matKhauDangHien = !matKhauDangHien;
        txtMatKhau.setEchoChar(matKhauDangHien ? (char) 0 : '\u2022');
        btnMatKhauEye.setText(matKhauDangHien ? "\uD83D\uDE48" : "\uD83D\uDC41");
    }

    /** O nhap dang khung bo goc, co icon trai va (tuy chon) nut/icon phai. */
    private JPanel oNhapCoIcon(String emojiTrai, JComponent oNhap, JComponent thanhPhanPhai) {
        JPanel bao = new JPanel(new BorderLayout(8, 0));
        bao.setBackground(Color.WHITE);
        bao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));

        JLabel icon = new JLabel(emojiTrai);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        icon.setForeground(UITheme.TEXT_MUTED);
        bao.add(icon, BorderLayout.WEST);
        bao.add(oNhap, BorderLayout.CENTER);
        if (thanhPhanPhai != null) bao.add(thanhPhanPhai, BorderLayout.EAST);
        return bao;
    }

    private JButton lienKet(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(UITheme.PRIMARY);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel iconTronMau(String emoji, Color mau, int kichThuoc) {
        JLabel icon = new JLabel(emoji, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mau);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.max(11, kichThuoc / 2)));
        icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(kichThuoc, kichThuoc));
        icon.setMaximumSize(new Dimension(kichThuoc, kichThuoc));
        return icon;
    }

    // ================== Hang tinh nang duoi cung ==================

    private JPanel buildHangTinhNang() {
        JPanel thanh = new JPanel(new GridLayout(1, 4, 0, 0));
        thanh.setOpaque(true);
        thanh.setBackground(new Color(0x10, 0x24, 0x55));
        thanh.setBorder(new EmptyBorder(16, 24, 16, 24));
        thanh.setMaximumSize(new Dimension(860, 60));

        thanh.add(tinhNang("\uD83D\uDEE1\uFE0F", "Bao mat tuyet doi"));
        thanh.add(tinhNang("\uD83D\uDD10", "Thanh toan an toan"));
        thanh.add(tinhNang("\uD83C\uDFA7", "Ho tro 24/7"));
        thanh.add(tinhNang("\u2705", "Xac nhan nhanh chong"));
        return thanh;
    }

    private JPanel tinhNang(String emoji, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        JLabel icon = new JLabel(emoji);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        p.add(icon);
        p.add(lbl);
        return p;
    }

    // ================== Xu ly dang nhap ==================

    private void thucHienDangNhap() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            lblThongBao.setText("Vui long nhap day du thong tin");
            return;
        }

        btnDangNhap.setEnabled(false);
        lblThongBao.setForeground(UITheme.TEXT_MUTED);
        lblThongBao.setText("Dang kiem tra...");

        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<>() {
            @Override
            protected TaiKhoan doInBackground() throws SQLException {
                return authService.dangNhap(tenDangNhap, matKhau);
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                try {
                    TaiKhoan tk = get();
                    if (tk == null) {
                        lblThongBao.setForeground(UITheme.DANGER);
                        lblThongBao.setText("Sai ten dang nhap hoac mat khau");
                        return;
                    }
                    new MainFrame(tk).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    lblThongBao.setForeground(UITheme.DANGER);
                    lblThongBao.setText("Khong the ket noi CSDL. Kiem tra cau hinh.");
                }
            }
        };
        worker.execute();
    }

    // ================== Dang nhap / Khoi phuc mat khau qua Google ==================

    /**
     * "Dang nhap bang Google": xac thuc qua Google roi tim tai khoan Sinh vien da
     * duoc Admin gan dung Gmail nay (cot GoogleEmail). Neu tim thay va tai khoan
     * dang o trang thai "bat buoc doi mat khau" (lan dau lien ket / vua khoi phuc),
     * bat dialog doi mat khau truoc khi vao trang chinh; neu khong, vao thang.
     */
    private void dangNhapQuaGoogle(JButton btnGoogle) {
        if (!GoogleOAuthConfig.daCauHinh()) {
            JOptionPane.showMessageDialog(this,
                    "Chua cau hinh Google OAuth Client ID/Secret.\n"
                            + "Mo file GoogleOAuthConfig.java trong package 'google' de xem huong dan.",
                    "Chua cau hinh", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnGoogle.setEnabled(false);
        lblThongBao.setForeground(UITheme.TEXT_MUTED);
        lblThongBao.setText("Dang mo trinh duyet de dang nhap Google...");

        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<>() {
            private String loiHienThi;

            @Override
            protected TaiKhoan doInBackground() {
                try {
                    GoogleAuthService.KetQuaGoogle ketQua = googleAuthService.dangNhap();
                    TaiKhoan tk = taiKhoanService.timTheoGoogleEmail(ketQua.email);
                    if (tk == null) {
                        loiHienThi = "Gmail " + ketQua.email + " chua duoc Admin lien ket voi tai khoan sinh vien nao.\n"
                                + "Vui long lien he Phong Ke toan de duoc lien ket Gmail nay.";
                        return null;
                    }
                    if (!tk.isTrangThai()) {
                        loiHienThi = "Tai khoan sinh vien nay da bi khoa. Vui long lien he Phong Ke toan.";
                        return null;
                    }
                    if (tk.getVaiTro() != VaiTro.SINHVIEN) {
                        loiHienThi = "Gmail nay khong lien ket voi tai khoan sinh vien.";
                        return null;
                    }
                    return tk;
                } catch (Exception ex) {
                    loiHienThi = "Dang nhap Google that bai: " + rootMessage(ex);
                    return null;
                }
            }

            @Override
            protected void done() {
                btnGoogle.setEnabled(true);
                TaiKhoan tk = get2();
                if (tk == null) {
                    lblThongBao.setForeground(UITheme.DANGER);
                    lblThongBao.setText(loiHienThi != null ? loiHienThi : "Dang nhap Google that bai");
                    return;
                }
                lblThongBao.setForeground(UITheme.TEXT_MUTED);
                lblThongBao.setText(" ");
                moTiepSauKhiXacThuc(tk);
            }

            /** Boc get() de khong phai try/catch InterruptedException/ExecutionException o tren. */
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

    /**
     * Sau khi xac thuc Google thanh cong (dang nhap thang hoac khoi phuc mat khau):
     * neu tai khoan dang bi bat "bat buoc doi mat khau" thi mo dialog bat buoc doi
     * truoc, chi mo MainFrame SAU KHI doi mat khau xong; neu khong thi vao thang.
     */
    private void moTiepSauKhiXacThuc(TaiKhoan tk) {
        if (tk.isBatBuocDoiMatKhau()) {
            DoiMatKhauDialog dialog = new DoiMatKhauDialog(this, tk, true, () -> {
                new MainFrame(tk).setVisible(true);
                dispose();
            });
            dialog.setVisible(true);
        } else {
            new MainFrame(tk).setVisible(true);
            dispose();
        }
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}