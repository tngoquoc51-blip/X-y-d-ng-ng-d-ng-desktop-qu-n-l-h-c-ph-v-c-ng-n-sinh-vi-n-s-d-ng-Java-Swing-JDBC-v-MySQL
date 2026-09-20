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

/** Cấu hình kỹ thuật – banner + tab card chuẩn Admin. */
public class CauHinhKyThuatPanel extends JPanel {
    private final TaiKhoan taiKhoan;
    private final AdminConfigService service = new AdminConfigService();
    private final JTabbedPane tabs = new JTabbedPane();
    private final Map<String, Map<String, JTextField>> fieldsByNhom = new LinkedHashMap<>();

    public CauHinhKyThuatPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 8, 4));

        add(buildBanner(), BorderLayout.NORTH);

        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(UITheme.BG_CARD);
        tabs.addTab("  SMTP Email  ", buildTab("SMTP", new String[][]{
                {"mail.host", "SMTP Host (mail.host)"},
                {"mail.port", "SMTP Port (mail.port)"},
                {"mail.username", "Email / Username (mail.username)"},
                {"mail.password", "App Password (mail.password)"}
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

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        banner.setPreferredSize(new Dimension(10, 82));
        JLabel t = new JLabel("Cấu hình hệ thống kỹ thuật");
        t.setFont(UITheme.FONT_TITLE);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("SMTP · SMS Gateway · VNPay / MoMo · Google OAuth — lưu qua UI, không sửa file cấu hình tay");
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

    private JPanel buildTab(String nhom, String[][] keys) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 4, 4, 4));

        JPanel p = UITheme.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(22, 28, 22, 28));

        Map<String, JTextField> map = new LinkedHashMap<>();
        for (String[] pair : keys) {
            boolean secret = pair[0].toLowerCase().contains("password")
                    || pair[0].toLowerCase().contains("secret")
                    || pair[0].toLowerCase().contains("hash");
            JLabel lbl = new JLabel(pair[1]);
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.TEXT_PRIMARY);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextField tf = secret ? new JPasswordField() : new JTextField();
            tf.setFont(UITheme.FONT_BASE);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            tf.setAlignmentX(Component.LEFT_ALIGNMENT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));

            map.put(pair[0], tf);
            p.add(lbl);
            p.add(Box.createRigidArea(new Dimension(0, 6)));
            p.add(tf);
            p.add(Box.createRigidArea(new Dimension(0, 14)));
        }
        fieldsByNhom.put(nhom, map);
        p.add(Box.createVerticalGlue());
        wrap.add(p, BorderLayout.CENTER);
        return wrap;
    }

    private void taiDuLieu() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
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

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
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
        try {
            service.luuNhieuCauHinh(data, nhom, taiKhoan.getTenDangNhap());
            UIUtils.thongBao(this, "Đã lưu cấu hình " + nhom);
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Lỗi lưu: " + ex.getMessage());
        }
    }
}