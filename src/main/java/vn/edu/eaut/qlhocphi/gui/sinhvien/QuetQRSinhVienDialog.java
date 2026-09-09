package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.util.QRCodeSinhVienUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

/** "Them sinh vien tu ma QR": Admin chon 1 anh chua ma QR (da duoc tao san tu
 *  nguon khac, VD: form dang ky, the SV cu...), he thong tu doc & dien san Ma
 *  SV/Ho ten/Ngay sinh/Que quan/Dia chi/Email/SDT - Admin CHI CAN chon Khoa va
 *  Lop roi luu, khong phai go tay toan bo thong tin ca nhan nua. */
public class QuetQRSinhVienDialog extends JDialog {
    private final SinhVienService sinhVienService = new SinhVienService();
    private final Consumer<SinhVien> khiThanhCong;

    private SinhVien svDaDoc;
    private JLabel lblTrangThaiDoc;
    private JPanel khoiThongTin;
    private JComboBox<String> cboKhoa, cboLop;
    private JButton btnLuu;

    public QuetQRSinhVienDialog(Frame owner, Consumer<SinhVien> khiThanhCong) {
        super(owner, "", true);
        this.khiThanhCong = khiThanhCong;
        setSize(460, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 78));
        JLabel t1 = new JLabel("Thêm sinh viên từ mã QR");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Chọn ảnh chứa mã QR để tự động điền thông tin");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t2.setForeground(new Color(255, 255, 255, 210));
        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        chuText.add(t1);
        chuText.add(t2);
        banner.add(chuText, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));

        JButton btnChonAnh = UITheme.primaryButton("📷 Chọn ảnh chứa mã QR");
        btnChonAnh.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnChonAnh.addActionListener(e -> chonAnhVaDoc());
        form.add(btnChonAnh);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        lblTrangThaiDoc = new JLabel("Chưa chọn ảnh nào.");
        lblTrangThaiDoc.setFont(UITheme.FONT_BASE);
        lblTrangThaiDoc.setForeground(UITheme.TEXT_MUTED);
        lblTrangThaiDoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTrangThaiDoc);
        form.add(Box.createRigidArea(new Dimension(0, 12)));

        khoiThongTin = new JPanel();
        khoiThongTin.setLayout(new BoxLayout(khoiThongTin, BoxLayout.Y_AXIS));
        khoiThongTin.setOpaque(false);
        khoiThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(khoiThongTin);
        form.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel lblKhoa = nhan("Khoa (bắt buộc)");
        form.add(lblKhoa);
        form.add(Box.createRigidArea(new Dimension(0, 6)));
        cboKhoa = new JComboBox<>();
        cboKhoa.setEditable(true);
        styleCombo(cboKhoa);
        form.add(cboKhoa);
        form.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel lblLop = nhan("Lớp (bắt buộc)");
        form.add(lblLop);
        form.add(Box.createRigidArea(new Dimension(0, 6)));
        cboLop = new JComboBox<>();
        cboLop.setEditable(true);
        styleCombo(cboLop);
        form.add(cboLop);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnHuy = UITheme.secondaryButton("Hủy");
        btnLuu = UITheme.primaryButton("Tạo sinh viên");
        btnLuu.setEnabled(false);
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luuSinhVien());
        actions.add(btnHuy);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);
    }

    private void chonAnhVaDoc() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh chứa mã QR");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        try {
            String noiDung = QRCodeSinhVienUtils.docAnhQR(file);
            svDaDoc = QRCodeSinhVienUtils.phanTichNoiDungSinhVien(noiDung);
            hienThiThongTinDaDoc();
            lblTrangThaiDoc.setForeground(UITheme.SUCCESS);
            lblTrangThaiDoc.setText("Đã đọc mã QR thành công.");
            btnLuu.setEnabled(true);
        } catch (Exception ex) {
            svDaDoc = null;
            btnLuu.setEnabled(false);
            khoiThongTin.removeAll();
            khoiThongTin.revalidate();
            khoiThongTin.repaint();
            lblTrangThaiDoc.setForeground(UITheme.DANGER);
            lblTrangThaiDoc.setText("Không đọc được mã QR từ ảnh này: " + ex.getMessage());
        }
    }

    private void hienThiThongTinDaDoc() {
        khoiThongTin.removeAll();
        khoiThongTin.add(dongThongTin("Mã SV", svDaDoc.getMaSV()));
        khoiThongTin.add(dongThongTin("Họ tên", svDaDoc.getHoTen()));
        khoiThongTin.add(dongThongTin("Ngày sinh", svDaDoc.getNgaySinh() != null ? svDaDoc.getNgaySinh().toString() : "-"));
        khoiThongTin.add(dongThongTin("Quê quán", svDaDoc.getQueQuan()));
        khoiThongTin.add(dongThongTin("Địa chỉ", svDaDoc.getDiaChi()));
        khoiThongTin.add(dongThongTin("Email", svDaDoc.getEmail()));
        khoiThongTin.add(dongThongTin("SĐT", svDaDoc.getSoDienThoai()));
        khoiThongTin.revalidate();
        khoiThongTin.repaint();
    }

    private JPanel dongThongTin(String nhan, String giaTri) {
        JPanel dong = new JPanel(new BorderLayout(16, 0));
        dong.setOpaque(true);
        dong.setBackground(UITheme.TINT_BLUE);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        dong.setBorder(new EmptyBorder(4, 10, 4, 10));
        JLabel l1 = new JLabel(nhan);
        l1.setFont(UITheme.FONT_BASE);
        l1.setForeground(UITheme.TEXT_BLUE);
        JLabel l2 = new JLabel(giaTri == null || giaTri.isBlank() ? "-" : giaTri);
        l2.setFont(UITheme.FONT_BOLD);
        l2.setForeground(UITheme.TEXT_BLUE);
        dong.add(l1, BorderLayout.WEST);
        dong.add(l2, BorderLayout.EAST);
        return dong;
    }

    private void luuSinhVien() {
        String khoa = ((String) cboKhoa.getEditor().getItem()).trim();
        String lop = ((String) cboLop.getEditor().getItem()).trim();
        if (khoa.isEmpty() || lop.isEmpty()) {
            UIUtils.thongBaoLoi(this, "Vui lòng nhập Khoa và Lớp cho sinh viên.");
            return;
        }
        if (svDaDoc.getMaSV() == null || svDaDoc.getMaSV().isBlank()) {
            UIUtils.thongBaoLoi(this, "Mã QR không chứa Mã SV hợp lệ.");
            return;
        }
        svDaDoc.setKhoa(khoa);
        svDaDoc.setLop(lop);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sinhVienService.them(svDaDoc);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(QuetQRSinhVienDialog.this, "Đã thêm sinh viên " + svDaDoc.getMaSV() + " từ mã QR.");
                    if (khiThanhCong != null) khiThanhCong.accept(svDaDoc);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(QuetQRSinhVienDialog.this, "Lỗi: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JLabel nhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(UITheme.FONT_BASE);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }
}