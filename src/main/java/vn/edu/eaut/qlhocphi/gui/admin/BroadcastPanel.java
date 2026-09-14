package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBaoBroadcast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Thông báo hệ thống toàn trường – hiện khi mọi user đăng nhập. */
public class BroadcastPanel extends JPanel {
    private final TaiKhoan taiKhoan;
    private final AdminConfigService service = new AdminConfigService();
    private final DefaultTableModel model;
    private final JTable table;
    private List<ThongBaoBroadcast> danhSach;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BroadcastPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        // Khởi tạo model + table TRƯỚC (tránh lỗi "might not have been initialized")
        model = new DefaultTableModel(new String[]{"ID", "Tiêu đề", "Mức độ", "Từ", "Đến", "Bật", "Người tạo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(UITheme.FONT_BASE);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);

        JLabel title = new JLabel("Thông báo hệ thống toàn trường (Broadcast)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Gửi thông báo chung cho TOÀN BỘ người dùng khi họ đăng nhập (bảo trì, khẩn cấp…)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(sub);
        head.add(text, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnThem = UITheme.primaryButton("+ Tạo thông báo");
        JButton btnSua = UITheme.secondaryButton("Sửa");
        JButton btnXoa = UITheme.dangerButton("Xóa");
        JButton btnTai = UITheme.secondaryButton("Tải lại");
        btnThem.addActionListener(e -> moForm(null));
        btnSua.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0 || danhSach == null) {
                UIUtils.thongBaoLoi(this, "Chọn 1 dòng");
                return;
            }
            moForm(danhSach.get(r));
        });
        btnXoa.addActionListener(e -> xoa());
        btnTai.addActionListener(e -> taiDuLieu());
        btns.add(btnTai);
        btns.add(btnSua);
        btns.add(btnXoa);
        btns.add(btnThem);
        head.add(btns, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
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
        dlg.setSize(520, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);

        JTextField txtTieuDe = new JTextField(sua == null ? "" : sua.getTieuDe());
        JTextArea txtNd = new JTextArea(sua == null ? "" : sua.getNoiDung(), 6, 30);
        txtNd.setLineWrap(true);
        txtNd.setWrapStyleWord(true);
        JComboBox<String> cboMuc = new JComboBox<>(new String[]{"THONG_TIN", "CANH_BAO", "KHAN_CAP"});
        if (sua != null) cboMuc.setSelectedItem(sua.getMucDo());
        JCheckBox chkBat = new JCheckBox("Đang bật (hiển thị cho user)", sua == null || sua.isDangBat());

        form.add(new JLabel("Tiêu đề"));
        form.add(txtTieuDe);
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(new JLabel("Nội dung"));
        form.add(new JScrollPane(txtNd));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(new JLabel("Mức độ"));
        form.add(cboMuc);
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(chkBat);
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(new JLabel("Hiệu lực từ ngay (đến = để trống = không hết hạn)"));

        dlg.add(form, BorderLayout.CENTER);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnHuy = new JButton("Hủy");
        JButton btnLuu = new JButton("Lưu");
        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            ThongBaoBroadcast b = sua == null ? new ThongBaoBroadcast() : sua;
            b.setTieuDe(txtTieuDe.getText().trim());
            b.setNoiDung(txtNd.getText().trim());
            b.setMucDo((String) cboMuc.getSelectedItem());
            b.setDangBat(chkBat.isSelected());
            final boolean laThem = (sua == null);
            if (laThem) {
                b.setHienThiTu(LocalDateTime.now());
                b.setNguoiTao(taiKhoan.getTenDangNhap());
            } else if (b.getHienThiTu() == null) {
                b.setHienThiTu(LocalDateTime.now());
            }
            // Ghi CSDL trên luồng nền
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (laThem) service.themBroadcast(b);
                    else service.capNhatBroadcast(b);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        UIUtils.thongBao(BroadcastPanel.this, "Đã lưu thông báo");
                        dlg.dispose();
                        taiDuLieu();
                    } catch (Exception ex) {
                        UIUtils.thongBaoLoi(dlg, ex.getMessage());
                    }
                }
            };
            w.execute();
        });
        acts.add(btnHuy);
        acts.add(btnLuu);
        dlg.add(acts, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void xoa() {
        int r = table.getSelectedRow();
        if (r < 0 || danhSach == null) {
            UIUtils.thongBaoLoi(this, "Chọn 1 dòng");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa thông báo này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        final int ma = danhSach.get(r).getMaBroadcast();
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.xoaBroadcast(ma);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    taiDuLieu();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(BroadcastPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }
}