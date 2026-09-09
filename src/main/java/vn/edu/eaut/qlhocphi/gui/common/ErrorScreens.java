package vn.edu.eaut.qlhocphi.gui.common;

import vn.edu.eaut.qlhocphi.config.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bo 4 man hinh loi dung chung cho toan he thong, thay the tinh than cua ma loi
 * web (403/404/500) nhung thiet ke rieng cho ung dung desktop:
 *  - Tai khoan tam khoa (thay cho "brute-force lockout")
 *  - Tu choi truy cap (thay cho 403)
 *  - Khong tim thay du lieu (thay cho 404)
 *  - Loi he thong (thay cho 500)
 */
public class ErrorScreens {
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ErrorScreens() {}

    // ================== 1. TAI KHOAN TAM KHOA (mau cam) ==================
    public static void hienKhoaTaiKhoan(Component parent, LocalDateTime khoaDenLuc) {
        String noiDung = "Tài khoản của bạn đã bị tạm khóa do nhập sai mật khẩu quá nhiều lần.\n"
                + "Vui lòng thử lại sau " + (khoaDenLuc != null ? khoaDenLuc.format(HM) : "ít phút");
        hienDialog(parent, "\uD83D\uDD12", new Color(0xF5, 0x9E, 0x0B), new Color(0xFE, 0xF3, 0xC7),
                "Tài khoản tạm khóa", noiDung, null);
    }

    // ================== 2. TU CHOI TRUY CAP (mau do) - thay cho 403 ==================
    public static void hienTuChoiTruyCap(Component parent, String hanhDong) {
        String noiDung = "Bạn không có quyền thực hiện thao tác này"
                + (hanhDong != null && !hanhDong.isBlank() ? ":\n" + hanhDong : ".")
                + "\nNếu cho rằng đây là nhầm lẫn, vui lòng liên hệ Quản trị viên.";
        hienDialog(parent, "\u26D4", new Color(0xDC, 0x26, 0x26), new Color(0xFE, 0xE2, 0xE2),
                "Truy cập bị từ chối", noiDung, null);
    }

    // ================== 3. KHONG TIM THAY DU LIEU (mau xam-xanh) - thay cho 404 ==================
    public static void hienKhongTimThay(Component parent, String doiTuong) {
        String noiDung = "Không tìm thấy " + (doiTuong == null || doiTuong.isBlank() ? "dữ liệu phù hợp" : doiTuong)
                + ".\nVui lòng kiểm tra lại thông tin đã nhập.";
        hienDialog(parent, "\uD83D\uDD0D", new Color(0x64, 0x74, 0x8B), new Color(0xF1, 0xF5, 0xF9),
                "Không tìm thấy dữ liệu", noiDung, null);
    }

    // ================== 4. LOI HE THONG (mau do dam) - thay cho 500 ==================
    public static void hienLoiHeThong(Component parent, Throwable loi) {
        String chiTiet = layStackTrace(loi);
        hienDialog(parent, "\u26A0\uFE0F", new Color(0x7F, 0x1D, 0x1D), new Color(0xFE, 0xE2, 0xE2),
                "Đã xảy ra lỗi hệ thống",
                "Ứng dụng gặp sự cố ngoài dự kiến. Bạn có thể tiếp tục sử dụng,\n"
                        + "nếu lỗi lặp lại vui lòng chụp màn hình gửi cho Quản trị viên.",
                chiTiet);
    }

    private static String layStackTrace(Throwable loi) {
        if (loi == null) return null;
        StringWriter sw = new StringWriter();
        loi.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // ================== KHUNG DIALOG DUNG CHUNG ==================
    private static void hienDialog(Component parent, String emoji, Color mauDam, Color mauNhat,
                                   String tieuDe, String noiDung, String chiTietKyThuat) {
        Window chaMe = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(chaMe, tieuDe, chaMe == null
                ? Dialog.ModalityType.MODELESS : Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(chiTietKyThuat != null ? 480 : 420, chiTietKyThuat != null ? 360 : 240);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JPanel noiDungPanel = new JPanel();
        noiDungPanel.setLayout(new BoxLayout(noiDungPanel, BoxLayout.Y_AXIS));
        noiDungPanel.setBorder(new EmptyBorder(28, 28, 20, 28));
        noiDungPanel.setBackground(Color.WHITE);

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNhat);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        icon.setPreferredSize(new Dimension(64, 64));
        icon.setMaximumSize(new Dimension(64, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTieuDe = new JLabel(tieuDe, SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTieuDe.setForeground(mauDam);
        lblTieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTieuDe.setBorder(new EmptyBorder(14, 0, 8, 0));

        JLabel lblNoiDung = new JLabel("<html><div style='text-align:center;width:320px'>"
                + noiDung.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        lblNoiDung.setFont(UITheme.FONT_BASE);
        lblNoiDung.setForeground(UITheme.TEXT_MUTED);
        lblNoiDung.setAlignmentX(Component.CENTER_ALIGNMENT);

        noiDungPanel.add(icon);
        noiDungPanel.add(lblTieuDe);
        noiDungPanel.add(lblNoiDung);

        if (chiTietKyThuat != null) {
            noiDungPanel.add(Box.createRigidArea(new Dimension(0, 14)));
            JTextArea taChiTiet = new JTextArea(chiTietKyThuat);
            taChiTiet.setEditable(false);
            taChiTiet.setFont(new Font("Consolas", Font.PLAIN, 11));
            taChiTiet.setForeground(new Color(0x44, 0x44, 0x44));
            taChiTiet.setBackground(new Color(0xF5, 0xF5, 0xF5));
            JScrollPane scroll = new JScrollPane(taChiTiet);
            scroll.setPreferredSize(new Dimension(420, 110));
            scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
            noiDungPanel.add(scroll);
        }

        noiDungPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        JButton btnDong = new JButton("Đã hiểu");
        btnDong.setFont(UITheme.FONT_BOLD);
        btnDong.setForeground(Color.WHITE);
        btnDong.setBackground(mauDam);
        btnDong.setBorderPainted(false);
        btnDong.setFocusPainted(false);
        btnDong.setOpaque(true);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDong.setMaximumSize(new Dimension(160, 40));
        btnDong.addActionListener(e -> dialog.dispose());
        noiDungPanel.add(btnDong);

        dialog.setContentPane(noiDungPanel);
        dialog.setVisible(true);
    }
}