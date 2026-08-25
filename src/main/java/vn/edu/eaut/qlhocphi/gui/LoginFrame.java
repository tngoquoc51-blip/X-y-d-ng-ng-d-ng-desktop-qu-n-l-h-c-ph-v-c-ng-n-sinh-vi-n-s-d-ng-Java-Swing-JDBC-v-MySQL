package vn.edu.eaut.qlhocphi.gui;

import vn.edu.eaut.qlhocphi.bus.AuthService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * Man hinh dang nhap - toan bo nen la gradient xanh, ben tren la 2 the trang noi:
 *  - The trai: gioi thieu + nut "Tra cuu cong no" (khong can dang nhap)
 *  - The phai: form dang nhap
 * Thao tac xac thuc (goi CSDL) duoc chay bang SwingWorker de khong lam treo giao dien.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;
    private JLabel lblThongBao;

    public LoginFrame() {
        setTitle("Dang nhap - He thong quan ly hoc phi");
        setSize(1040, 680);
        setMinimumSize(new Dimension(820, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setContentPane(buildNenGradient());
    }

    /** Nen toan man hinh: gradient xanh dam -> xanh sang, chua tieu de + 2 the trang. */
    private JPanel buildNenGradient() {
        JPanel nen = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0D, 0x25, 0x66),
                        getWidth(), getHeight(), UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                // KHONG goi super.paintComponent(g) o day - vi panel opaque se ve de len gradient vua ve.
            }
        };
        nen.setOpaque(false);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));

        noiDung.add(buildTieuDeTren());
        noiDung.add(Box.createRigidArea(new Dimension(0, 26)));
        noiDung.add(buildHang2The());
        noiDung.add(Box.createRigidArea(new Dimension(0, 24)));
        noiDung.add(buildHangTinhNang());

        nen.add(noiDung, new GridBagConstraints());
        return nen;
    }

    // ================== Tieu de tren cung (chu dam, tren nen xanh) ==================

    private JPanel buildTieuDeTren() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("\uD83C\uDF93", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        icon.setPreferredSize(new Dimension(56, 56));
        icon.setMaximumSize(new Dimension(56, 56));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("HE THONG QUAN LY HOC PHI VA CONG NO SINH VIEN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(14, 0, 6, 0));

        JLabel lblSub = new JLabel("Tra cuu cong no va thanh toan hoc phi nhanh chong, chinh xac", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(new Color(0xDC, 0xE7, 0xFF));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(icon);
        box.add(lblTitle);
        box.add(lblSub);
        return box;
    }

    // ================== 2 the trang nam ngang ==================

    private JPanel buildHang2The() {
        JPanel hang = new JPanel();
        hang.setOpaque(false);
        hang.setLayout(new BoxLayout(hang, BoxLayout.X_AXIS));

        JPanel theTraCuu = theTrang(460);
        theTraCuu.setLayout(new BoxLayout(theTraCuu, BoxLayout.Y_AXIS));
        dienNoiDungTraCuu(theTraCuu);

        JPanel theDangNhap = theTrang(360);
        theDangNhap.setLayout(new BoxLayout(theDangNhap, BoxLayout.Y_AXIS));
        dienNoiDungDangNhap(theDangNhap);

        hang.add(theTraCuu);
        hang.add(Box.createRigidArea(new Dimension(24, 0)));
        hang.add(theDangNhap);
        return hang;
    }

    /** Tao 1 "the" (card) trang, bo goc, co do bong nhe, do rong co dinh. */
    private JPanel theTrang(int rong) {
        JPanel the = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.dispose();
            }
        };
        the.setOpaque(false);
        the.setBorder(new EmptyBorder(26, 30, 26, 30));
        the.setPreferredSize(new Dimension(rong, 380));
        the.setMaximumSize(new Dimension(rong, 380));
        return the;
    }

    private void dienNoiDungTraCuu(JPanel the) {
        JLabel iconTron = iconTronMau("\uD83D\uDC65", UITheme.PRIMARY, 40);
        iconTron.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tieuDe = new JLabel("DANH CHO SINH VIEN TRA CUU");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        tieuDe.setBorder(new EmptyBorder(12, 0, 6, 0));

        JLabel moTa = new JLabel("<html>Khong can tai khoan - chi can nhap Ma SV<br>de xem cong no va thanh toan truc tuyen.</html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(0, 0, 18, 0));

        the.add(iconTron);
        the.add(tieuDe);
        the.add(moTa);
        the.add(dongBuoc("1", "Nhap Ma sinh vien de tra cuu"));
        the.add(Box.createRigidArea(new Dimension(0, 10)));
        the.add(dongBuoc("2", "Xem danh sach hoa don va cong no"));
        the.add(Box.createRigidArea(new Dimension(0, 10)));
        the.add(dongBuoc("3", "Thanh toan truc tuyen (mo phong)"));
        the.add(Box.createVerticalGlue());
        the.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnTraCuu = UITheme.primaryButton("DANG NHAP SINH VIEN");
        btnTraCuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTraCuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnTraCuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTraCuu.addActionListener(e -> new StudentLoginFrame(this).setVisible(true));
        the.add(btnTraCuu);
    }

    private void dienNoiDungDangNhap(JPanel the) {
        JLabel iconTron = iconTronMau("\uD83D\uDD12", UITheme.PRIMARY, 52);
        iconTron.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("DANG NHAP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(14, 0, 22, 0));

        txtTenDangNhap = UIUtils.textField(18);

        txtMatKhau = new JPasswordField(18);
        txtMatKhau.setFont(UITheme.FONT_BASE);
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        lblThongBao = new JLabel(" ", SwingConstants.CENTER);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setFont(UITheme.FONT_BASE);
        lblThongBao.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnDangNhap = UITheme.primaryButton("DANG NHAP");
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lblTenDangNhap = boldLabel("Ten dang nhap");
        lblTenDangNhap.setHorizontalAlignment(SwingConstants.CENTER);
        lblTenDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMatKhau = boldLabel("Mat khau");
        lblMatKhau.setHorizontalAlignment(SwingConstants.CENTER);
        lblMatKhau.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtTenDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtMatKhau.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Bam Enter o o Ten dang nhap -> nhay xuong o Mat khau (khong submit form)
        txtTenDangNhap.addActionListener(e -> txtMatKhau.requestFocusInWindow());

        the.add(iconTron);
        the.add(lblTitle);
        the.add(lblTenDangNhap);
        the.add(Box.createRigidArea(new Dimension(0, 6)));
        the.add(txtTenDangNhap);
        the.add(Box.createRigidArea(new Dimension(0, 16)));
        the.add(lblMatKhau);
        the.add(Box.createRigidArea(new Dimension(0, 6)));
        the.add(txtMatKhau);
        the.add(Box.createRigidArea(new Dimension(0, 10)));
        the.add(lblThongBao);
        the.add(Box.createVerticalGlue());
        the.add(btnDangNhap);

        btnDangNhap.addActionListener(e -> thucHienDangNhap());
        getRootPane().setDefaultButton(btnDangNhap);
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
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
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, kichThuoc / 2));
        icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(kichThuoc, kichThuoc));
        icon.setMaximumSize(new Dimension(kichThuoc, kichThuoc));
        return icon;
    }

    private JPanel dongBuoc(String so, String noiDung) {
        JPanel dong = new JPanel(new BorderLayout(10, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lblSo = new JLabel(so, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSo.setPreferredSize(new Dimension(24, 24));
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSo.setForeground(Color.WHITE);

        JLabel lblText = new JLabel(noiDung);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblText.setForeground(UITheme.TEXT_PRIMARY);

        dong.add(lblSo, BorderLayout.WEST);
        dong.add(lblText, BorderLayout.CENTER);
        return dong;
    }

    // ================== Hang tinh nang duoi cung (tren nen xanh) ==================

    private JPanel buildHangTinhNang() {
        JPanel hang = new JPanel(new GridLayout(1, 4, 18, 0));
        hang.setOpaque(false);
        hang.setMaximumSize(new Dimension(840, 60));

        hang.add(tinhNang("\uD83D\uDEE1\uFE0F", "Bao mat tuyet doi"));
        hang.add(tinhNang("\uD83D\uDD10", "Thanh toan an toan"));
        hang.add(tinhNang("\uD83C\uDFA7", "Ho tro 24/7"));
        hang.add(tinhNang("\u2705", "Xac nhan nhanh chong"));
        return hang;
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
                    if (tk.isBatBuocDoiMatKhau()) {
                        // Tai khoan dang bi bat co "bat buoc doi mat khau" (vi du: Admin
                        // vua dat lai mat khau, hoac vua khoi phuc qua Google tu lan truoc
                        // nhung chua doi xong) -> bat dialog doi mat khau truoc, chi mo
                        // MainFrame SAU KHI doi xong.
                        vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog dialog =
                                new vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog(
                                        LoginFrame.this, tk, true, () -> {
                                    new MainFrame(tk).setVisible(true);
                                    dispose();
                                });
                        dialog.setVisible(true);
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
}