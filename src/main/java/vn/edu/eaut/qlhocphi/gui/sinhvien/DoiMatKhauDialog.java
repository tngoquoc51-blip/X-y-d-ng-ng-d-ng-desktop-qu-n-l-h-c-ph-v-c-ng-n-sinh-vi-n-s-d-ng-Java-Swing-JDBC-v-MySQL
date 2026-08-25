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
 * Hop thoai doi mat khau - dung chung cho moi vai tro (Sinh vien / Ke toan / Admin).
 *
 * THIET KE V2: banner mau gradient + icon ve vector (khong dung emoji - tranh loi
 * hien o vuong trong tren may thieu font) dong bo voi phong cach cac trang khac
 * trong he thong, thay vi khung trang don gian nhu ban dau.
 *
 * 2 CHE DO:
 *  - BINH THUONG (macDinh: batBuoc = false): mo tu menu "Doi mat khau", nguoi dung
 *    phai nhap dung mat khau HIEN TAI moi doi duoc, co nut "Huy" de dong khong doi gi.
 *  - BAT BUOC (batBuoc = true): dung ngay sau khi mot sinh vien xac thuc danh tinh
 *    thanh cong qua Google (luong "Quen mat khau"). Khong yeu cau nhap mat khau cu
 *    (vi danh tinh da duoc Google xac nhan roi), KHONG co nut Huy va KHONG cho dong
 *    bang nut X - bat buoc phai dat mat khau moi thi moi tiep tuc vao he thong duoc.
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

    /** Che do binh thuong (co the Huy, phai nhap mat khau cu). */
    public DoiMatKhauDialog(Window chaMe, TaiKhoan taiKhoan) {
        this(chaMe, taiKhoan, false, null);
    }

    /**
     * @param batBuoc true = che do bat buoc doi mat khau sau khi xac thuc qua Google
     *                (khong can mat khau cu, khong the dong/huy).
     * @param khiDoiThanhCong callback goi ngay sau khi doi mat khau thanh cong va dialog
     *                        dong lai (dung de MainFrame biet ma mo tiep man hinh chinh).
     */
    public DoiMatKhauDialog(Window chaMe, TaiKhoan taiKhoan, boolean batBuoc, Runnable khiDoiThanhCong) {
        super(chaMe, batBuoc ? "Bắt buộc đổi mật khẩu" : "Đổi mật khẩu",
                ModalityType.APPLICATION_MODAL);
        this.taiKhoan = taiKhoan;
        this.batBuoc = batBuoc;
        this.khiDoiThanhCong = khiDoiThanhCong;
        setSize(460, batBuoc ? 500 : 560);
        setMinimumSize(new Dimension(420, batBuoc ? 470 : 520));
        setLocationRelativeTo(chaMe);
        setResizable(false);
        // Che do bat buoc: khong cho dong bang nut X / Alt+F4 - phai dat mat khau moi.
        setDefaultCloseOperation(batBuoc ? JDialog.DO_NOTHING_ON_CLOSE : JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildBanner(), BorderLayout.NORTH);
        add(buildNoiDung(), BorderLayout.CENTER);
    }

    // ================== Banner tren cung ==================

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(khoaIcon(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel(batBuoc ? "Cần đặt mật khẩu mới" : "Đổi mật khẩu");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 19));
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("<html>" + (batBuoc
                ? "Xác thực Google thành công - hãy đặt mật khẩu mới"
                : "Tài khoản: " + taiKhoan.getTenDangNhap()) + "</html>");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);
        return banner;
    }

    /** Icon o khoa ve bang Graphics2D thuan vector - khong dung emoji. */
    private JComponent khoaIcon() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                // Than khoa (hinh chu nhat bo tron)
                g2.drawRoundRect(cx - 9, cy - 2, 18, 15, 5, 5);
                // Quai khoa (nua vong tron phia tren)
                g2.drawArc(cx - 6, cy - 14, 12, 16, 0, 180);
                // Lo khoa
                g2.fillOval(cx - 1, cy + 3, 2, 2);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(54, 54));
        badge.setOpaque(false);
        return badge;
    }

    // ================== Noi dung form ==================

    private JPanel buildNoiDung() {
        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(24, 28, 22, 28));

        if (batBuoc) {
            JPanel canhBao = canhBaoBatBuoc();
            canhBao.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.add(canhBao);
            wrap.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        txtMatKhauCu = oMatKhau();
        txtMatKhauMoi = oMatKhau();
        txtXacNhan = oMatKhau();

        lblThongBao = new JLabel(" ", SwingConstants.LEFT);
        lblThongBao.setFont(UITheme.FONT_BASE);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongBao.setBorder(new EmptyBorder(2, 0, 10, 0));

        btnLuu = UITheme.primaryButton(batBuoc ? "Đặt mật khẩu mới và tiếp tục" : "Lưu mật khẩu mới");
        btnLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
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
            JButton btnHuy = UITheme.secondaryButton("Hủy");
            btnHuy.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnHuy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            btnHuy.addActionListener(e -> dispose());
            wrap.add(btnHuy);
        }

        wrap.add(Box.createVerticalGlue());
        return wrap;
    }

    /** Dai canh bao mau vang, giai thich vi sao bat buoc doi mat khau. */
    private JPanel canhBaoBatBuoc() {
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF, 0xF3, 0xE0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel text = new JLabel("<html>Đây là lần đầu bạn đăng nhập qua Google (hoặc vừa khôi phục mật khẩu)."
                + "<br>Vì lý do bảo mật, bạn cần đặt một mật khẩu mới trước khi tiếp tục.</html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        text.setForeground(UITheme.TEXT_PRIMARY);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JLabel nhanTruong(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 2, 5, 0));
        return l;
    }

    private JPasswordField oMatKhau() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_BASE);
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return pf;
    }

    // ================== Xu ly luu ==================

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
                // Ca 2 truong hop deu goi 1 ham duy nhat: doi mat khau VA tu dong bo co
                // "bat buoc doi mat khau" (neu dang bat) trong cung 1 lan cap nhat CSDL.
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