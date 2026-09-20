package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.CauHinhBaoMat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cấu hình bảo mật – giao diện chuẩn Admin trường ĐH:
 * banner + card chính sách + card mã hóa AES.
 */
public class CauHinhBaoMatPanel extends JPanel {
    private final AdminConfigService service = new AdminConfigService();
    private final JSpinner spDoDai = new JSpinner(new SpinnerNumberModel(6, 4, 32, 1));
    private final JSpinner spSession = new JSpinner(new SpinnerNumberModel(60, 5, 1440, 5));
    private final JSpinner spLanSai = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    private final JSpinner spKhoaPhut = new JSpinner(new SpinnerNumberModel(15, 1, 1440, 1));
    private final JCheckBox chkDoiMk = new JCheckBox("Bắt buộc đổi mật khẩu lần đăng nhập đầu");

    public CauHinhBaoMatPanel() {
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 8, 4));

        add(buildBanner(), BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildCardChinhSach());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildCardMaHoa());
        add(giua, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnTai = UITheme.secondaryButton("Tải lại");
        JButton btnLuu = UITheme.primaryButton("Lưu cấu hình bảo mật");
        btnTai.addActionListener(e -> taiDuLieu());
        btnLuu.addActionListener(e -> luu());
        actions.add(btnTai);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);

        styleSpinner(spDoDai);
        styleSpinner(spSession);
        styleSpinner(spLanSai);
        styleSpinner(spKhoaPhut);
        chkDoiMk.setFont(UITheme.FONT_BASE);
        chkDoiMk.setOpaque(false);
        taiDuLieu();
    }

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        banner.setPreferredSize(new Dimension(10, 82));
        JLabel t = new JLabel("Cấu hình bảo mật");
        t.setFont(UITheme.FONT_TITLE);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Chính sách mật khẩu · Phiên đăng nhập · Khóa tài khoản · Mã hóa AES-256");
        s.setFont(UITheme.FONT_BASE);
        s.setForeground(new Color(255, 255, 255, 215));
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(t);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(s);
        banner.add(box, BorderLayout.CENTER);
        return banner;
    }

    private JPanel buildCardChinhSach() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JLabel td = new JLabel("Chính sách đăng nhập & mật khẩu");
        td.setFont(new Font("Segoe UI", Font.BOLD, 15));
        td.setForeground(UITheme.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        themDong(form, gbc, 0, "Độ dài mật khẩu tối thiểu", spDoDai, "ký tự (4–32)");
        themDong(form, gbc, 1, "Thời gian hết phiên (session)", spSession, "phút");
        themDong(form, gbc, 2, "Số lần đăng nhập sai tối đa", spLanSai, "lần → khóa tạm");
        themDong(form, gbc, 3, "Thời gian khóa tài khoản", spKhoaPhut, "phút");

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        form.add(chkDoiMk, gbc);

        card.add(td, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCardMaHoa() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 24, 18, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel td = new JLabel("Mã hóa dữ liệu nhạy cảm (AES-256)");
        td.setFont(new Font("Segoe UI", Font.BOLD, 15));
        td.setForeground(UITheme.TEXT_PRIMARY);

        JLabel moTa = new JLabel("<html>Mã hóa Email, Số điện thoại, Địa chỉ, Quê quán của sinh viên.<br>"
                + "Chạy một lần để mã hóa toàn bộ dữ liệu cũ đang lưu dạng plaintext.</html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);

        JButton btn = UITheme.primaryButton("Mã hóa dữ liệu cũ ngay");
        btn.addActionListener(e -> maHoaDuLieuCu());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        south.add(btn);

        card.add(td, BorderLayout.NORTH);
        card.add(moTa, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private void maHoaDuLieuCu() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Chạy migrate mã hóa toàn bộ trường nhạy cảm còn plaintext?\nCó thể mất vài giây với dữ liệu lớn.",
                "Xác nhận mã hóa", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        btnBusy(true);
        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try {
                    Class<?> c = Class.forName("vn.edu.eaut.qlhocphi.util.DataEncryptionMigrator");
                    Object r = c.getMethod("migrateSinhVien").invoke(null);
                    return r instanceof Integer ? (Integer) r : 0;
                } catch (ClassNotFoundException ex) {
                    return -1;
                }
            }

            @Override
            protected void done() {
                btnBusy(false);
                try {
                    int n = get();
                    if (n < 0) {
                        UIUtils.thongBaoLoi(CauHinhBaoMatPanel.this,
                                "Không tìm thấy DataEncryptionMigrator. Chạy migrate bằng SQL/tool đã triển khai.");
                    } else {
                        UIUtils.thongBao(CauHinhBaoMatPanel.this, "Đã mã hóa " + n + " bản ghi (hoặc bỏ qua bản ghi đã mã hóa).");
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CauHinhBaoMatPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void btnBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void themDong(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field, String donVi) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        form.add(lbl, gbc);
        gbc.gridx = 1;
        field.setPreferredSize(new Dimension(110, 34));
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
        try {
            service.luuCauHinhBaoMat(b);
            UIUtils.thongBao(this, "Đã lưu cấu hình bảo mật");
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, ex.getMessage());
        }
    }
}