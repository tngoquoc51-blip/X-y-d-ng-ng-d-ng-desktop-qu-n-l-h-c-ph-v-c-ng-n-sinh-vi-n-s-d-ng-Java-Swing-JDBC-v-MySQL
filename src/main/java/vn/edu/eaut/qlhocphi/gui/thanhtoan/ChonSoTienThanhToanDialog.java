package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Buoc "chon so tien muon thanh toan" - hien ra TRUOC PaymentMethodDialog.
 * Sinh vien co the dong het no hoac dong 1 phan, nhung KHONG duoc vuot qua
 * HAN_MUC_MOT_LAN (50 trieu) vi cong thanh toan demo (VNPay/MoMo sandbox +
 * gateway tu viet bang AI) chua xu ly duoc giao dich qua lon trong 1 lan.
 *
 * Cach dung (thay vi mo thang PaymentMethodDialog nhu truoc):
 *   new ChonSoTienThanhToanDialog(chaMe, hd.tinhConNo(), soTienDaChon -> {
 *       new PaymentMethodDialog(chaMe, hd.getMaHoaDon(), soTienDaChon, khiThanhCong).setVisible(true);
 *   }).setVisible(true);
 */
public class ChonSoTienThanhToanDialog extends JDialog {
    /** Han muc toi da cho 1 lan giao dich - dieu chinh tai day neu nang cap gateway sau nay. */
    public static final BigDecimal HAN_MUC_MOT_LAN = BigDecimal.valueOf(50_000_000);

    private final BigDecimal conNo;
    private final Consumer<BigDecimal> khiXacNhan;

    private JTextField txtSoTien;
    private JLabel lblThongBao;
    private JButton btnTiepTuc;

    public ChonSoTienThanhToanDialog(Window chaMe, BigDecimal conNo, Consumer<BigDecimal> khiXacNhan) {
        super(chaMe, "Chọn số tiền thanh toán", ModalityType.APPLICATION_MODAL);
        this.conNo = conNo;
        this.khiXacNhan = khiXacNhan;
        setSize(420, conNo.compareTo(HAN_MUC_MOT_LAN) > 0 ? 460 : 400);
        setMinimumSize(new Dimension(380, 380));
        setLocationRelativeTo(chaMe);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildNoiDung(), BorderLayout.CENTER);
    }

    private JPanel buildNoiDung() {
        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(24, 26, 22, 26));

        JLabel tieuDe = new JLabel("Chọn số tiền thanh toán");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel moTaConNo = new JLabel("Số tiền còn nợ: " + MoneyUtils.format(conNo));
        moTaConNo.setFont(UITheme.FONT_BASE);
        moTaConNo.setForeground(UITheme.TEXT_MUTED);
        moTaConNo.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTaConNo.setBorder(new EmptyBorder(4, 0, 18, 0));

        wrap.add(tieuDe);
        wrap.add(moTaConNo);

        // Neu con no vuot han muc 1 lan, canh bao ro va cho phep dong toi da phan cho phep
        if (conNo.compareTo(HAN_MUC_MOT_LAN) > 0) {
            JPanel canhBao = new JPanel(new BorderLayout(10, 0));
            canhBao.setBackground(UITheme.TINT_RED);
            canhBao.setBorder(new EmptyBorder(10, 12, 10, 12));
            canhBao.setAlignmentX(Component.LEFT_ALIGNMENT);
            canhBao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            JLabel icon = new JLabel("\u26A0");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            icon.setForeground(UITheme.TEXT_RED);
            canhBao.add(icon, BorderLayout.WEST);
            JLabel chu = new JLabel("<html><body style='width:260px'>Mỗi lần thanh toán online chỉ được tối đa "
                    + MoneyUtils.format(HAN_MUC_MOT_LAN) + ". Số nợ của bạn vượt mức này nên cần thanh toán thành nhiều lần.</body></html>");
            chu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chu.setForeground(UITheme.TEXT_RED);
            canhBao.add(chu, BorderLayout.CENTER);
            wrap.add(canhBao);
            wrap.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        wrap.add(UIUtils.formLabel("Số tiền muốn thanh toán (đ)"));
        wrap.add(Box.createRigidArea(new Dimension(0, 6)));

        txtSoTien = UIUtils.textField(20);
        txtSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSoTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtSoTien.setText(dinhDangSo(soTienGoiY()));
        wrap.add(txtSoTien);

        wrap.add(Box.createRigidArea(new Dimension(0, 10)));

        // 2 nut chon nhanh: dong toan bo (neu duoc phep) va dong theo han muc toi da
        JPanel hangNutNhanh = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangNutNhanh.setOpaque(false);
        hangNutNhanh.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangNutNhanh.setBorder(new EmptyBorder(0, 0, 0, 0));

        if (conNo.compareTo(HAN_MUC_MOT_LAN) <= 0) {
            JButton btnDongDu = nutNho("Đóng đủ (" + MoneyUtils.format(conNo) + ")");
            btnDongDu.addActionListener(e -> txtSoTien.setText(dinhDangSo(conNo)));
            hangNutNhanh.add(btnDongDu);
        } else {
            JButton btnDongToiDa = nutNho("Đóng tối đa (" + MoneyUtils.format(HAN_MUC_MOT_LAN) + ")");
            btnDongToiDa.addActionListener(e -> txtSoTien.setText(dinhDangSo(HAN_MUC_MOT_LAN)));
            hangNutNhanh.add(btnDongToiDa);
        }
        wrap.add(hangNutNhanh);

        lblThongBao = new JLabel(" ", SwingConstants.LEFT);
        lblThongBao.setFont(UITheme.FONT_BASE);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongBao.setBorder(new EmptyBorder(12, 0, 8, 0));
        wrap.add(lblThongBao);

        btnTiepTuc = UITheme.primaryButton("Tiếp tục chọn phương thức");
        btnTiepTuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTiepTuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnTiepTuc.addActionListener(e -> xacNhan());

        JButton btnHuy = UITheme.secondaryButton("Hủy");
        btnHuy.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHuy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnHuy.addActionListener(e -> dispose());

        wrap.add(btnTiepTuc);
        wrap.add(Box.createRigidArea(new Dimension(0, 8)));
        wrap.add(btnHuy);

        return wrap;
    }

    private JButton nutNho(String text) {
        JButton b = UITheme.secondaryButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return b;
    }

    /** So tien goi y mac dinh khi mo dialog: dong het no neu <= han muc, con khong thi goi y dung han muc toi da. */
    private BigDecimal soTienGoiY() {
        return conNo.compareTo(HAN_MUC_MOT_LAN) <= 0 ? conNo : HAN_MUC_MOT_LAN;
    }

    private String dinhDangSo(BigDecimal so) {
        return so.toBigInteger().toString();
    }

    private void xacNhan() {
        String raw = txtSoTien.getText().replaceAll("[^0-9]", "");
        if (raw.isEmpty()) {
            baoLoi("Vui lòng nhập số tiền muốn thanh toán");
            return;
        }

        BigDecimal soTien;
        try {
            soTien = new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            baoLoi("Số tiền không hợp lệ");
            return;
        }

        if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
            baoLoi("Số tiền phải lớn hơn 0");
            return;
        }
        if (soTien.compareTo(conNo) > 0) {
            baoLoi("Số tiền không được vượt quá số còn nợ (" + MoneyUtils.format(conNo) + ")");
            return;
        }
        if (soTien.compareTo(HAN_MUC_MOT_LAN) > 0) {
            baoLoi("Mỗi lần thanh toán online tối đa " + MoneyUtils.format(HAN_MUC_MOT_LAN));
            return;
        }

        dispose();
        if (khiXacNhan != null) khiXacNhan.accept(soTien);
    }

    private void baoLoi(String text) {
        lblThongBao.setText(text);
    }
}