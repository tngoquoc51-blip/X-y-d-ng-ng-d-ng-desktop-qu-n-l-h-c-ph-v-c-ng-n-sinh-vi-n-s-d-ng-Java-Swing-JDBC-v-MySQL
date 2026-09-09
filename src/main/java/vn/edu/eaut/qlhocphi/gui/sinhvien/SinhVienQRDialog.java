package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.util.QRCodeSinhVienUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Hien ma QR chua thong tin ca nhan cua 1 sinh vien vua duoc them, cho phep
 *  luu ra file .png (VD: in ra the sinh vien, dan vao ho so giay). */
public class SinhVienQRDialog extends JDialog {
    private BufferedImage anhQR;

    public SinhVienQRDialog(Frame owner, SinhVien sv) {
        super(owner, "", true);
        setSize(420, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 78));
        JLabel t1 = new JLabel("Mã QR hồ sơ sinh viên");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel(sv.getMaSV() + " - " + sv.getHoTen());
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t2.setForeground(new Color(255, 255, 255, 210));
        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        chuText.add(t1);
        chuText.add(t2);
        banner.add(chuText, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        JLabel lblAnh = new JLabel("", SwingConstants.CENTER);
        try {
            String noiDung = QRCodeSinhVienUtils.maHoaNoiDungSinhVien(sv);
            anhQR = QRCodeSinhVienUtils.taoAnhQR(noiDung, 320);
            lblAnh.setIcon(new ImageIcon(anhQR));
        } catch (Exception ex) {
            lblAnh.setText("Không thể tạo mã QR: " + ex.getMessage());
        }
        JPanel giua = new JPanel(new GridBagLayout());
        giua.setBackground(Color.WHITE);
        giua.add(lblAnh);
        add(giua, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnDong = UITheme.secondaryButton("Đóng");
        JButton btnLuu = UITheme.primaryButton("Lưu ảnh QR (.png)");
        btnDong.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luuAnh(sv.getMaSV()));
        actions.add(btnDong);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);
    }

    private void luuAnh(String maSV) {
        if (anhQR == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("qr_sinhvien_" + maSV + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".png")) path += ".png";
        try {
            QRCodeSinhVienUtils.luuAnhQR(anhQR, new File(path));
            UIUtils.thongBao(this, "Đã lưu mã QR: " + path);
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Lưu thất bại: " + ex.getMessage());
        }
    }
}