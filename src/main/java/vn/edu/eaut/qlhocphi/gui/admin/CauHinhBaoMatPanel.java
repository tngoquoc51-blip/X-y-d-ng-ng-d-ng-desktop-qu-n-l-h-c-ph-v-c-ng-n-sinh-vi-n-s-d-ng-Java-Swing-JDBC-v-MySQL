package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.CauHinhBaoMat;
import vn.edu.eaut.qlhocphi.util.DataEncryptionMigrator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Cấu hình bảo mật: độ dài MK, session timeout, khóa sau N lần sai + mã hóa dữ liệu. */
public class CauHinhBaoMatPanel extends JPanel {
    private final AdminConfigService service = new AdminConfigService();
    private final JSpinner spDoDai = new JSpinner(new SpinnerNumberModel(6, 4, 32, 1));
    private final JSpinner spSession = new JSpinner(new SpinnerNumberModel(60, 5, 1440, 5));
    private final JSpinner spLanSai = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    private final JSpinner spKhoaPhut = new JSpinner(new SpinnerNumberModel(15, 1, 1440, 1));
    private final JCheckBox chkDoiMk = new JCheckBox("Bắt buộc đổi mật khẩu lần đăng nhập đầu");
    private final JLabel lblTrangThaiMaHoa = new JLabel(" ");

    public CauHinhBaoMatPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel("Cấu hình bảo mật");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Chính sách mật khẩu, phiên đăng nhập, khóa tài khoản và mã hóa dữ liệu");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(Box.createRigidArea(new Dimension(0, 4)));
        head.add(sub);
        add(head, BorderLayout.NORTH);

        // ===== Form cấu hình mật khẩu =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(true);
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(24, 28, 24, 28)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        themDong(form, gbc, 0, "Độ dài mật khẩu tối thiểu", spDoDai, "ký tự (4–32)");
        themDong(form, gbc, 1, "Thời gian hết phiên (session)", spSession, "phút");
        themDong(form, gbc, 2, "Số lần đăng nhập sai tối đa", spLanSai, "lần → khóa tạm");
        themDong(form, gbc, 3, "Thời gian khóa tài khoản", spKhoaPhut, "phút");

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        chkDoiMk.setFont(UITheme.FONT_BASE);
        chkDoiMk.setOpaque(false);
        form.add(chkDoiMk, gbc);

        // ===== Phần Mã hóa dữ liệu =====
        JPanel maHoaPanel = new JPanel(new BorderLayout(12, 8));
        maHoaPanel.setOpaque(true);
        maHoaPanel.setBackground(UITheme.BG_CARD);
        maHoaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)));

        JLabel lblMaHoaTitle = new JLabel("Mã hóa dữ liệu nhạy cảm (AES-256)");
        lblMaHoaTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMaHoaTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblMaHoaMoTa = new JLabel("<html>Mã hóa Email, Số điện thoại, Địa chỉ, Quê quán của sinh viên.<br>"
                + "Chạy một lần để mã hóa toàn bộ dữ liệu cũ đang lưu dạng plaintext.</html>");
        lblMaHoaMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMaHoaMoTa.setForeground(UITheme.TEXT_MUTED);

        JPanel textMaHoa = new JPanel();
        textMaHoa.setOpaque(false);
        textMaHoa.setLayout(new BoxLayout(textMaHoa, BoxLayout.Y_AXIS));
        textMaHoa.add(lblMaHoaTitle);
        textMaHoa.add(Box.createRigidArea(new Dimension(0, 6)));
        textMaHoa.add(lblMaHoaMoTa);
        textMaHoa.add(Box.createRigidArea(new Dimension(0, 8)));
        lblTrangThaiMaHoa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textMaHoa.add(lblTrangThaiMaHoa);

        JButton btnMaHoa = UITheme.primaryButton("Mã hóa dữ liệu cũ ngay");
        btnMaHoa.addActionListener(e -> chayMaHoaDuLieuCu());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btnMaHoa);

        maHoaPanel.add(textMaHoa, BorderLayout.CENTER);
        maHoaPanel.add(btnWrap, BorderLayout.SOUTH);

        // Ghép layout
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(form);
        center.add(Box.createRigidArea(new Dimension(0, 16)));
        center.add(maHoaPanel);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrap.setOpaque(false);
        wrap.add(center);
        add(wrap, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton btnTai = UITheme.secondaryButton("Tải lại");
        JButton btnLuu = UITheme.primaryButton("Lưu cấu hình bảo mật");
        btnTai.addActionListener(e -> taiDuLieu());
        btnLuu.addActionListener(e -> luu());
        actions.add(btnTai);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);

        styleSpinner(spDoDai); styleSpinner(spSession); styleSpinner(spLanSai); styleSpinner(spKhoaPhut);
        taiDuLieu();
    }

    private void chayMaHoaDuLieuCu() {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn mã hóa toàn bộ dữ liệu nhạy cảm đang lưu dạng plaintext?\n"
                        + "Thao tác này an toàn, có thể chạy nhiều lần.",
                "Xác nhận mã hóa dữ liệu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (xacNhan != JOptionPane.YES_OPTION) return;

        lblTrangThaiMaHoa.setText("Đang mã hóa, vui lòng chờ...");
        lblTrangThaiMaHoa.setForeground(UITheme.TEXT_MUTED);

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return DataEncryptionMigrator.migrateSinhVien();
            }

            @Override
            protected void done() {
                try {
                    int soBanGhi = get();
                    if (soBanGhi == 0) {
                        lblTrangThaiMaHoa.setText("✓ Tất cả dữ liệu đã được mã hóa rồi.");
                        lblTrangThaiMaHoa.setForeground(new Color(34, 139, 34));
                        UIUtils.thongBao(CauHinhBaoMatPanel.this, "Tất cả dữ liệu nhạy cảm đã được mã hóa sẵn.");
                    } else {
                        lblTrangThaiMaHoa.setText("✓ Đã mã hóa thành công " + soBanGhi + " bản ghi.");
                        lblTrangThaiMaHoa.setForeground(new Color(34, 139, 34));
                        UIUtils.thongBao(CauHinhBaoMatPanel.this,
                                "Đã mã hóa thành công " + soBanGhi + " bản ghi sinh viên.");
                    }
                } catch (Exception ex) {
                    lblTrangThaiMaHoa.setText("✗ Lỗi: " + ex.getMessage());
                    lblTrangThaiMaHoa.setForeground(Color.RED);
                    UIUtils.thongBaoLoi(CauHinhBaoMatPanel.this, "Lỗi khi mã hóa: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void themDong(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field, String donVi) {
        gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        form.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        field.setPreferredSize(new Dimension(100, 36));
        form.add(field, gbc);
        gbc.gridx = 2;
        JLabel dv = new JLabel(donVi);
        dv.setFont(UITheme.FONT_BASE);
        dv.setForeground(UITheme.TEXT_MUTED);
        form.add(dv, gbc);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(UITheme.FONT_BASE);
        if (sp.getEditor() instanceof JSpinner.DefaultEditor ed) {
            ed.getTextField().setHorizontalAlignment(JTextField.CENTER);
        }
    }

    private void taiDuLieu() {
        SwingWorker<CauHinhBaoMat, Void> w = new SwingWorker<>() {
            @Override
            protected CauHinhBaoMat doInBackground() throws Exception {
                return service.layCauHinhBaoMat();
            }

            @Override
            protected void done() {
                try {
                    CauHinhBaoMat b = get();
                    spDoDai.setValue(b.getDoDaiMatKhauToiThieu());
                    spSession.setValue(b.getSessionTimeoutPhut());
                    spLanSai.setValue(b.getSoLanDangNhapSaiToiDa());
                    spKhoaPhut.setValue(b.getThoiGianKhoaPhut());
                    chkDoiMk.setSelected(b.isBatBuocDoiMkLanDau());
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CauHinhBaoMatPanel.this, "Không tải được: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void luu() {
        CauHinhBaoMat b = new CauHinhBaoMat();
        b.setDoDaiMatKhauToiThieu((Integer) spDoDai.getValue());
        b.setSessionTimeoutPhut((Integer) spSession.getValue());
        b.setSoLanDangNhapSaiToiDa((Integer) spLanSai.getValue());
        b.setThoiGianKhoaPhut((Integer) spKhoaPhut.getValue());
        b.setBatBuocDoiMkLanDau(chkDoiMk.isSelected());

        // Lưu CSDL trên luồng nền – không treo UI (SwingWorker)
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.luuCauHinhBaoMat(b);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CauHinhBaoMatPanel.this, "Đã lưu cấu hình bảo mật");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CauHinhBaoMatPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }
}