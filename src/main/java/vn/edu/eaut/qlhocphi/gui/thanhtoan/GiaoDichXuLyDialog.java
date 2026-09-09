package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.DoiSoatNganHangService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.GiaoDichNganHang;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Dialog cho Admin/Ke toan xu ly 1 giao dich: xac nhan hoa don goi y (hoac tu chon
 *  hoa don khac trong danh sach con no), hoac bo qua giao dich neu khong lien quan. */
public class GiaoDichXuLyDialog extends JDialog {
    private final DoiSoatNganHangService service = new DoiSoatNganHangService();
    private final GiaoDichNganHang giaoDich;
    private final List<HoaDonHocPhi> danhSachConNo;
    private final TaiKhoan taiKhoan;
    private boolean daXuLy = false;

    private JComboBox<HoaDonHocPhi> cboHoaDon;

    public GiaoDichXuLyDialog(Frame owner, GiaoDichNganHang giaoDich, List<HoaDonHocPhi> danhSachConNo, TaiKhoan taiKhoan) {
        super(owner, "Xu ly giao dich chuyen khoan", true);
        this.giaoDich = giaoDich;
        this.danhSachConNo = danhSachConNo;
        this.taiKhoan = taiKhoan;

        setLayout(new BorderLayout(0, 14));
        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(20, 22, 16, 22));

        dongThongTin(noiDung, "So tien chuyen", MoneyUtils.format(giaoDich.getSoTien()));
        dongThongTin(noiDung, "Noi dung CK", giaoDich.getNoiDung());
        dongThongTin(noiDung, "Ma tham chieu", giaoDich.getMaThamChieu());
        if (giaoDich.getDoTinCay() != null) {
            dongThongTin(noiDung, "Do tin cay he thong", giaoDich.getDoTinCay() + "%");
        }

        noiDung.add(Box.createRigidArea(new Dimension(0, 14)));
        JLabel lblChonHD = new JLabel("Chon hoa don khop dung:");
        lblChonHD.setFont(UITheme.FONT_BOLD);
        lblChonHD.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(lblChonHD);
        noiDung.add(Box.createRigidArea(new Dimension(0, 6)));

        cboHoaDon = new JComboBox<>(danhSachConNo.toArray(new HoaDonHocPhi[0]));
        cboHoaDon.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof HoaDonHocPhi hd) {
                    setText(hd.getMaSV() + " - " + hd.getTenSV() + " (" + hd.getTenHocKy()
                            + ") - Con no: " + MoneyUtils.format(hd.tinhConNo()));
                }
                return this;
            }
        });
        // Neu he thong da co goi y (TU_DONG_KHOP / NGHI_VAN), chon san hoa don do trong combo.
        if (giaoDich.getMaHoaDonKhop() != null) {
            danhSachConNo.stream()
                    .filter(hd -> hd.getMaHoaDon() == giaoDich.getMaHoaDonKhop())
                    .findFirst().ifPresent(cboHoaDon::setSelectedItem);
        }
        cboHoaDon.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboHoaDon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        noiDung.add(cboHoaDon);

        add(noiDung, BorderLayout.CENTER);
        add(buildNutHanhDong(), BorderLayout.SOUTH);

        setSize(480, 380);
        setLocationRelativeTo(owner);
    }

    private void dongThongTin(JPanel container, String nhan, String giaTri) {
        JPanel dong = new JPanel(new BorderLayout(16, 0));
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UITheme.FONT_BASE);
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        JLabel lblGiaTri = new JLabel(giaTri == null ? "-" : giaTri);
        lblGiaTri.setFont(UITheme.FONT_BOLD);
        dong.add(lblNhan, BorderLayout.WEST);
        dong.add(lblGiaTri, BorderLayout.EAST);
        container.add(dong);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JPanel buildNutHanhDong() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 16));

        JButton btnBoQua = UITheme.secondaryButton("Bo qua giao dich nay");
        btnBoQua.addActionListener(e -> boQua());

        JButton btnXacNhan = UITheme.primaryButton("Xac nhan da thu");
        btnXacNhan.addActionListener(e -> xacNhan());

        panel.add(btnBoQua);
        panel.add(btnXacNhan);
        return panel;
    }

    private void xacNhan() {
        HoaDonHocPhi hd = (HoaDonHocPhi) cboHoaDon.getSelectedItem();
        if (hd == null) {
            UIUtils.thongBaoLoi(this, "Vui long chon 1 hoa don.");
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xac nhan da thu " + MoneyUtils.format(giaoDich.getSoTien()) + " cho hoa don cua "
                        + hd.getTenSV() + " (" + hd.getMaSV() + ")?",
                "Xac nhan thu tien", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        try {
            service.xacNhanKhop(giaoDich, hd, taiKhoan);
            daXuLy = true;
            UIUtils.thongBao(this, "Da xac nhan thu tien thanh cong.");
            dispose();
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Xac nhan that bai: " + ex.getMessage());
        }
    }

    private void boQua() {
        try {
            service.boQua(giaoDich.getMaGiaoDich());
            daXuLy = true;
            dispose();
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Khong the bo qua giao dich: " + ex.getMessage());
        }
    }

    public boolean daXuLy() { return daXuLy; }
}