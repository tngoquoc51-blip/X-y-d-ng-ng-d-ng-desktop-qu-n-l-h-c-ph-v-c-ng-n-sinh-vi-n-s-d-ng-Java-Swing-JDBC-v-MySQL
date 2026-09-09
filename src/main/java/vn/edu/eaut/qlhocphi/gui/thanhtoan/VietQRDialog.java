package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.ThanhToanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.VietQRUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Popup hien thi ma QR VietQR de sinh vien/phu huynh quet chuyen khoan truc
 * tiep bang app ngan hang bat ky - khong can qua VNPay/MoMo, khong mat phi.
 *
 * 2 CHE DO SU DUNG:
 * 1) choPhepXacNhanThu = true  -> danh cho man hinh KE TOAN: hien them o
 *    "Nguoi thu" + nut "Xac nhan da nhan tien" de ghi nhan thu cong sau khi
 *    doi chieu sao ke ngan hang that (goi ThanhToanService.ghiNhanThanhToan).
 * 2) choPhepXacNhanThu = false -> danh cho man hinh SINH VIEN / TRA CUU CONG
 *    KHAI: chi xem QR de chuyen khoan, khong co quyen tu xac nhan da thu tien.
 */
public class VietQRDialog extends JDialog {

    public VietQRDialog(Window chaMe, HoaDonHocPhi hoaDon, boolean choPhepXacNhanThu, Runnable khiXacNhanXong) {
        super(chaMe, "Thanh toan qua VietQR", ModalityType.APPLICATION_MODAL);
        setSize(440, choPhepXacNhanThu ? 700 : 640);
        setMinimumSize(new Dimension(420, 560));
        setLocationRelativeTo(chaMe);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        java.math.BigDecimal soTien = hoaDon.tinhConNo();
        String noiDungCK = VietQRUtils.taoNoiDungChuyenKhoan(hoaDon.getMaSV(), hoaDon.getMaHoaDon());

        add(buildHeader(hoaDon), BorderLayout.NORTH);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(new EmptyBorder(16, 18, 16, 18));

        noiDung.add(buildTheQR(soTien, noiDungCK));
        noiDung.add(Box.createRigidArea(new Dimension(0, 14)));
        noiDung.add(buildTheThongTin(hoaDon, soTien, noiDungCK));
        noiDung.add(Box.createRigidArea(new Dimension(0, 12)));
        noiDung.add(buildGhiChu());

        if (choPhepXacNhanThu) {
            noiDung.add(Box.createRigidArea(new Dimension(0, 14)));
            noiDung.add(buildKhuXacNhan(hoaDon, soTien, khiXacNhanXong));
        }

        JScrollPane scroll = new JScrollPane(noiDung);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        add(buildChanTrang(), BorderLayout.SOUTH);
    }

    // ================== HEADER (banner gradient) ==================

    private JPanel buildHeader(HoaDonHocPhi hd) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 14, 0, 14));

        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(18, 20, 18, 20));
        banner.setPreferredSize(new Dimension(100, 78));

        JLabel lblIcon = new JLabel("\uD83D\uDCF1  Quet ma de chuyen khoan");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblIcon.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Hoa don #" + hd.getMaHoaDon() + " - " + hd.getTenHocKy());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(255, 255, 255, 210));

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.add(lblIcon);
        textBox.add(Box.createRigidArea(new Dimension(0, 4)));
        textBox.add(lblSub);

        banner.add(textBox, BorderLayout.WEST);
        wrap.add(banner, BorderLayout.CENTER);
        return wrap;
    }

    // ================== THE QR ==================

    private JPanel buildTheQR(java.math.BigDecimal soTien, String noiDungCK) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));

        JLabel lblQR = new JLabel("Dang tai ma QR...", SwingConstants.CENTER);
        lblQR.setFont(UITheme.FONT_BASE);
        lblQR.setForeground(UITheme.TEXT_MUTED);
        lblQR.setPreferredSize(new Dimension(280, 280));
        lblQR.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblQR, BorderLayout.CENTER);

        if (!VietQRUtils.daCauHinh()) {
            lblQR.setText("<html><center>Chua cau hinh vietqr.bank.bin<br/>trong application.properties</center></html>");
            lblQR.setForeground(UITheme.DANGER);
            return card;
        }

        String urlAnh = VietQRUtils.taoUrlAnhQR(soTien, noiDungCK);
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    BufferedImage anh = ImageIO.read(new URL(urlAnh));
                    if (anh == null) return null;
                    Image thuNho = anh.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                    return new ImageIcon(thuNho);
                } catch (IOException ex) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblQR.setText(null);
                        lblQR.setIcon(icon);
                    } else {
                        lblQR.setText("<html><center>Khong tai duoc ma QR.<br/>Kiem tra ket noi Internet.</center></html>");
                        lblQR.setForeground(UITheme.DANGER);
                    }
                } catch (Exception ex) {
                    lblQR.setText("Loi tai ma QR");
                    lblQR.setForeground(UITheme.DANGER);
                }
            }
        };
        worker.execute();

        return card;
    }

    // ================== THE THONG TIN CHUYEN KHOAN ==================

    private JPanel buildTheThongTin(HoaDonHocPhi hd, java.math.BigDecimal soTien, String noiDungCK) {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(dongThongTin("Ngan hang", VietQRUtils.tenNganHang(), null));
        card.add(dongDuongKe());
        card.add(dongThongTin("So tai khoan", VietQRUtils.soTaiKhoan(), VietQRUtils.soTaiKhoan()));
        card.add(dongDuongKe());
        card.add(dongThongTin("Chu tai khoan", VietQRUtils.tenChuTaiKhoan(), null));
        card.add(dongDuongKe());
        card.add(dongThongTin("Noi dung CK", noiDungCK, noiDungCK));
        card.add(dongDuongKe());

        JPanel dongSoTien = new JPanel(new BorderLayout());
        dongSoTien.setOpaque(false);
        dongSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongSoTien.setBorder(new EmptyBorder(10, 0, 2, 0));
        JLabel lblNhanTien = new JLabel("So tien can chuyen");
        lblNhanTien.setFont(UITheme.FONT_BASE);
        lblNhanTien.setForeground(UITheme.TEXT_MUTED);
        JLabel lblGiaTri = new JLabel(MoneyUtils.format(soTien));
        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGiaTri.setForeground(UITheme.PRIMARY);
        dongSoTien.add(lblNhanTien, BorderLayout.NORTH);
        dongSoTien.add(lblGiaTri, BorderLayout.CENTER);
        card.add(dongSoTien);

        return card;
    }

    private JPanel dongThongTin(String nhan, String giaTri, String giaTriDeCopy) {
        JPanel dong = new JPanel(new BorderLayout(10, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setBorder(new EmptyBorder(7, 0, 7, 0));
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UITheme.FONT_BASE);
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        dong.add(lblNhan, BorderLayout.WEST);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        phai.setOpaque(false);
        JLabel lblGiaTri = new JLabel(giaTri == null || giaTri.isBlank() ? "-" : giaTri);
        lblGiaTri.setFont(UITheme.FONT_BOLD);
        lblGiaTri.setForeground(UITheme.TEXT_PRIMARY);
        phai.add(lblGiaTri);

        if (giaTriDeCopy != null) {
            JButton btnCopy = new JButton("Sao chep");
            btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnCopy.setForeground(UITheme.PRIMARY);
            btnCopy.setBorderPainted(false);
            btnCopy.setContentAreaFilled(false);
            btnCopy.setFocusPainted(false);
            btnCopy.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCopy.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(giaTriDeCopy), null);
                btnCopy.setText("Da chep!");
                Timer t = new Timer(1400, ev -> btnCopy.setText("Sao chep"));
                t.setRepeats(false);
                t.start();
            });
            phai.add(btnCopy);
        }

        dong.add(phai, BorderLayout.EAST);
        return dong;
    }

    private JSeparator dongDuongKe() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    // ================== GHI CHU ==================

    private JPanel buildGhiChu() {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setOpaque(true);
        box.setBackground(UITheme.TINT_BLUE);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(new EmptyBorder(12, 14, 12, 14));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lblIcon = new JLabel("\u2139\uFE0F");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel lblText = new JLabel("<html>Mo app ngan hang bat ky co ho tro VietQR &rarr; chon <b>Quet ma QR</b> &rarr;"
                + " kiem tra dung so tien va noi dung &rarr; xac nhan chuyen khoan.</html>");
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblText.setForeground(UITheme.TEXT_BLUE);

        box.add(lblIcon, BorderLayout.WEST);
        box.add(lblText, BorderLayout.CENTER);
        return box;
    }

    // ================== KHU XAC NHAN (danh cho KE TOAN) ==================

    private JPanel buildKhuXacNhan(HoaDonHocPhi hd, java.math.BigDecimal soTien, Runnable khiXacNhanXong) {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tieuDe = UITheme.sectionLabel("Xac nhan da nhan tien");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(tieuDe);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel phu = new JLabel("Chi bam sau khi da kiem tra sao ke ngan hang that co giao dich nay.");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        phu.setForeground(UITheme.TEXT_MUTED);
        card.add(phu);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel dongNhap = new JPanel(new BorderLayout(8, 0));
        dongNhap.setOpaque(false);
        dongNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lblNguoiThu = new JLabel("Nguoi thu:");
        lblNguoiThu.setFont(UITheme.FONT_BASE);
        JTextField txtNguoiThu = new JTextField();
        txtNguoiThu.setFont(UITheme.FONT_BASE);
        dongNhap.add(lblNguoiThu, BorderLayout.WEST);
        dongNhap.add(txtNguoiThu, BorderLayout.CENTER);
        card.add(dongNhap);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JButton btnXacNhan = UITheme.primaryButton("\u2713  Xac nhan da nhan " + MoneyUtils.format(soTien));
        btnXacNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnXacNhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(btnXacNhan);

        btnXacNhan.addActionListener(e -> {
            String nguoiThu = txtNguoiThu.getText().trim();
            if (nguoiThu.isEmpty()) {
                UIUtils.thongBaoLoi(this, "Vui long nhap ten nguoi thu truoc khi xac nhan.");
                return;
            }
            btnXacNhan.setEnabled(false);
            btnXacNhan.setText("Dang xu ly...");

            ThanhToanService thanhToanService = new ThanhToanService();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    thanhToanService.ghiNhanThanhToan(hd.getMaHoaDon(), soTien, "VietQR", nguoiThu);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        UIUtils.thongBao(VietQRDialog.this, "Da ghi nhan thanh toan " + MoneyUtils.format(soTien)
                                + " cho hoa don #" + hd.getMaHoaDon());
                        dispose();
                        if (khiXacNhanXong != null) khiXacNhanXong.run();
                    } catch (Exception ex) {
                        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                        UIUtils.thongBaoLoi(VietQRDialog.this, "Ghi nhan that bai: " + c.getMessage());
                        btnXacNhan.setEnabled(true);
                        btnXacNhan.setText("\u2713  Xac nhan da nhan " + MoneyUtils.format(soTien));
                    }
                }
            };
            worker.execute();
        });

        return card;
    }

    // ================== CHAN TRANG ==================

    private JPanel buildChanTrang() {
        JPanel chan = new JPanel(new BorderLayout());
        chan.setBackground(UITheme.BG_CARD);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(10, 18, 10, 18)));

        JButton btnDong = UITheme.secondaryButton("Dong");
        btnDong.addActionListener(e -> dispose());

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        phai.setOpaque(false);
        phai.add(btnDong);
        chan.add(phai, BorderLayout.EAST);
        return chan;
    }
}
