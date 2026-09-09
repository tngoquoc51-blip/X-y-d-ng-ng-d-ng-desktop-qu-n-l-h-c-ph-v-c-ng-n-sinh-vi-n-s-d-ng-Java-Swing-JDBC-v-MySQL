package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.ThuTuDongService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.LichThuTuDong;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog tao lich thu tu dong HANG LOAT cho TAT CA hoa don con no trong he thong
 * (khong con chon tung hoa don rieng le) - chi can chon 1 khoang ngay quet dung
 * chung, ap dung dong loat cho moi sinh vien dang con no hoc phi.
 */
public class TaoLichThuDialog extends JDialog {
    private final ThuTuDongService thuTuDongService = new ThuTuDongService();
    private final CongNoService congNoService = new CongNoService();

    private JSpinner spNgayBatDau, spNgayKetThuc;
    private JLabel lblSoLuongConNo;

    public TaoLichThuDialog(Window chaMe, TaiKhoan taiKhoan, Runnable khiThanhCong) {
        super(chaMe, "Tạo Lịch Thu Tự Động (Toàn Bộ Sinh Viên Còn Nợ)", ModalityType.APPLICATION_MODAL);
        setSize(460, 300);
        setLocationRelativeTo(chaMe);
        setResizable(false);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        noiDung.setBackground(Color.WHITE);

        int soLuongConNo = 0;
        try {
            soLuongConNo = congNoService.layDanhSachConNo().size();
        } catch (Exception ignored) { }

        JLabel lblMoTa = new JLabel("<html>Thao tác này sẽ tự động lập lịch thu cho <b>TẤT CẢ</b> hóa đơn"
                + " đang còn nợ trong toàn hệ thống (không chọn riêng từng sinh viên).</html>");
        lblMoTa.setFont(UITheme.FONT_BASE);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSoLuongConNo = new JLabel(soLuongConNo + " hóa đơn đang còn nợ sẽ được lập lịch");
        lblSoLuongConNo.setFont(UITheme.FONT_BOLD);
        lblSoLuongConNo.setForeground(UITheme.PRIMARY);
        lblSoLuongConNo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSoLuongConNo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel lblBatDau = new JLabel("Bắt đầu quét từ ngày:");
        lblBatDau.setFont(UITheme.FONT_BOLD);
        lblBatDau.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBatDau.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        spNgayBatDau = new JSpinner(new SpinnerDateModel());
        spNgayBatDau.setEditor(new JSpinner.DateEditor(spNgayBatDau, "dd/MM/yyyy"));
        spNgayBatDau.setAlignmentX(Component.LEFT_ALIGNMENT);
        spNgayBatDau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lblKetThuc = new JLabel("Quét đến ngày (hết hạn ủy quyền):");
        lblKetThuc.setFont(UITheme.FONT_BOLD);
        lblKetThuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblKetThuc.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        spNgayKetThuc = new JSpinner(new SpinnerDateModel());
        spNgayKetThuc.setEditor(new JSpinner.DateEditor(spNgayKetThuc, "dd/MM/yyyy"));
        spNgayKetThuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        spNgayKetThuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton btnTao = UITheme.primaryButton("Tạo Lịch Thu Cho Tất Cả");
        btnTao.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnTao.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        btnTao.addActionListener(e -> taoLichHangLoat(taiKhoan, khiThanhCong));

        noiDung.add(lblMoTa);
        noiDung.add(lblSoLuongConNo);
        noiDung.add(lblBatDau);
        noiDung.add(spNgayBatDau);
        noiDung.add(lblKetThuc);
        noiDung.add(spNgayKetThuc);
        noiDung.add(btnTao);

        setContentPane(noiDung);
    }

    private void taoLichHangLoat(TaiKhoan taiKhoan, Runnable khiThanhCong) {
        LocalDate batDau = toLocalDate((java.util.Date) spNgayBatDau.getValue());
        LocalDate ketThuc = toLocalDate((java.util.Date) spNgayKetThuc.getValue());
        if (ketThuc.isBefore(batDau)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<HoaDonHocPhi> dsConNo = congNoService.layDanhSachConNo();
            if (dsConNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hiện không có sinh viên nào còn nợ học phí.");
                return;
            }

            // Lay truoc danh sach hoa don DA co lich DANG_CHO, de bo qua - tranh tao
            // trung nhieu lich cho cung 1 hoa don khi bam nut nay nhieu lan.
            Set<Integer> daCoLich = new HashSet<>();
            for (LichThuTuDong l : thuTuDongService.layTatCaLich()) {
                if ("DANG_CHO".equals(l.getTrangThai())) daCoLich.add(l.getMaHoaDon());
            }

            int daTao = 0, boQuaDaCoLich = 0, loi = 0;
            String nguoiTao = taiKhoan != null ? taiKhoan.getTenDangNhap() : "he_thong";

            for (HoaDonHocPhi hd : dsConNo) {
                if (daCoLich.contains(hd.getMaHoaDon())) {
                    boQuaDaCoLich++;
                    continue;
                }
                try {
                    thuTuDongService.taoLichThu(hd.getMaHoaDon(), batDau, ketThuc, nguoiTao);
                    daTao++;
                } catch (Exception ex) {
                    loi++;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã tạo lịch thu tự động cho " + daTao + " hóa đơn.\n"
                            + (boQuaDaCoLich > 0 ? "- Bỏ qua " + boQuaDaCoLich + " hóa đơn đã có lịch đang chờ từ trước.\n" : "")
                            + (loi > 0 ? "- Lỗi " + loi + " hóa đơn (xem log để biết chi tiết).\n" : ""),
                    "Kết quả tạo lịch hàng loạt", JOptionPane.INFORMATION_MESSAGE);
            khiThanhCong.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(java.util.Date date) {
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}