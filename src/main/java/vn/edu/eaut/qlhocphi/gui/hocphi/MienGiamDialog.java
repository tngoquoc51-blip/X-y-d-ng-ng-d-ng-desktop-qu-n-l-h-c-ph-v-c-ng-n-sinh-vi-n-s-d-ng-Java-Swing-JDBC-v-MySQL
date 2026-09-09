package vn.edu.eaut.qlhocphi.gui.hocphi;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/** Dialog thiet lap % mien giam hoc phi / hoc bong cho 1 hoa don cu the.
 *  Ap dung ngay lap tuc: SoTien phai dong = SoTien goc * (100 - %) / 100. */
public class MienGiamDialog extends JDialog {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final HoaDonHocPhi hoaDon;
    private final Runnable khiThanhCong;

    private JSlider sliderTyLe;
    private JLabel lblTyLe, lblSoTienGiam, lblSoTienConLai;
    private JTextField txtLyDo;

    public MienGiamDialog(Frame owner, HoaDonHocPhi hoaDon, Runnable khiThanhCong) {
        super(owner, "", true);
        this.hoaDon = hoaDon;
        this.khiThanhCong = khiThanhCong;
        setSize(440, 460);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 80));
        JLabel iconTron = new JLabel("\uD83C\uDF93", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconTron.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconTron.setForeground(Color.WHITE);
        iconTron.setPreferredSize(new Dimension(44, 44));
        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("Miễn giảm học phí");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Hóa đơn #" + hoaDon.getMaHoaDon() + " - " + hoaDon.getTenSV());
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t2.setForeground(new Color(255, 255, 255, 200));
        chuText.add(t1);
        chuText.add(t2);
        banner.add(iconTron, BorderLayout.WEST);
        banner.add(chuText, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(22, 26, 10, 26));

        JLabel lblHocPhiGoc = nhan("Học phí gốc: " + MoneyUtils.format(hoaDon.getSoTien()));
        lblHocPhiGoc.setFont(UITheme.FONT_BOLD);
        form.add(lblHocPhiGoc);
        form.add(Box.createRigidArea(new Dimension(0, 16)));

        form.add(nhan("Tỷ lệ miễn giảm (%)"));
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        int tyLeHienTai = hoaDon.getTyLeMienGiam() != null ? hoaDon.getTyLeMienGiam().intValue() : 0;
        sliderTyLe = new JSlider(0, 100, tyLeHienTai);
        sliderTyLe.setOpaque(false);
        sliderTyLe.setMajorTickSpacing(25);
        sliderTyLe.setPaintTicks(true);
        sliderTyLe.setPaintLabels(true);
        sliderTyLe.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderTyLe.addChangeListener(e -> capNhatHienThi());
        form.add(sliderTyLe);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        lblTyLe = nhan("");
        lblTyLe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTyLe.setForeground(UITheme.PRIMARY);
        form.add(lblTyLe);
        form.add(Box.createRigidArea(new Dimension(0, 14)));

        lblSoTienGiam = nhan("");
        lblSoTienGiam.setForeground(UITheme.SUCCESS);
        form.add(lblSoTienGiam);
        lblSoTienConLai = nhan("");
        lblSoTienConLai.setFont(UITheme.FONT_BOLD);
        form.add(lblSoTienConLai);
        form.add(Box.createRigidArea(new Dimension(0, 16)));

        form.add(nhan("Lý do miễn giảm (VD: Học bổng khuyến khích, Diện chính sách...)"));
        form.add(Box.createRigidArea(new Dimension(0, 6)));
        txtLyDo = new JTextField(hoaDon.getLyDoMienGiam() != null ? hoaDon.getLyDoMienGiam() : "");
        txtLyDo.setFont(UITheme.FONT_BASE);
        txtLyDo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        txtLyDo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtLyDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(txtLyDo);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnHuy = UITheme.secondaryButton("Hủy");
        JButton btnLuu = UITheme.primaryButton("Áp dụng miễn giảm");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> apDung());
        actions.add(btnHuy);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);

        capNhatHienThi();
    }

    private void capNhatHienThi() {
        int tyLe = sliderTyLe.getValue();
        BigDecimal soTienGiam = hoaDon.getSoTien().multiply(BigDecimal.valueOf(tyLe))
                .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
        BigDecimal conLai = hoaDon.getSoTien().subtract(soTienGiam);
        lblTyLe.setText(tyLe + "%");
        lblSoTienGiam.setText("Số tiền được giảm: " + MoneyUtils.format(soTienGiam));
        lblSoTienConLai.setText("Số tiền phải đóng sau giảm: " + MoneyUtils.format(conLai));
    }

    private void apDung() {
        int tyLe = sliderTyLe.getValue();
        String lyDo = txtLyDo.getText().trim();
        if (tyLe > 0 && lyDo.isEmpty()) {
            UIUtils.thongBaoLoi(this, "Vui lòng nhập lý do miễn giảm.");
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                hoaDonDAO.capNhatMienGiam(hoaDon.getMaHoaDon(), BigDecimal.valueOf(tyLe), lyDo.isEmpty() ? null : lyDo);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(MienGiamDialog.this, "Đã áp dụng miễn giảm " + tyLe + "%.");
                    if (khiThanhCong != null) khiThanhCong.run();
                    dispose();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(MienGiamDialog.this, "Lỗi: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JLabel nhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}