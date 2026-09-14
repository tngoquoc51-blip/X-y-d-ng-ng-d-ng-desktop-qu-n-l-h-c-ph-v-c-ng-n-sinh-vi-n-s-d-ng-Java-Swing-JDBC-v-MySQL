package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.MauThongBao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Quản lý mẫu Email / SMS (thay vì hard-code trong code). */
public class MauThongBaoPanel extends JPanel {
    private final AdminConfigService service = new AdminConfigService();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtTieuDe = new JTextField();
    private final JTextArea txtNoiDung = new JTextArea(8, 40);
    private final JLabel lblBien = new JLabel(" ");
    private final JCheckBox chkDangDung = new JCheckBox("Đang dùng");
    private List<MauThongBao> danhSach;

    public MauThongBaoPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel("Quản lý mẫu thông báo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Sửa nội dung mẫu Email / SMS (nhắc nợ, OTP, hóa đơn…)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(Box.createRigidArea(new Dimension(0, 4)));
        head.add(sub);
        add(head, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Mã loại", "Kênh", "Tiêu đề", "Đang dùng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(UITheme.FONT_BASE);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienChiTiet();
        });
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setPreferredSize(new Dimension(0, 180));
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

        // Form chi tiết
        JPanel form = new JPanel();
        form.setOpaque(true);
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(lbl("Tiêu đề (chỉ Email)"));
        styleField(txtTieuDe);
        form.add(txtTieuDe);
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(lbl("Nội dung"));
        txtNoiDung.setFont(UITheme.FONT_BASE);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        txtNoiDung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane scrollNd = new JScrollPane(txtNoiDung);
        scrollNd.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollNd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        form.add(scrollNd);
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        lblBien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBien.setForeground(UITheme.PRIMARY);
        lblBien.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblBien);
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        chkDangDung.setOpaque(false);
        chkDangDung.setFont(UITheme.FONT_BASE);
        chkDangDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkDangDung);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(scrollTable, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton btnTai = UITheme.secondaryButton("Tải lại");
        JButton btnLuu = UITheme.primaryButton("Lưu mẫu đang chọn");
        btnTai.addActionListener(e -> taiDuLieu());
        btnLuu.addActionListener(e -> luu());
        actions.add(btnTai);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);

        taiDuLieu();
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(UITheme.FONT_BASE);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
    }

    private void taiDuLieu() {
        SwingWorker<List<MauThongBao>, Void> w = new SwingWorker<>() {
            @Override protected List<MauThongBao> doInBackground() throws Exception {
                return service.layMauThongBao();
            }
            @Override protected void done() {
                try {
                    danhSach = get();
                    model.setRowCount(0);
                    for (MauThongBao m : danhSach) {
                        model.addRow(new Object[]{m.getMaLoai(), m.getKenh(),
                                m.getTieuDe() == null ? "—" : m.getTieuDe(),
                                m.isDangDung() ? "Có" : "Không"});
                    }
                    if (model.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(MauThongBaoPanel.this, "Không tải được: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void hienChiTiet() {
        int r = table.getSelectedRow();
        if (r < 0 || danhSach == null || r >= danhSach.size()) return;
        MauThongBao m = danhSach.get(r);
        txtTieuDe.setText(m.getTieuDe() == null ? "" : m.getTieuDe());
        txtTieuDe.setEnabled("EMAIL".equals(m.getKenh()));
        txtNoiDung.setText(m.getNoiDung() == null ? "" : m.getNoiDung());
        lblBien.setText("Biến hỗ trợ: " + (m.getBienHoTro() == null ? "—" : m.getBienHoTro()));
        chkDangDung.setSelected(m.isDangDung());
    }

    private void luu() {
        int r = table.getSelectedRow();
        if (r < 0 || danhSach == null) {
            UIUtils.thongBaoLoi(this, "Chọn 1 mẫu để lưu");
            return;
        }
        MauThongBao m = danhSach.get(r);
        m.setTieuDe(txtTieuDe.getText().trim());
        m.setNoiDung(txtNoiDung.getText());
        m.setDangDung(chkDangDung.isSelected());
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.capNhatMau(m);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(MauThongBaoPanel.this, "Đã lưu mẫu " + m.getMaLoai());
                    taiDuLieu();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(MauThongBaoPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }
}