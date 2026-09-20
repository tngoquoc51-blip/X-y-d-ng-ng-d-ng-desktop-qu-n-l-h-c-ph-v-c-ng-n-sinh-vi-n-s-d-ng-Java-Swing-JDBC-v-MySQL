package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBaoBroadcast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Broadcast toàn trường – KPI + bảng + empty state + form tạo. */
public class BroadcastPanel extends JPanel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final TaiKhoan taiKhoan;
    private final AdminConfigService service = new AdminConfigService();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Tiêu đề", "Mức độ", "Từ", "Đến", "Bật", "Người tạo"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<ThongBaoBroadcast> danhSach;
    private JLabel lblTrong;
    private JLabel lblTong, lblDangBat, lblKhanCap;

    public BroadcastPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 8, 4));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildBanner());
        north.add(Box.createRigidArea(new Dimension(0, 12)));
        north.add(buildHangKPI());
        add(north, BorderLayout.NORTH);

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JLabel td = new JLabel("Danh sách thông báo broadcast");
        td.setFont(new Font("Segoe UI", Font.BOLD, 14));
        td.setForeground(UITheme.TEXT_PRIMARY);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton btnTai = UITheme.secondaryButton("Tải lại");
        JButton btnSua = UITheme.secondaryButton("Sửa");
        JButton btnXoa = UITheme.dangerButton("Xóa");
        JButton btnThem = UITheme.primaryButton("+ Tạo thông báo");
        btnTai.addActionListener(e -> taiDuLieu());
        btnSua.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0 || danhSach == null) {
                UIUtils.thongBaoLoi(this, "Chọn 1 dòng để sửa");
                return;
            }
            moForm(danhSach.get(r));
        });
        btnXoa.addActionListener(e -> xoa());
        btnThem.addActionListener(e -> moForm(null));
        acts.add(btnTai);
        acts.add(btnSua);
        acts.add(btnXoa);
        acts.add(btnThem);
        toolbar.add(td, BorderLayout.WEST);
        toolbar.add(acts, BorderLayout.EAST);

        table.setFont(UITheme.FONT_BASE);
        table.setRowHeight(34);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
                }
                if (c == 2 && v != null) {
                    String m = v.toString();
                    if ("KHAN_CAP".equals(m)) setForeground(new Color(0xB9, 0x1C, 0x1C));
                    else if ("CANH_BAO".equals(m)) setForeground(new Color(0xB4, 0x53, 0x09));
                    else setForeground(UITheme.TEXT_PRIMARY);
                } else {
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                return comp;
            }
        });

        lblTrong = new JLabel("<html><div style='text-align:center;padding:24px'>"
                + "<b>Chưa có thông báo toàn trường</b><br>"
                + "Bấm <b>+ Tạo thông báo</b> hoặc chạy SQL seed để thêm mẫu (bảo trì, nhắc học phí, khẩn cấp)."
                + "</div></html>", SwingConstants.CENTER);
        lblTrong.setFont(UITheme.FONT_BASE);
        lblTrong.setForeground(UITheme.TEXT_MUTED);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(lblTrong, BorderLayout.SOUTH);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        banner.setPreferredSize(new Dimension(10, 78));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("Thông báo hệ thống toàn trường (Broadcast)");
        t.setFont(UITheme.FONT_TITLE);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Gửi thông báo chung cho toàn bộ người dùng khi đăng nhập (bảo trì, nhắc việc, khẩn cấp…)");
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

    private JPanel buildHangKPI() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        lblTong = new JLabel("0");
        lblDangBat = new JLabel("0");
        lblKhanCap = new JLabel("0");

        row.add(theKPI("Tổng broadcast", lblTong, "Tất cả thông báo đã tạo",
                new Color(0xDB, 0xEA, 0xFE), new Color(0x1D, 0x4E, 0xD8)));
        row.add(theKPI("Đang bật", lblDangBat, "Hiển thị khi user đăng nhập",
                new Color(0xD1, 0xFA, 0xE5), new Color(0x04, 0x78, 0x57)));
        row.add(theKPI("Mức khẩn cấp", lblKhanCap, "Ưu tiên cảnh báo mạnh",
                new Color(0xFE, 0xE2, 0xE2), new Color(0xB9, 0x1C, 0x1C)));
        return row;
    }

    private JPanel theKPI(String tieuDe, JLabel lblGiaTri, String phu, Color nen, Color chu) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(nen);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel td = new JLabel(tieuDe);
        td.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        td.setForeground(chu);
        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGiaTri.setForeground(chu);
        JLabel sp = new JLabel(phu);
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sp.setForeground(new Color(chu.getRed(), chu.getGreen(), chu.getBlue(), 180));
        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(td);
        mid.add(lblGiaTri);
        mid.add(sp);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private void capNhatKPI() {
        int tong = danhSach == null ? 0 : danhSach.size();
        int bat = 0, khan = 0;
        if (danhSach != null) {
            for (ThongBaoBroadcast b : danhSach) {
                if (b.isDangBat()) bat++;
                if ("KHAN_CAP".equalsIgnoreCase(b.getMucDo())) khan++;
            }
        }
        lblTong.setText(String.valueOf(tong));
        lblDangBat.setText(String.valueOf(bat));
        lblKhanCap.setText(String.valueOf(khan));
    }

    private void taiDuLieu() {
        SwingWorker<List<ThongBaoBroadcast>, Void> w = new SwingWorker<>() {
            @Override
            protected List<ThongBaoBroadcast> doInBackground() throws Exception {
                return service.layTatCaBroadcast();
            }

            @Override
            protected void done() {
                try {
                    danhSach = get();
                    model.setRowCount(0);
                    if (danhSach != null) {
                        for (ThongBaoBroadcast b : danhSach) {
                            model.addRow(new Object[]{
                                    b.getMaBroadcast(),
                                    b.getTieuDe(),
                                    b.getMucDo(),
                                    b.getHienThiTu() == null ? "—" : b.getHienThiTu().format(FMT),
                                    b.getHienThiDen() == null ? "Không hạn" : b.getHienThiDen().format(FMT),
                                    b.isDangBat() ? "Có" : "Không",
                                    b.getNguoiTao() == null ? "—" : b.getNguoiTao()
                            });
                        }
                    }
                    boolean empty = danhSach == null || danhSach.isEmpty();
                    lblTrong.setVisible(empty);
                    capNhatKPI();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(BroadcastPanel.this, "Không tải được: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void moForm(ThongBaoBroadcast sua) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                sua == null ? "Tạo thông báo broadcast" : "Sửa thông báo",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setBorder(new EmptyBorder(18, 22, 18, 22));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);

        JTextField txtTieuDe = new JTextField(sua == null ? "" : sua.getTieuDe());
        txtTieuDe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextArea txtNd = new JTextArea(sua == null ? "" : sua.getNoiDung(), 7, 30);
        txtNd.setLineWrap(true);
        txtNd.setWrapStyleWord(true);
        JComboBox<String> cboMuc = new JComboBox<>(new String[]{"THONG_TIN", "CANH_BAO", "KHAN_CAP"});
        if (sua != null) cboMuc.setSelectedItem(sua.getMucDo());
        JCheckBox chkBat = new JCheckBox("Đang bật (hiển thị cho user)", sua == null || sua.isDangBat());

        form.add(label("Tiêu đề"));
        form.add(txtTieuDe);
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(label("Nội dung"));
        form.add(new JScrollPane(txtNd));
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(label("Mức độ"));
        form.add(cboMuc);
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(chkBat);

        dlg.add(form, BorderLayout.CENTER);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnHuy = UITheme.secondaryButton("Hủy");
        JButton btnLuu = UITheme.primaryButton("Lưu");
        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            try {
                if (txtTieuDe.getText().trim().isEmpty()) {
                    UIUtils.thongBaoLoi(dlg, "Nhập tiêu đề");
                    return;
                }
                ThongBaoBroadcast b = sua == null ? new ThongBaoBroadcast() : sua;
                b.setTieuDe(txtTieuDe.getText().trim());
                b.setNoiDung(txtNd.getText().trim());
                b.setMucDo((String) cboMuc.getSelectedItem());
                b.setDangBat(chkBat.isSelected());
                if (sua == null) {
                    b.setHienThiTu(LocalDateTime.now());
                    b.setNguoiTao(taiKhoan.getTenDangNhap());
                    service.themBroadcast(b);
                } else {
                    if (b.getHienThiTu() == null) b.setHienThiTu(LocalDateTime.now());
                    service.capNhatBroadcast(b);
                }
                UIUtils.thongBao(BroadcastPanel.this, "Đã lưu thông báo");
                dlg.dispose();
                taiDuLieu();
            } catch (Exception ex) {
                UIUtils.thongBaoLoi(dlg, ex.getMessage());
            }
        });
        acts.add(btnHuy);
        acts.add(btnLuu);
        dlg.add(acts, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_BOLD);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void xoa() {
        int r = table.getSelectedRow();
        if (r < 0 || danhSach == null) {
            UIUtils.thongBaoLoi(this, "Chọn 1 dòng");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa thông báo này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            service.xoaBroadcast(danhSach.get(r).getMaBroadcast());
            taiDuLieu();
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, ex.getMessage());
        }
    }
}