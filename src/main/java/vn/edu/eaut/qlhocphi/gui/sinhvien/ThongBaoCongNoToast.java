package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Toast (thong bao noi, tu bien mat) hien thi tong cong no hoc phi ngay sau khi
 * Sinh vien dang nhap thanh cong va con no > 0. Tu dong dong sau 5 giay, co
 * thanh dem nguoc chay o day va nut "x" de dong som neu muon.
 *
 * Cach dung (goi 1 lan moi phien dang nhap, sau khi da co du lieu cong no that su):
 *   Window chaMe = SwingUtilities.getWindowAncestor(motComponentNaoDoTrongMainFrame);
 *   new ThongBaoCongNoToast(chaMe, hoTenSV, tongConNo, soHoaDonChuaDong).hienThi();
 */
public class ThongBaoCongNoToast extends JWindow {
    private static final int RONG = 360;
    private static final int CAO = 128;
    private static final int THOI_GIAN_HIEN_MS = 5000;
    private static final int BUOC_CAP_NHAT_MS = 40;

    private final Timer timerDemNguoc;
    private float phanTramConLai = 1f;

    public ThongBaoCongNoToast(Window chaMe, String hoTenSV, BigDecimal tongConNo, int soHoaDonChuaDong) {
        super(chaMe);
        setSize(RONG, CAO);
        capNhatViTri(chaMe);

        boolean hoTroTrongSuot = false;
        try {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            hoTroTrongSuot = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
        } catch (Exception ignored) {
            // Neu moi truong khong ho tro trong suot, toast van hien binh thuong (chi khong bo tron duoc goc ngoai cung)
        }
        if (hoTroTrongSuot) {
            setBackground(new Color(0, 0, 0, 0));
        }

        ThePanel the = new ThePanel(hoTenSV, tongConNo, soHoaDonChuaDong);
        setContentPane(the);

        timerDemNguoc = new Timer(BUOC_CAP_NHAT_MS, e -> {
            phanTramConLai -= (float) BUOC_CAP_NHAT_MS / THOI_GIAN_HIEN_MS;
            if (phanTramConLai <= 0f) {
                dongLai();
            } else {
                the.repaint();
            }
        });
    }

    private void capNhatViTri(Window chaMe) {
        if (chaMe != null && chaMe.isShowing()) {
            Insets insets = chaMe.getInsets();
            int x = chaMe.getX() + chaMe.getWidth() - RONG - 24;
            int y = chaMe.getY() + insets.top + 20;
            setLocation(Math.max(x, 0), Math.max(y, 0));
        } else {
            Dimension man = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(man.width - RONG - 40, 60);
        }
    }

    /** Hien thi toast va bat dau dem nguoc tu dong dong sau 5 giay. */
    public void hienThi() {
        setVisible(true);
        timerDemNguoc.start();
    }

    private void dongLai() {
        timerDemNguoc.stop();
        dispose();
    }

    /** Noi dung toast: icon canh bao, so tien no, ghi chu so hoa don, thanh dem nguoc o day. */
    private class ThePanel extends JPanel {
        private static final int BO_GOC = 16;

        ThePanel(String hoTenSV, BigDecimal tongConNo, int soHoaDonChuaDong) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 12));

            JPanel noiDung = new JPanel(new BorderLayout(12, 0));
            noiDung.setOpaque(false);

            JLabel lblIcon = new JLabel("\u26A0");
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            lblIcon.setForeground(Color.WHITE);
            lblIcon.setVerticalAlignment(SwingConstants.TOP);
            lblIcon.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
            noiDung.add(lblIcon, BorderLayout.WEST);

            JPanel textBox = new JPanel();
            textBox.setOpaque(false);
            textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));

            JLabel lblChao = new JLabel("Xin chao, " + hoTenSV);
            lblChao.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblChao.setForeground(new Color(255, 255, 255, 190));
            lblChao.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblTieuDe = new JLabel("BAN DANG CON NO HOC PHI");
            lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTieuDe.setForeground(Color.WHITE);
            lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblTieuDe.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

            JLabel lblSoTien = new JLabel(MoneyUtils.format(tongConNo));
            lblSoTien.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblSoTien.setForeground(Color.WHITE);
            lblSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblSoTien.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

            String ghiChu = soHoaDonChuaDong <= 1
                    ? "Ban co 1 hoa don chua dong du. Vui long dong hoc phi dung han."
                    : "Ban co " + soHoaDonChuaDong + " hoa don chua dong du. Vui long dong hoc phi dung han.";
            JLabel lblGhiChu = new JLabel("<html><body style='width:220px'>" + ghiChu + "</body></html>");
            lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblGhiChu.setForeground(new Color(255, 255, 255, 210));
            lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

            textBox.add(lblChao);
            textBox.add(lblTieuDe);
            textBox.add(lblSoTien);
            textBox.add(lblGhiChu);
            noiDung.add(textBox, BorderLayout.CENTER);

            JButton btnDong = new JButton("\u2715");
            btnDong.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnDong.setForeground(new Color(255, 255, 255, 200));
            btnDong.setContentAreaFilled(false);
            btnDong.setBorderPainted(false);
            btnDong.setFocusPainted(false);
            btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDong.setVerticalAlignment(SwingConstants.TOP);
            btnDong.addActionListener(e -> dongLai());
            noiDung.add(btnDong, BorderLayout.EAST);

            add(noiDung, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint nen = new GradientPaint(
                    0, 0, UITheme.DANGER.darker(),
                    getWidth(), getHeight(), UITheme.DANGER);
            g2.setPaint(nen);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 6, BO_GOC, BO_GOC);

            // Thanh dem nguoc mau trang mo o day toast, thu nho dan theo thoi gian con lai
            g2.setColor(new Color(255, 255, 255, 90));
            g2.fillRoundRect(0, getHeight() - 5, getWidth(), 5, 4, 4);
            g2.setColor(Color.WHITE);
            int rongThanh = Math.max(0, Math.round(getWidth() * phanTramConLai));
            g2.fillRoundRect(0, getHeight() - 5, rongThanh, 5, 4, 4);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}