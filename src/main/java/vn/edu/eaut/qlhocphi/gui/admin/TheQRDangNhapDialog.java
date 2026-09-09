package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.DangNhapQRService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.QrCodeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Man hinh Admin cap/thu hoi "the QR dang nhap" cho 1 tai khoan Sinh vien.
 *  In/luu anh nay dua cho sinh vien - sinh vien dung no de dang nhap nhanh
 *  vao trang cua minh bang cach quet, khong can nho mat khau. */
public class TheQRDangNhapDialog extends JDialog {
    private final DangNhapQRService dangNhapQRService = new DangNhapQRService();
    private final TaiKhoan taiKhoan;
    private BufferedImage anhQR;
    private JLabel lblAnh;
    private JButton btnLuu, btnThuHoi;

    public TheQRDangNhapDialog(Frame owner, TaiKhoan taiKhoan) {
        super(owner, "", true);
        this.taiKhoan = taiKhoan;
        setSize(420, 540);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 80));
        JLabel t1 = new JLabel("Thẻ QR đăng nhập");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel(taiKhoan.getTenDangNhap() + " - " + taiKhoan.getHoTen());
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t2.setForeground(new Color(255, 255, 255, 210));
        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        chuText.add(t1);
        chuText.add(t2);
        banner.add(chuText, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 14));
        giua.setBackground(Color.WHITE);
        giua.setBorder(new EmptyBorder(20, 24, 10, 24));

        JLabel canhBao = new JLabel("<html>Mỗi lần tạo thẻ mới, thẻ cũ (nếu có) sẽ TỰ ĐỘNG bị vô hiệu hóa.<br>Không chia sẻ ảnh này cho người khác ngoài chính sinh viên đó.</html>");
        canhBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        canhBao.setForeground(UITheme.WARNING);
        giua.add(canhBao, BorderLayout.NORTH);

        lblAnh = new JLabel("Bấm \"Tạo thẻ QR mới\" để bắt đầu.", SwingConstants.CENTER);
        lblAnh.setOpaque(true);
        lblAnh.setBackground(new Color(0xF3, 0xF4, 0xFB));
        lblAnh.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        giua.add(lblAnh, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 8));
        actions.setBackground(Color.WHITE);
        actions.setBorder(new EmptyBorder(0, 24, 20, 24));
        JButton btnTaoMoi = UITheme.primaryButton("Tạo thẻ QR mới (thu hồi thẻ cũ)");
        btnTaoMoi.addActionListener(e -> taoTheMoi());

        JPanel hangDuoi = new JPanel(new GridLayout(1, 2, 8, 0));
        hangDuoi.setOpaque(false);
        btnLuu = UITheme.secondaryButton("Lưu ảnh (.png)");
        btnLuu.setEnabled(false);
        btnLuu.addActionListener(e -> luuAnh());
        btnThuHoi = UITheme.dangerButton("Thu hồi thẻ hiện tại");
        btnThuHoi.addActionListener(e -> thuHoiThe());
        hangDuoi.add(btnLuu);
        hangDuoi.add(btnThuHoi);

        actions.add(btnTaoMoi);
        actions.add(hangDuoi);
        add(actions, BorderLayout.SOUTH);
    }

    private void taoTheMoi() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return dangNhapQRService.taoTheMoi(taiKhoan.getMaTK());
            }
            @Override
            protected void done() {
                try {
                    String noiDung = get();
                    anhQR = QrCodeUtils.taoAnhQR(noiDung, 320);
                    lblAnh.setText("");
                    lblAnh.setIcon(new ImageIcon(anhQR));
                    btnLuu.setEnabled(true);
                    UIUtils.thongBao(TheQRDangNhapDialog.this, "Đã tạo thẻ QR mới. Thẻ cũ (nếu có) đã bị vô hiệu hóa.");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TheQRDangNhapDialog.this, "Lỗi: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void thuHoiThe() {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Thu hồi thẻ QR hiện tại của " + taiKhoan.getTenDangNhap() + "?\nSinh viên sẽ không thể đăng nhập bằng thẻ cũ nữa.",
                "Xác nhận thu hồi", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                dangNhapQRService.thuHoiThe(taiKhoan.getMaTK());
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    lblAnh.setIcon(null);
                    lblAnh.setText("Đã thu hồi. Bấm \"Tạo thẻ QR mới\" nếu muốn cấp lại.");
                    btnLuu.setEnabled(false);
                    UIUtils.thongBao(TheQRDangNhapDialog.this, "Đã thu hồi thẻ QR.");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TheQRDangNhapDialog.this, "Lỗi: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void luuAnh() {
        if (anhQR == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("the_qr_" + taiKhoan.getTenDangNhap() + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".png")) path += ".png";
        try {
            QrCodeUtils.luuAnhQR(anhQR, new File(path));
            UIUtils.thongBao(this, "Đã lưu: " + path);
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Lưu thất bại: " + ex.getMessage());
        }
    }
}