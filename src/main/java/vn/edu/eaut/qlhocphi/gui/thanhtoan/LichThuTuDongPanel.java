package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.ThuTuDongService;
import vn.edu.eaut.qlhocphi.bus.TuDongQuetScheduler;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.LichThuTuDong;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Man hinh quan tri "Uy quyen trich no tu dong": tao lich quet cho 1 hoa don con
 * no (chon khoang ngay), xem danh sach lich + trang thai, va nut "Quet thu ngay"
 * de kich hoat thu cong 1 lan (khong can cho scheduler den chu ky).
 */
public class LichThuTuDongPanel extends JPanel {
    private final ThuTuDongService thuTuDongService = new ThuTuDongService();
    private final TaiKhoan taiKhoan;

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblTongLich, lblDangCho, lblDaThu;
    private JLabel lblTrangThaiTuDong;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LichThuTuDongPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildThongKeCards());
        north.add(Box.createRigidArea(new Dimension(0, 10)));
        north.add(buildTrangThaiTuDongRow());
        add(north, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();

        // Moi lan bo lich tu dong vua quet xong nen, tu lam moi bang + dong ho trang thai,
        // Admin khong can bam tay nua.
        TuDongQuetScheduler.getInstance().themNguoiNghe(kq -> SwingUtilities.invokeLater(() -> {
            lblTrangThaiTuDong.setText(TuDongQuetScheduler.getInstance().moTaTrangThai());
            taiDuLieu();
        }));
    }

    private JPanel buildTrangThaiTuDongRow() {
        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        lblTrangThaiTuDong = new JLabel(TuDongQuetScheduler.getInstance().moTaTrangThai());
        lblTrangThaiTuDong.setFont(UITheme.FONT_BASE);
        lblTrangThaiTuDong.setForeground(UITheme.TEXT_MUTED);
        hang.add(lblTrangThaiTuDong, BorderLayout.WEST);
        return hang;
    }

    private void moDialogCauHinhTuDong() {
        new CauHinhTuDongQuetDialog(SwingUtilities.getWindowAncestor(this), () -> {
            lblTrangThaiTuDong.setText(TuDongQuetScheduler.getInstance().moTaTrangThai());
            taiDuLieu();
        }).setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(UITheme.PRIMARY);
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Ủy Quyền Trích Nợ Tự Động");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel moTa = new JLabel("Tự động trừ Ví học phí điện tử của sinh viên khi đến kỳ hạn, không cần thao tác thủ công");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(new Color(255, 255, 255, 220));
        trai.add(tieuDe);
        trai.add(moTa);

        JButton btnTaoLich = UITheme.secondaryButton("+ Tạo Lịch Thu Mới");
        btnTaoLich.addActionListener(e -> moDialogTaoLich());

        JButton btnQuetNgay = new JButton("Quét Thu Ngay");
        btnQuetNgay.setFocusPainted(false);
        btnQuetNgay.setBackground(UITheme.SUCCESS);
        btnQuetNgay.setForeground(Color.WHITE);
        btnQuetNgay.setBorderPainted(false);
        btnQuetNgay.addActionListener(e -> quetThuNgay());

        JButton btnCauHinhTuDong = new JButton("Cấu Hình Tự Động");
        btnCauHinhTuDong.setFocusPainted(false);
        btnCauHinhTuDong.setBackground(Color.WHITE);
        btnCauHinhTuDong.setForeground(UITheme.PRIMARY);
        btnCauHinhTuDong.setBorderPainted(false);
        btnCauHinhTuDong.addActionListener(e -> moDialogCauHinhTuDong());

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        phai.setOpaque(false);
        phai.add(btnQuetNgay);
        phai.add(btnCauHinhTuDong);
        phai.add(btnTaoLich);

        banner.add(trai, BorderLayout.WEST);
        banner.add(phai, BorderLayout.EAST);
        return banner;
    }

    private JPanel buildThongKeCards() {
        JPanel hang = new JPanel(new GridLayout(1, 3, 16, 0));
        hang.setOpaque(false);
        lblTongLich = new JLabel("0");
        lblDangCho = new JLabel("0");
        lblDaThu = new JLabel("0");
        hang.add(theThongKe("Tổng số lịch", lblTongLich, UITheme.TEXT_BLUE));
        hang.add(theThongKe("Đang chờ quét", lblDangCho, UITheme.WARNING));
        hang.add(theThongKe("Đã thu tự động", lblDaThu, UITheme.SUCCESS));
        return hang;
    }

    private JPanel theThongKe(String nhan, JLabel lblSo, Color mau) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(UITheme.BG_CARD);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UITheme.FONT_BASE);
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblSo.setForeground(mau);
        the.add(lblNhan);
        the.add(lblSo);
        return the;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 16, 16, 16)));

        String[] cot = {"Mã Lịch", "Mã HĐ", "Sinh Viên", "Từ Ngày", "Đến Ngày", "Trạng Thái", "Lần Quét Gần Nhất"};
        tableModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UITheme.FONT_BASE);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem huy = new JMenuItem("Hủy lịch này");
        huy.addActionListener(e -> huyLichDangChon());
        popup.add(huy);
        table.setComponentPopupMenu(popup);

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void taiDuLieu() {
        try {
            List<LichThuTuDong> danhSach = thuTuDongService.layTatCaLich();
            tableModel.setRowCount(0);
            int dangCho = 0, daThu = 0;
            for (LichThuTuDong l : danhSach) {
                tableModel.addRow(new Object[]{
                        l.getMaLich(), l.getMaHoaDon(), l.getTenSV() + " (" + l.getMaSV() + ")",
                        DINH_DANG_NGAY.format(l.getNgayBatDauQuet()),
                        DINH_DANG_NGAY.format(l.getNgayKetThucQuet()),
                        l.getTrangThai(),
                        l.getLanQuetGanNhat() != null ? l.getLanQuetGanNhat().toString() : "Chưa quét lần nào"
                });
                if ("DANG_CHO".equals(l.getTrangThai())) dangCho++;
                if ("DA_THU".equals(l.getTrangThai())) daThu++;
            }
            lblTongLich.setText(String.valueOf(danhSach.size()));
            lblDangCho.setText(String.valueOf(dangCho));
            lblDaThu.setText(String.valueOf(daThu));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huyLichDangChon() {
        int hang = table.getSelectedRow();
        if (hang < 0) return;
        int maLich = (int) tableModel.getValueAt(hang, 0);
        int xacNhan = JOptionPane.showConfirmDialog(this, "Hủy lịch thu tự động #" + maLich + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        try {
            thuTuDongService.huyLich(maLich);
            taiDuLieu();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quetThuNgay() {
        SwingWorker<ThuTuDongService.KetQuaQuet, Void> worker = new SwingWorker<>() {
            @Override
            protected ThuTuDongService.KetQuaQuet doInBackground() throws Exception {
                return thuTuDongService.quetMotLan();
            }

            @Override
            protected void done() {
                try {
                    ThuTuDongService.KetQuaQuet kq = get();
                    JOptionPane.showMessageDialog(LichThuTuDongPanel.this,
                            "Đã quét " + kq.tongQuet + " lịch:\n"
                                    + "- Thu thành công: " + kq.thuThanhCong + "\n"
                                    + "- Chưa đủ tiền (giữ lại quét lần sau): " + kq.boQuaChuaDuTien + "\n"
                                    + "- Hết hạn: " + kq.hetHan,
                            "Kết quả quét", JOptionPane.INFORMATION_MESSAGE);
                    taiDuLieu();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LichThuTuDongPanel.this,
                            "Lỗi khi quét: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void moDialogTaoLich() {
        new TaoLichThuDialog((Window) SwingUtilities.getWindowAncestor(this), taiKhoan, this::taiDuLieu)
                .setVisible(true);
    }
}