package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.CauHinhHeThong;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cấu hình hệ thống kỹ thuật: SMTP, SMS (eSMS), VNPay, MoMo, Google OAuth.
 * Lưu vào bảng cauhinhhethong – không cần sửa file .properties.
 */
public class CauHinhKyThuatPanel extends JPanel {
    private final TaiKhoan taiKhoan;
    private final AdminConfigService service = new AdminConfigService();
    private final JTabbedPane tabs = new JTabbedPane();
    private final Map<String, Map<String, JTextField>> fieldsByNhom = new LinkedHashMap<>();

    public CauHinhKyThuatPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel("Cấu hình hệ thống kỹ thuật");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Quản lý SMTP, SMS Gateway, VNPay/MoMo, Google OAuth qua UI");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(Box.createRigidArea(new Dimension(0, 4)));
        head.add(sub);
        add(head, BorderLayout.NORTH);

        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("  SMTP Email  ", buildTab("SMTP", new String[][]{
                {"mail.host", "SMTP Host"},
                {"mail.port", "SMTP Port"},
                {"mail.username", "Email / Username"},
                {"mail.password", "App Password"}
        }));
        tabs.addTab("  SMS (eSMS)  ", buildTab("SMS", new String[][]{
                {"esms.api.key", "API Key"},
                {"esms.secret.key", "Secret Key"},
                {"esms.brandname", "Brandname"}
        }));
        tabs.addTab("  VNPay  ", buildTab("VNPAY", new String[][]{
                {"vnpay.tmncode", "TMN Code"},
                {"vnpay.hashsecret", "Hash Secret"},
                {"vnpay.payurl", "Pay URL"}
        }));
        tabs.addTab("  MoMo  ", buildTab("MOMO", new String[][]{
                {"momo.partnercode", "Partner Code"},
                {"momo.accesskey", "Access Key"},
                {"momo.secretkey", "Secret Key"},
                {"momo.payurl", "Pay URL"}
        }));
        tabs.addTab("  Google OAuth  ", buildTab("OAUTH", new String[][]{
                {"google.client.id", "Client ID"},
                {"google.client.secret", "Client Secret"}
        }));
        add(tabs, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnTai = UITheme.secondaryButton("Tải lại");
        JButton btnLuu = UITheme.primaryButton("Lưu cấu hình tab hiện tại");
        btnTai.addActionListener(e -> taiDuLieu());
        btnLuu.addActionListener(e -> luuTabHienTai());
        actions.add(btnTai);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);

        taiDuLieu();
    }

    private JPanel buildTab(String nhom, String[][] keys) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 12, 12, 12));
        Map<String, JTextField> map = new LinkedHashMap<>();
        for (String[] pair : keys) {
            JLabel lbl = new JLabel(pair[1] + "  (" + pair[0] + ")");
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.TEXT_MUTED);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            JTextField tf = new JTextField();
            tf.setFont(UITheme.FONT_BASE);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            tf.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Ẩn password-like fields
            if (pair[0].contains("password") || pair[0].contains("secret") || pair[0].contains("key")) {
                JPasswordField pf = new JPasswordField();
                pf.setFont(UITheme.FONT_BASE);
                pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                pf.setBorder(tf.getBorder());
                pf.setAlignmentX(Component.LEFT_ALIGNMENT);
                map.put(pair[0], pf);
                p.add(lbl);
                p.add(Box.createRigidArea(new Dimension(0, 4)));
                p.add(pf);
            } else {
                map.put(pair[0], tf);
                p.add(lbl);
                p.add(Box.createRigidArea(new Dimension(0, 4)));
                p.add(tf);
            }
            p.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        fieldsByNhom.put(nhom, map);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private void taiDuLieu() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                for (Map.Entry<String, Map<String, JTextField>> e : fieldsByNhom.entrySet()) {
                    List<CauHinhHeThong> list = service.layCauHinhTheoNhom(e.getKey());
                    Map<String, String> val = new HashMap<>();
                    for (CauHinhHeThong c : list) val.put(c.getKhoa(), c.getGiaTri() == null ? "" : c.getGiaTri());
                    SwingUtilities.invokeLater(() -> {
                        for (Map.Entry<String, JTextField> f : e.getValue().entrySet()) {
                            f.getValue().setText(val.getOrDefault(f.getKey(), ""));
                        }
                    });
                }
                return null;
            }
            @Override protected void done() {
                try { get(); } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CauHinhKyThuatPanel.this, "Không tải được cấu hình: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void luuTabHienTai() {
        int idx = tabs.getSelectedIndex();
        String[] nhoms = {"SMTP", "SMS", "VNPAY", "MOMO", "OAUTH"};
        if (idx < 0 || idx >= nhoms.length) return;
        String nhom = nhoms[idx];
        Map<String, JTextField> fields = fieldsByNhom.get(nhom);
        Map<String, String> data = new LinkedHashMap<>();
        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            data.put(e.getKey(), e.getValue().getText().trim());
        }
        // Lưu CSDL trên luồng nền – không treo UI (SwingWorker)
        final String nhomFinal = nhom;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.luuNhieuCauHinh(data, nhomFinal, taiKhoan.getTenDangNhap());
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CauHinhKyThuatPanel.this, "Đã lưu cấu hình " + nhomFinal);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CauHinhKyThuatPanel.this, "Lỗi lưu: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }
}